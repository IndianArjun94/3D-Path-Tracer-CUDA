package arjun.path_tracer_cuda;

import arjun.path_tracer_cuda.geometry.*;
import arjun.path_tracer_cuda.geometry.BVH.AABB;
import arjun.path_tracer_cuda.geometry.BVH.BVHNode;
import arjun.path_tracer_cuda.kernel.Kernel;
import arjun.path_tracer_cuda.kernel.KernelManager;
import arjun.path_tracer_cuda.kernel.SampleKernel;
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

    public int[] primCount; // the ids for each primitive (points to GPUManager objects arraylist), to be shuffled later

    public float[] minBounds;
    public float[] maxBounds;
    public int[] primCounts; // always the prim counts
    public int[] idx; // for leafs: the first prim index. for interiors: index of right child
    //  ---------------------------------------------

    public GPUManager(Window window) {
        this.window = window;
        instance = this;

        // scene init
        initJCuda();
        initBuffers();

        PresetScenes.loadScene(1, triangles, spheres, pointLights, new Vec3(0, 0, 0));

        objects.addAll(triangles);
        objects.addAll(spheres);

        // bvh init
        primCount = new int[GPUManager.instance.objects.size()];

        primBounds = new AABB[GPUManager.instance.objects.size()];
        primCentroids = new Vec3[GPUManager.instance.objects.size()];

        for (int i = 0; i < GPUManager.instance.objects.size(); i++) {
            GPUManager.instance.primBounds[i] = AABB.computeBounds(i);
            GPUManager.instance.primCentroids[i] = AABB.centroidOf(i);
            primCount[i] = i;
        }

        BVHNode root = BVHNode.build(0, primCount.length, 0);
        assert root != null;

        // fill bvh data
        minBounds = new float[GPUManager.instance.objects.size()*3];
        maxBounds = new float[GPUManager.instance.objects.size()*3];
        primCounts = new int[GPUManager.instance.objects.size()];
        idx = new int[GPUManager.instance.objects.size()];

        int flatten = BVHNode.flatten(root);
        assert flatten == 0;

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

        float[] triangleData = new float[triangles.size() * 17];
        float[] sphereData = new float[spheres.size() * 12];
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

            i += 17;
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

            i += 12;
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
        int normalAllocs = 7;

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

        floatData[0] = triangleData;
        floatData[1] = sphereData;
        floatData[2] = pLightData;
        floatData[3] = minBounds;
        floatData[4] = maxBounds;
        intData[5] = primCount;
        intData[6] = idx;

        int bvhNodes = BVHNode.nodeCount();
        int[] normalCounts = new int[normalAllocs];
        normalCounts[0] = triangles.size();
        normalCounts[1] = spheres.size();
        normalCounts[2] = pointLights.size();
        normalCounts[3] = bvhNodes; // minBounds
        normalCounts[4] = bvhNodes; // maxBounds
        normalCounts[5] = bvhNodes; // primCount
        normalCounts[6] = bvhNodes; // idx

        for (i = 0; i < normalAllocs; i++) {
            CUdeviceptr devicePtr = new CUdeviceptr();
            normalDevicePtrs[i] = devicePtr;

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

            if (byteSize > 0) {
                JCudaDriver.cuMemAlloc(normalDevicePtrs[i], byteSize);
                JCudaDriver.cuMemcpyHtoD(normalDevicePtrs[i], Pointer.to(data), byteSize);
                normalSizes[i] = normalCounts[i];
            }
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

        try {
            while (!glfwWindowShouldClose(window.windowHandle) && frame <= samples) {
                glfwPollEvents();

                if (frame % 25 == 0) System.out.println("Frame " + frame);

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
