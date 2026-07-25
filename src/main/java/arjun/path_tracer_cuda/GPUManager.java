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

//        PresetScenes.loadScene(1, triangles, spheres, pointLights, new Vec3(-2, -2, -5));
        PresetScenes.loadScene(1, triangles, spheres, pointLights, new Vec3(0, 0, 0));

        sendSceneData(triangles.toArray(new Triangle[0]), spheres.toArray(new Sphere[0]), pointLights.toArray(new PointLight[0]));

        initKernels();
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

        float[] triangleData = new float[triangles.length * 16];
        float[] sphereData = new float[spheres.length * 11];
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

            triangleData[i + 9] = sRGBtoLinear(triangle.material.color.x);
            triangleData[i + 10] = sRGBtoLinear(triangle.material.color.y);
            triangleData[i + 11] = sRGBtoLinear(triangle.material.color.z);

            triangleData[i + 12] = triangle.material.metallic;
            triangleData[i + 13] = triangle.material.roughness;
            triangleData[i + 14] = triangle.material.transmission;
            triangleData[i + 15] = triangle.material.ior;

            i += 16;
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

            i += 11;
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

        CUdeviceptr allocationBufferPtr = new CUdeviceptr();
        int allocationBufferByteSize = window.WIDTH * window.HEIGHT * 4 * Sizeof.DOUBLE;
        JCudaDriver.cuMemAlloc(allocationBufferPtr, allocationBufferByteSize);
        JCudaDriver.cuMemsetD8(allocationBufferPtr, (byte)0, allocationBufferByteSize);


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

        sceneDataDevicePtrs = new CUdeviceptr[]{allocationBufferPtr, trianglePtr, spherePtr, pLightPtr};
        sceneDataSizes = new int[]{allocationBufferByteSize, triangles.length, spheres.length, pointLights.length};
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
            while (!glfwWindowShouldClose(window.windowHandle) && frame <= 1024 * 16) {
//                glfwPollEvents();

//                if (frame % 25 == 0) System.out.println("Frame " + frame);
                System.out.println("Frame " + frame);

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

        window.cleanup();
    }

}
