package arjun.path_tracer_cuda.kernel;

import arjun.path_tracer_cuda.Window;
import jcuda.Pointer;
import jcuda.driver.CUdeviceptr;
import jcuda.driver.CUfunction;
import jcuda.driver.CUmodule;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class Kernel {
    public CUfunction function;
    public CUmodule module;
    public Pointer kernelParams;

    public void launch(CUdeviceptr pbo, CUdeviceptr[] sceneDataDevicePtrs, int[] sceneDataSizes, int frame) {

    }

    public static String preparePtxFile(String resourcePath) {
        try {
            // 1. Find the file in the resources folder
            InputStream in = Window.class.getResourceAsStream(resourcePath);
            if (in == null) {
                throw new RuntimeException("Could not find PTX file in resources: " + resourcePath);
            }

            // 2. Create a temporary file on the OS
            File tempFile = File.createTempFile("kernel_temp", ".ptx");

            // Tells Windows to automatically delete this file when your Java app closes
            tempFile.deleteOnExit();

            // 3. Copy the bytes from the resource folder into the temporary OS file
            java.nio.file.Files.copy(in, tempFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            in.close();

            // 4. Return the standard Windows file path for CUDA to read
            return tempFile.getAbsolutePath();

        } catch (IOException e) {
            throw new RuntimeException("Failed to extract PTX file", e);
        }
    }
}
