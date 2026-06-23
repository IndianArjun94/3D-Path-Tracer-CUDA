package arjun.path_tracer_cuda;

import arjun.path_tracer_cuda.geometry.*;
import arjun.path_tracer_cuda.kernel.Kernel;
import arjun.path_tracer_cuda.kernel.KernelManager;
import arjun.path_tracer_cuda.kernel.SampleKernel;
import jcuda.Pointer;
import jcuda.Sizeof;
import jcuda.driver.*;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL15.GL_DYNAMIC_DRAW;
import static org.lwjgl.opengl.GL21.GL_PIXEL_UNPACK_BUFFER;

public class GPUManager {

    private Window window;

    public int textureId;
    public int pboId;

    public CUdevice device;
    public CUcontext context;

    public CUgraphicsResource pboResource = new CUgraphicsResource();

    public KernelManager kernelManager;


    //  Scene stuff ---------------------------------
    public ArrayList<Triangle> triangles = new ArrayList<>();
    public ArrayList<Sphere> spheres = new ArrayList<>();
    public ArrayList<PointLight> pointLights = new ArrayList<>();

    public CUdeviceptr[] sceneDataDevicePtrs;
    public int[] sceneDataSizes;

//  ---------------------------------------------

    public GPUManager(Window window) {
        this.window = window;

        initJCuda();
        initBuffers();

        Material matFractal = new Material(new Vec3(50, 100, 255), 0.5f, 0.5f); // Metallic Blue

        Vec3 top = new Vec3(0.0f, 5.0f, -12.0f);
        Vec3 botLeft = new Vec3(-5.657f, -2.0f, -12.0f);
        Vec3 botRight = new Vec3(0.0f, -2.0f, -6.343f);
        Vec3 botBack = new Vec3(2.828f, -2.0f, -14.828f);

        generateSierpinski(top, botLeft, botRight, botBack, 5, matFractal, triangles);

        pointLights.add(new PointLight(new Vec3(0.0f, 12.0f, -4.0f), new Vec3(255, 255, 255), 2.5f));

        // ==========================================
// 1. MASSIVE ROOM PLANES (100x100x50)
// ==========================================

// Floor (Y = -3.0)
        Material matFloor = new Material(new Vec3(220, 220, 220), 0.5f, 0.1f);
        triangles.add(new Triangle(new Vec3(-50.0f, -3.0f, 50.0f), new Vec3(50.0f, -3.0f, 50.0f), new Vec3(50.0f, -3.0f, -50.0f), matFloor));
        triangles.add(new Triangle(new Vec3(-50.0f, -3.0f, 50.0f), new Vec3(50.0f, -3.0f, -50.0f), new Vec3(-50.0f, -3.0f, -50.0f), matFloor));

// Back Wall (Z = -50.0)
        Material matBackWall = new Material(new Vec3(160, 160, 160), 0.3f, 0.1f);
        triangles.add(new Triangle(new Vec3(50.0f, -3.0f, -50.0f), new Vec3(50.0f, 50.0f, -50.0f), new Vec3(-50.0f, 50.0f, -50.0f), matBackWall));
        triangles.add(new Triangle(new Vec3(50.0f, -3.0f, -50.0f), new Vec3(-50.0f, 50.0f, -50.0f), new Vec3(-50.0f, -3.0f, -50.0f), matBackWall));

// Front Wall - Behind the Camera (Z = 50.0)
// This encloses the room so light bounces back onto the front of the fractal!
        triangles.add(new Triangle(new Vec3(-50.0f, -3.0f, 50.0f), new Vec3(-50.0f, 50.0f, 50.0f), new Vec3(50.0f, 50.0f, 50.0f), matBackWall));
        triangles.add(new Triangle(new Vec3(-50.0f, -3.0f, 50.0f), new Vec3(50.0f, 50.0f, 50.0f), new Vec3(50.0f, -3.0f, 50.0f), matBackWall));

// Left Wall - Red (X = -50.0)
        Material matLeftWall = new Material(new Vec3(255, 65, 65), 0.7f, 0.1f);
        triangles.add(new Triangle(new Vec3(-50.0f, -3.0f, -50.0f), new Vec3(-50.0f, 50.0f, -50.0f), new Vec3(-50.0f, 50.0f, 50.0f), matLeftWall));
        triangles.add(new Triangle(new Vec3(-50.0f, -3.0f, -50.0f), new Vec3(-50.0f, 50.0f, 50.0f), new Vec3(-50.0f, -3.0f, 50.0f), matLeftWall));

// Right Wall - Green (X = 50.0)
        Material matRightWall = new Material(new Vec3(65, 255, 65), 0.7f, 0.1f);
        triangles.add(new Triangle(new Vec3(50.0f, -3.0f, 50.0f), new Vec3(50.0f, 50.0f, 50.0f), new Vec3(50.0f, 50.0f, -50.0f), matRightWall));
        triangles.add(new Triangle(new Vec3(50.0f, -3.0f, 50.0f), new Vec3(50.0f, 50.0f, -50.0f), new Vec3(50.0f, -3.0f, -50.0f), matRightWall));

// Ceiling (Y = 50.0)
        Material matCeiling = new Material(new Vec3(100, 100, 100), 0.7f, 0.1f);
        triangles.add(new Triangle(new Vec3(-50.0f, 50.0f, -50.0f), new Vec3(50.0f, 50.0f, -50.0f), new Vec3(50.0f, 50.0f, 50.0f), matCeiling));
        triangles.add(new Triangle(new Vec3(-50.0f, 50.0f, -50.0f), new Vec3(50.0f, 50.0f, 50.0f), new Vec3(-50.0f, 50.0f, 50.0f), matCeiling));

        sendSceneData(triangles.toArray(new Triangle[0]), spheres.toArray(new Sphere[0]), pointLights.toArray(new PointLight[0]));

        initKernels();
    }

    // Recursive function to generate a fractal
    public void generateSierpinski(Vec3 v0, Vec3 v1, Vec3 v2, Vec3 v3, int depth, Material mat, List<Triangle> triangles) {
        if (depth == 0) {
            // Base case: build the 4 sides of the tetrahedron
            triangles.add(new Triangle(v0, v1, v2, mat)); // Front face
            triangles.add(new Triangle(v0, v2, v3, mat)); // Right face
            triangles.add(new Triangle(v0, v3, v1, mat)); // Left face
            triangles.add(new Triangle(v1, v3, v2, mat)); // Bottom face
            return;
        }

        // Calculate midpoints of every edge
        Vec3 m01 = new Vec3((v0.x + v1.x) / 2, (v0.y + v1.y) / 2, (v0.z + v1.z) / 2);
        Vec3 m02 = new Vec3((v0.x + v2.x) / 2, (v0.y + v2.y) / 2, (v0.z + v2.z) / 2);
        Vec3 m03 = new Vec3((v0.x + v3.x) / 2, (v0.y + v3.y) / 2, (v0.z + v3.z) / 2);
        Vec3 m12 = new Vec3((v1.x + v2.x) / 2, (v1.y + v2.y) / 2, (v1.z + v2.z) / 2);
        Vec3 m13 = new Vec3((v1.x + v3.x) / 2, (v1.y + v3.y) / 2, (v1.z + v3.z) / 2);
        Vec3 m23 = new Vec3((v2.x + v3.x) / 2, (v2.y + v3.y) / 2, (v2.z + v3.z) / 2);

        // Recurse into the 4 smaller corner tetrahedrons
        generateSierpinski(v0, m01, m02, m03, depth - 1, mat, triangles);
        generateSierpinski(m01, v1, m12, m13, depth - 1, mat, triangles);
        generateSierpinski(m02, m12, v2, m23, depth - 1, mat, triangles);
        generateSierpinski(m03, m13, m23, v3, depth - 1, mat, triangles);
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

    private void sendSceneData(Triangle[] triangles, Sphere[] spheres, PointLight[] pointLights) {

        float[] triangleData = new float[triangles.length * 14];
        float[] sphereData = new float[spheres.length * 9];
        float[] pLightData = new float[pointLights.length * 7];

        int i = 0;

        for (Triangle triangle : triangles) {
            triangleData[i] = triangle.v1.x;
            triangleData[i + 1] = triangle.v1.y;
            triangleData[i + 2] = triangle.v1.z;

            triangleData[i + 3] = triangle.v2.x;
            triangleData[i + 4] = triangle.v2.y;
            triangleData[i + 5] = triangle.v2.z;

            triangleData[i + 6] = triangle.v3.x;
            triangleData[i + 7] = triangle.v3.y;
            triangleData[i + 8] = triangle.v3.z;

            triangleData[i + 9] = triangle.material.color.x;
            triangleData[i + 10] = triangle.material.color.y;
            triangleData[i + 11] = triangle.material.color.z;

            triangleData[i + 12] = triangle.material.metallic;
            triangleData[i + 13] = triangle.material.roughness;

            i += 14;
        }

        i = 0;

        for (Sphere sphere : spheres) {
            sphereData[i] = sphere.pos.x;
            sphereData[i + 1] = sphere.pos.y;
            sphereData[i + 2] = sphere.pos.z;

            sphereData[i + 3] = sphere.radius;

            sphereData[i + 4] = sphere.material.color.x;
            sphereData[i + 5] = sphere.material.color.y;
            sphereData[i + 6] = sphere.material.color.z;

            sphereData[i + 7] = sphere.material.metallic;
            sphereData[i + 8] = sphere.material.roughness;

            i += 9;
        }

        i = 0;

        for (PointLight pLight : pointLights) {
            pLightData[i] = pLight.pos.x;
            pLightData[i + 1] = pLight.pos.y;
            pLightData[i + 2] = pLight.pos.z;

            pLightData[i + 3] = pLight.color.x;
            pLightData[i + 4] = pLight.color.y;
            pLightData[i + 5] = pLight.color.z;

            pLightData[i + 6] = pLight.intensity;

            i += 7;
        }

        // --- SAFE ALLOCATIONS ---

        CUdeviceptr trianglePtr = new CUdeviceptr();
        int trianglesByteSize = triangleData.length * Sizeof.FLOAT;
        if (trianglesByteSize > 0) {
            JCudaDriver.cuMemAlloc(trianglePtr, trianglesByteSize);
            JCudaDriver.cuMemcpyHtoD(trianglePtr, Pointer.to(triangleData), trianglesByteSize);
        }

        CUdeviceptr spherePtr = new CUdeviceptr();
        int spheresByteSize = sphereData.length * Sizeof.FLOAT;
        if (spheresByteSize > 0) {
            JCudaDriver.cuMemAlloc(spherePtr, spheresByteSize);
            JCudaDriver.cuMemcpyHtoD(spherePtr, Pointer.to(sphereData), spheresByteSize);
        }

        CUdeviceptr pLightPtr = new CUdeviceptr();
        int pLightsByteSize = pLightData.length * Sizeof.FLOAT;
        if (pLightsByteSize > 0) {
            JCudaDriver.cuMemAlloc(pLightPtr, pLightsByteSize);
            JCudaDriver.cuMemcpyHtoD(pLightPtr, Pointer.to(pLightData), pLightsByteSize);
        }

        sceneDataDevicePtrs = new CUdeviceptr[]{trianglePtr, spherePtr, pLightPtr};
        sceneDataSizes = new int[]{triangles.length, spheres.length, pointLights.length};
    }

    private void initKernels() {
        kernelManager = new KernelManager();
        kernelManager.addKernel(new SampleKernel(window.WIDTH, window.HEIGHT));
        kernelManager.endKernelSetup();
    }


    public void loop() {

        glfwShowWindow(window.windowHandle);

        int frame = 1;

//        while (!glfwWindowShouldClose(window.windowHandle)) {
        for (int i = 0; i < 1024 * 16; i++) {

            System.out.println(frame);

            glClear(GL_COLOR_BUFFER_BIT); // Clear the canvas

            JCudaDriver.cuGraphicsMapResources(1, new CUgraphicsResource[]{pboResource}, null);

            CUdeviceptr pboDevicePtr = new CUdeviceptr();
            JCudaDriver.cuGraphicsResourceGetMappedPointer(pboDevicePtr, new long[1], pboResource);

            Kernel[] kernels = kernelManager.getKernelsInStage();

            for (Kernel kernel : kernels) {
                kernel.launch(pboDevicePtr, sceneDataDevicePtrs, sceneDataSizes, frame);
            }

            JCudaDriver.cuCtxSynchronize();

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

        while (!glfwWindowShouldClose(window.windowHandle)) {
            glfwPollEvents();

        }


        window.cleanup();
    }

}
