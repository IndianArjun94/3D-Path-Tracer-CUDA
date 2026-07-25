package arjun.path_tracer_cuda.kernel;

import jcuda.Pointer;
import jcuda.driver.CUdeviceptr;
import jcuda.driver.CUfunction;
import jcuda.driver.CUmodule;
import jcuda.driver.JCudaDriver;


// NOT A "SAMPLE", but a kernel that calculates a sample of the image
public class SampleKernel extends Kernel {
    private int width, height;

    public SampleKernel(int width, int height) {
        this.module = new CUmodule();
        this.function = new CUfunction();
        this.kernelParams = new Pointer();

        this.width = width;
        this.height = height;

        String ptxPath = preparePtxFile("/sample.ptx");
        JCudaDriver.cuModuleLoad(module, ptxPath);
        JCudaDriver.cuModuleGetFunction(this.function, module, "sample");


    }

    public void launch(CUdeviceptr pbo, CUdeviceptr[] sceneDataDevicePtrs, int[] sceneDataSizes, int frame) {
        int blockSizeX = 16;
        int blockSizeY = 16;
        int gridSizeX = (int) Math.ceil((double) width / blockSizeX);
        int gridSizeY = (int) Math.ceil((double) height / blockSizeY);

        kernelParams = Pointer.to(
                Pointer.to(pbo),
                Pointer.to(sceneDataDevicePtrs[0]),
                Pointer.to(new int[]{width}),
                Pointer.to(new int[]{height}),
                Pointer.to(sceneDataDevicePtrs[1]),
                Pointer.to(new int[]{sceneDataSizes[1]}),
                Pointer.to(sceneDataDevicePtrs[2]),
                Pointer.to(new int[]{sceneDataSizes[2]}),
                Pointer.to(sceneDataDevicePtrs[3]),
                Pointer.to(new int[]{sceneDataSizes[3]}),
                Pointer.to(new int[]{frame})
        );

        JCudaDriver.cuLaunchKernel(
                function,
                gridSizeX, gridSizeY, 1,
                blockSizeX, blockSizeY, 1,
                0, null,
                kernelParams, null
        );
    }
}
