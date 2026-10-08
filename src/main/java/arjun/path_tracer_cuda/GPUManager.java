package arjun.path_tracer_cuda;

import arjun.path_tracer_cuda.geometry.*;
import arjun.path_tracer_cuda.geometry.BVH.AABB;
import arjun.path_tracer_cuda.geometry.BVH.BVHNode;
import arjun.path_tracer_cuda.geometry.BVH.EmissiveBVHNode;
import arjun.path_tracer_cuda.geometry.BVH.PLightBVHNode;
import arjun.path_tracer_cuda.kernel.Kernel;
import arjun.path_tracer_cuda.kernel.KernelManager;
import arjun.path_tracer_cuda.kernel.SampleKernel;
import arjun.path_tracer_cuda.preset_scenes.PresetScenesManager;
import jcuda.Pointer;
import jcuda.Sizeof;
import jcuda.driver.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL15.GL_DYNAMIC_DRAW;
import static org.lwjgl.opengl.GL21.GL_PIXEL_UNPACK_BUFFER;

public class GPUManager {

    private Window window;

    public static GPUManager instance;

    public int textureId;
    public int pboId;

    public CUdevice device;
    public CUcontext context;

    public CUgraphicsResource pboResource = new CUgraphicsResource();

    public KernelManager kernelManager;

    public int samples = 1024;


    //  Scene stuff ---------------------------------
    public ArrayList<Triangle> triangles = new ArrayList<>();
    public ArrayList<Sphere> spheres = new ArrayList<>();
    public ArrayList<PointLight> pointLights = new ArrayList<>();

    public ArrayList<SceneObject> objects = new ArrayList<>();

    public CUdeviceptr[] sceneDataDevicePtrs;
    public int[] sceneDataSizes;

    public AABB[] primBounds; // aabb for each primitive (triangle or sphere)
    public Vec3[] primCentroids; // centroid (center) of each primitive
    public Vec3[] pLightCentroids;

    public int[] primitiveIDs; // the ids for each primitive (points to GPUManager objects arraylist), to be shuffled later
    public int[] pLightIDs; // the ids for each primitive (points to GPUManager objects arraylist), to be shuffled later
    public int[] emissiveIDs; // the ids for each emissive primitive (points to GPUManager objects arraylist, like primitiveIDs), to be shuffled later

    public float[] primMinBounds;
    public float[] primMaxBounds;
    public int[] primCounts; // always the prim counts
    public int[] primIdx; // for leafs: the first prim index. for interiors: index of right child

    public float[] pLightMinBounds;
    public float[] pLightMaxBounds;
    public int[] pLightCounts; // always the prim counts
    public int[] pLightIdx; // for leafs: the first prim index. for interiors: index of right child
    public float[] pLightPower; // 3 floats per node: the summed power of the lights under it

    public float[] emissiveMinBounds;
    public float[] emissiveMaxBounds;
    public int[] emissiveCounts; // always the prim counts
    public int[] emissiveIdx; // for leafs: the first prim index. for interiors: index of right child
    public float[] emissivePower; // 3 floats per node: the summed power of the emissives under it
    //  ---------------------------------------------

    public GPUManager(Window window, int sceneNumber) {
        this(window, sceneNumber, new Vec3(0, 0, 0));
    }

    /* positionOffset moves the scene relative to the camera: +x right, +y up, +z towards the camera,
     * in the scene's own units (see PresetScenesManager.loadScene) - (0, 0, -5) steps the camera back 5. */
    public GPUManager(Window window, int sceneNumber, Vec3 positionOffset) {
        this.window = window;
        instance = this;

        // scene init
        initJCuda();
        initBuffers();

        PresetScenesManager.loadScene(sceneNumber, triangles, spheres, pointLights, new Vec3(positionOffset));

        objects.addAll(triangles);
        objects.addAll(spheres);

        // bvh init
        primitiveIDs = new int[GPUManager.instance.objects.size()];
        pLightIDs = new int[GPUManager.instance.pointLights.size()];

        primBounds = new AABB[GPUManager.instance.objects.size()];
        primCentroids = new Vec3[GPUManager.instance.objects.size()];
        pLightCentroids = new Vec3[GPUManager.instance.pointLights.size()];

        for (int i = 0; i < GPUManager.instance.objects.size(); i++) {
            GPUManager.instance.primBounds[i] = AABB.computeBounds(i);
            GPUManager.instance.primCentroids[i] = AABB.centroidOf(i);
            primitiveIDs[i] = i;
        }

        for (int i = 0; i < GPUManager.instance.pointLights.size(); i++) {
            GPUManager.instance.pLightCentroids[i] = GPUManager.instance.pointLights.get(i).pos;
            pLightIDs[i] = i;
        }

        // only the primitives that give off light. they reuse primBounds and primCentroids, since they're in the same objects list
        emissiveIDs = IntStream.range(0, GPUManager.instance.objects.size())
                .filter(id -> GPUManager.instance.objects.get(id).getMaterial().isEmissive())
                .toArray();


        BVHNode root = new BVHNode();
        root = root.build(0, primitiveIDs.length, 0);
        assert root != null;

        // fill bvh data
        int maxNodes = 2 * GPUManager.instance.objects.size() - 1; // worst case: every leaf holds 1 prim -> N leaves -> 2N-1 nodes
        primMinBounds = new float[maxNodes*3];
        primMaxBounds = new float[maxNodes*3];
        primCounts = new int[maxNodes];
        primIdx = new int[maxNodes];

        int flatten = BVHNode.flatten(root);
        assert flatten == 0;

        // point light bvh
        int maxLightNodes = Math.max(0, 2 * GPUManager.instance.pointLights.size() - 1);
        pLightMinBounds = new float[maxLightNodes*3];
        pLightMaxBounds = new float[maxLightNodes*3];
        pLightCounts = new int[maxLightNodes];
        pLightIdx = new int[maxLightNodes];
        pLightPower = new float[maxLightNodes*3];

        if (!pointLights.isEmpty()) {
            PLightBVHNode lightRoot = (PLightBVHNode) new PLightBVHNode().build(0, pLightIDs.length, 0);
            int lightFlatten = PLightBVHNode.flatten(lightRoot);
            assert lightFlatten == 0;
        }

        // emissive bvh
        int maxEmissiveNodes = Math.max(0, 2 * emissiveIDs.length - 1);
        emissiveMinBounds = new float[maxEmissiveNodes*3];
        emissiveMaxBounds = new float[maxEmissiveNodes*3];
        emissiveCounts = new int[maxEmissiveNodes];
        emissiveIdx = new int[maxEmissiveNodes];
        emissivePower = new float[maxEmissiveNodes*3];

        if (emissiveIDs.length > 0) {
            EmissiveBVHNode emissiveRoot = (EmissiveBVHNode) new EmissiveBVHNode().build(0, emissiveIDs.length, 0);
            int emissiveFlatten = EmissiveBVHNode.flatten(emissiveRoot);
            assert emissiveFlatten == 0;
        }

        // send scene data + final inits
        sendSceneData();
        samples = 8192;
        initKernels();
    }

    public Triangle getTriangle(int id) {
        if (objects.get(id) instanceof Triangle) return (Triangle) instance.objects.get(id);
        return null;
    }

    public Sphere getSphere(int id) {
        if (objects.get(id) instanceof Sphere) return (Sphere) instance.objects.get(id);
        return null;
    }

    private void initBuffers() {
        textureId = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureId);

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, window.WIDTH, window.HEIGHT, 0, GL_RGBA, GL_UNSIGNED_BYTE, (ByteBuffer) null);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glBindTexture(GL_TEXTURE_2D, 0);

        pboId = glGenBuffers();
        glBindBuffer(GL_PIXEL_UNPACK_BUFFER, pboId);

        long byteSize = (long) window.WIDTH * window.HEIGHT * 4;
        glBufferData(GL_PIXEL_UNPACK_BUFFER, byteSize, GL_DYNAMIC_DRAW);

        glBindBuffer(GL_PIXEL_UNPACK_BUFFER, 0); // Unbind when done

        JCudaDriver.cuGraphicsGLRegisterBuffer(
                pboResource,
                pboId,
                CUgraphicsRegisterFlags.CU_GRAPHICS_REGISTER_FLAGS_WRITE_DISCARD
        );
    }

    private void initJCuda() {
        JCudaDriver.setExceptionsEnabled(true);
        JCudaDriver.cuInit(0);

        device = new CUdevice();
        JCudaDriver.cuDeviceGet(device, 0);
        context = new CUcontext();
        JCudaDriver.cuCtxCreate(context, 0, device);
    }

    private void sendSceneData() {

        float[] triangleData = new float[triangles.size() * 20];
        float[] sphereData = new float[spheres.size() * 15];
        float[] pLightData = new float[pointLights.size() * 7];

        int i = 0;

        for (Triangle triangle : triangles) {
            triangleData[i] = triangle.p1.x;
            triangleData[i + 1] = triangle.p1.y;
            triangleData[i + 2] = triangle.p1.z;

            triangleData[i + 3] = triangle.p2.x;
            triangleData[i + 4] = triangle.p2.y;
            triangleData[i + 5] = triangle.p2.z;

            triangleData[i + 6] = triangle.p3.x;
            triangleData[i + 7] = triangle.p3.y;
            triangleData[i + 8] = triangle.p3.z;

            triangleData[i + 9] = sRGBtoLinear(triangle.material.color.x);
            triangleData[i + 10] = sRGBtoLinear(triangle.material.color.y);
            triangleData[i + 11] = sRGBtoLinear(triangle.material.color.z);

            triangleData[i + 12] = triangle.material.metallic;
            triangleData[i + 13] = triangle.material.roughness;
            triangleData[i + 14] = triangle.material.transmission;
            triangleData[i + 15] = triangle.material.ior;
            triangleData[i + 16] = triangle.material.density;

            Vec3 triangleEmission = triangle.material.linearEmission(); // already linear, intensity multiplied in
            triangleData[i + 17] = triangleEmission.x;
            triangleData[i + 18] = triangleEmission.y;
            triangleData[i + 19] = triangleEmission.z;

            i += 20;
        }

        i = 0;

        for (Sphere sphere : spheres) {
            sphereData[i] = sphere.pos.x;
            sphereData[i + 1] = sphere.pos.y;
            sphereData[i + 2] = sphere.pos.z;

            sphereData[i + 3] = sphere.radius;

            sphereData[i + 4] = sRGBtoLinear(sphere.material.color.x);
            sphereData[i + 5] = sRGBtoLinear(sphere.material.color.y);
            sphereData[i + 6] = sRGBtoLinear(sphere.material.color.z);

            sphereData[i + 7] = sphere.material.metallic;
            sphereData[i + 8] = sphere.material.roughness;
            sphereData[i + 9] = sphere.material.transmission;
            sphereData[i + 10] = sphere.material.ior;
            sphereData[i + 11] = sphere.material.density;

            Vec3 sphereEmission = sphere.material.linearEmission(); // already linear, intensity multiplied in
            sphereData[i + 12] = sphereEmission.x;
            sphereData[i + 13] = sphereEmission.y;
            sphereData[i + 14] = sphereEmission.z;

            i += 15;
        }

        i = 0;

        for (PointLight pLight : pointLights) {
            pLightData[i] = pLight.pos.x;
            pLightData[i + 1] = pLight.pos.y;
            pLightData[i + 2] = pLight.pos.z;

            pLightData[i + 3] = sRGBtoLinear(pLight.color.x);
            pLightData[i + 4] = sRGBtoLinear(pLight.color.y);
            pLightData[i + 5] = sRGBtoLinear(pLight.color.z);

            pLightData[i + 6] = pLight.intensity;

            i += 7;
        }

        // --- SAFE ALLOCATIONS ---




        // setup
        int normalAllocs = 20;

        CUdeviceptr[] normalDevicePtrs = new CUdeviceptr[normalAllocs];
        CUdeviceptr[] otherDevicePtrs = new CUdeviceptr[1];
        int[] types = new int[normalAllocs];
        int[][] intData = new int[normalAllocs][];
        float[][] floatData = new float[normalAllocs][];
        double[][] doubleData = new double[normalAllocs][];
        int[] normalSizes = new int[normalAllocs];
        int[] otherSizes = new int[1];

        // not normal allocations
        CUdeviceptr allocationBufferPtr = new CUdeviceptr();
        int allocationBufferByteSize = window.WIDTH * window.HEIGHT * 4 * Sizeof.DOUBLE;
        JCudaDriver.cuMemAlloc(allocationBufferPtr, allocationBufferByteSize);
        JCudaDriver.cuMemsetD8(allocationBufferPtr, (byte)0, allocationBufferByteSize);
        otherDevicePtrs[0] = allocationBufferPtr;
        otherSizes[0] = allocationBufferByteSize;

        // normal allocations
        types[0] = 1; // 1 = float
        types[1] = 1;
        types[2] = 1;
        types[3] = 1;
        types[4] = 1;
        types[5] = 0; // 0 = int
        types[6] = 0;
        types[7] = 0;
        types[8] = 1;
        types[9] = 1;
        types[10] = 0;
        types[11] = 0;
        types[12] = 0;
        types[13] = 1;
        types[14] = 1;
        types[15] = 1;
        types[16] = 0;
        types[17] = 0;
        types[18] = 0;
        types[19] = 1;

        // order must match the kernel's parameter order
        floatData[0] = triangleData;
        floatData[1] = sphereData;
        floatData[2] = pLightData;
        floatData[3] = primMinBounds;
        floatData[4] = primMaxBounds;
        intData[5] = primCounts;
        intData[6] = primIdx;
        intData[7] = primitiveIDs;
        floatData[8] = pLightMinBounds;
        floatData[9] = pLightMaxBounds;
        intData[10] = pLightCounts;
        intData[11] = pLightIdx;
        intData[12] = pLightIDs;
        floatData[13] = pLightPower;
        floatData[14] = emissiveMinBounds;
        floatData[15] = emissiveMaxBounds;
        intData[16] = emissiveCounts;
        intData[17] = emissiveIdx;
        intData[18] = emissiveIDs;
        floatData[19] = emissivePower;

        int bvhNodes = BVHNode.nodeCount();
        int lightBvhNodes = PLightBVHNode.nodeCount();
        int emissiveBvhNodes = EmissiveBVHNode.nodeCount();
        int[] normalCounts = new int[normalAllocs];
        normalCounts[0] = triangles.size();
        normalCounts[1] = spheres.size();
        normalCounts[2] = pointLights.size();
        normalCounts[3] = bvhNodes; // minBounds
        normalCounts[4] = bvhNodes; // maxBounds
        normalCounts[5] = bvhNodes; // primCount
        normalCounts[6] = bvhNodes; // idx
        normalCounts[7] = primitiveIDs.length; // primitiveIDs
        normalCounts[8] = lightBvhNodes; // pLightMinBounds
        normalCounts[9] = lightBvhNodes; // pLightMaxBounds
        normalCounts[10] = lightBvhNodes; // pLightCounts
        normalCounts[11] = lightBvhNodes; // pLightIdx
        normalCounts[12] = pLightIDs.length; // pLightIDs
        normalCounts[13] = lightBvhNodes; // pLightPower
        normalCounts[14] = emissiveBvhNodes; // emissiveMinBounds
        normalCounts[15] = emissiveBvhNodes; // emissiveMaxBounds
        normalCounts[16] = emissiveBvhNodes; // emissiveCounts
        normalCounts[17] = emissiveBvhNodes; // emissiveIdx
        normalCounts[18] = emissiveIDs.length; // emissiveIDs
        normalCounts[19] = emissiveBvhNodes; // emissivePower

        for (i = 0; i < normalAllocs; i++) {
            int byteSize = 0;

            ByteBuffer data = ByteBuffer.allocate(0).order(ByteOrder.nativeOrder());

            if (types[i] == 0) { // int
                byteSize = Sizeof.INT * intData[i].length;
                data = ByteBuffer.allocate(byteSize).order(ByteOrder.nativeOrder());
                for (int Int : intData[i]) {
                    data.putInt(Int);
                }
            } else if (types[i] == 1) { // float
                byteSize = Sizeof.FLOAT * floatData[i].length;
                data = ByteBuffer.allocate(byteSize).order(ByteOrder.nativeOrder());
                for (float Flt : floatData[i]) {
                    data.putFloat(Flt);
                }
            } else if (types[i] == 2) { // double
                byteSize = Sizeof.DOUBLE * doubleData[i].length;
                data = ByteBuffer.allocate(byteSize).order(ByteOrder.nativeOrder());
                for (double Dbl : doubleData[i]) {
                    data.putDouble(Dbl);
                }
            }

            data.flip();

            normalDevicePtrs[i] = uploadToDevice(data, byteSize);
            normalSizes[i] = normalCounts[i];
        }

//        CUdeviceptr trianglePtr = new CUdeviceptr();
//        int trianglesByteSize = triangleData.length * Sizeof.FLOAT;
//        if (trianglesByteSize > 0) {
//            JCudaDriver.cuMemAlloc(trianglePtr, trianglesByteSize);
//            JCudaDriver.cuMemcpyHtoD(trianglePtr, Pointer.to(triangleData), trianglesByteSize);
//        }
//
//        CUdeviceptr spherePtr = new CUdeviceptr();
//        int spheresByteSize = sphereData.length * Sizeof.FLOAT;
//        if (spheresByteSize > 0) {
//            JCudaDriver.cuMemAlloc(spherePtr, spheresByteSize);
//            JCudaDriver.cuMemcpyHtoD(spherePtr, Pointer.to(sphereData), spheresByteSize);
//        }
//
//        CUdeviceptr pLightPtr = new CUdeviceptr();
//        int pLightsByteSize = pLightData.length * Sizeof.FLOAT;
//        if (pLightsByteSize > 0) {
//            JCudaDriver.cuMemAlloc(pLightPtr, pLightsByteSize);
//            JCudaDriver.cuMemcpyHtoD(pLightPtr, Pointer.to(pLightData), pLightsByteSize);
//        }
//
//        CUdeviceptr minBoundsPtr = new CUdeviceptr();
//        int minBoundsByteSize = minBounds.length * Sizeof.FLOAT;
//        if (minBoundsByteSize > 0) {
//            JCudaDriver.cuMemAlloc(minBoundsPtr, minBoundsByteSize);
//            JCudaDriver.cuMemcpyHtoD(minBoundsPtr, Pointer.to(minBounds), minBoundsByteSize);
//        }
//
//        CUdeviceptr maxBoundsPtr = new CUdeviceptr();
//        int maxBoundsByteSize = maxBounds.length * Sizeof.FLOAT;
//        if (maxBoundsByteSize > 0) {
//            JCudaDriver.cuMemAlloc(maxBoundsPtr, maxBoundsByteSize);
//            JCudaDriver.cuMemcpyHtoD(maxBoundsPtr, Pointer.to(maxBounds), maxBoundsByteSize);
//        }

        sceneDataDevicePtrs = Stream.concat(Arrays.stream(otherDevicePtrs), Arrays.stream(normalDevicePtrs))
                .toArray(CUdeviceptr[]::new);
        sceneDataSizes = IntStream.concat(Arrays.stream(otherSizes), Arrays.stream(normalSizes))
                .toArray();
    }

    private static CUdeviceptr uploadToDevice(ByteBuffer data, int byteSize) {
        CUdeviceptr devicePtr = new CUdeviceptr();

        if (byteSize > 0) {
            JCudaDriver.cuMemAlloc(devicePtr, byteSize);
            JCudaDriver.cuMemcpyHtoD(devicePtr, Pointer.to(data), byteSize);
        }

        return devicePtr;
    }

    public static float sRGBtoLinear(float sRGB) {
        if (sRGB < 0.04045f) {
            return (sRGB/255f)/12.92f;
        }
        return (float)Math.pow((sRGB/255f+0.055f)/1.055f, 2.4f);
    }

    private void initKernels() {
        kernelManager = new KernelManager();
        kernelManager.addKernel(new SampleKernel(window.WIDTH, window.HEIGHT));
        kernelManager.endKernelSetup();
    }


    public void loop() {

        glfwShowWindow(window.windowHandle);

        int frame = 1;
        long startTime = System.nanoTime();

        try {
            while (!glfwWindowShouldClose(window.windowHandle) && frame <= samples) {
                glfwPollEvents();

//                if (frame % 25 == 0) System.out.println("Frame " + frame);

                glClear(GL_COLOR_BUFFER_BIT); // Clear the canvas

                JCudaDriver.cuGraphicsMapResources(1, new CUgraphicsResource[]{pboResource}, null);

                CUdeviceptr pboDevicePtr = new CUdeviceptr();
                JCudaDriver.cuGraphicsResourceGetMappedPointer(pboDevicePtr, new long[1], pboResource);

                Kernel[] kernels = kernelManager.getKernelsInStage();

                for (Kernel kernel : kernels) {
                    kernel.launch(pboDevicePtr, sceneDataDevicePtrs, sceneDataSizes, frame);
                }

                JCudaDriver.cuCtxSynchronize(); // problem area here 

                JCudaDriver.cuGraphicsUnmapResources(1, new CUgraphicsResource[]{pboResource}, null);

                glBindBuffer(GL_PIXEL_UNPACK_BUFFER, pboId);
                glBindTexture(GL_TEXTURE_2D, textureId);
                glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, window.WIDTH, window.HEIGHT, GL_RGBA, GL_UNSIGNED_BYTE, 0);
                glBindBuffer(GL_PIXEL_UNPACK_BUFFER, 0);

                glEnable(GL_TEXTURE_2D);
                glBegin(GL_QUADS);
                glTexCoord2f(0, 1);
                glVertex2f(-1, -1); // Bottom Left
                glTexCoord2f(1, 1);
                glVertex2f(1, -1);  // Bottom Right
                glTexCoord2f(1, 0);
                glVertex2f(1, 1);   // Top Right
                glTexCoord2f(0, 0);
                glVertex2f(-1, 1);  // Top Left
                glEnd();
                glBindTexture(GL_TEXTURE_2D, 0);

                glfwSwapBuffers(window.windowHandle);
                glfwPollEvents();

                kernelManager.resetRunStage();

                System.out.println("Frame: " + frame + " at " + (System.nanoTime() - startTime) / 1000000 + " ms");
                startTime = System.nanoTime();
                frame++;

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        while (!glfwWindowShouldClose(window.windowHandle)) {
            glfwPollEvents();
        }

        window.cleanup();
    }

}
