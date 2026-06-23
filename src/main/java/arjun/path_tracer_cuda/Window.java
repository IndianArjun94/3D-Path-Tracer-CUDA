package arjun.path_tracer_cuda;

import jcuda.Pointer;
import jcuda.driver.*;
import org.lwjgl.opengl.GL;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import static arjun.path_tracer_cuda.kernel.Kernel.preparePtxFile;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL21.GL_PIXEL_UNPACK_BUFFER;

public class Window {
    public long windowHandle;
    public final int WIDTH, HEIGHT;

    public Window(int width, int height) {
        WIDTH = width;
        HEIGHT = height;

        glfwInit();
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);

        windowHandle = glfwCreateWindow(WIDTH, HEIGHT, "Path Tracer", 0, 0);

        glfwMakeContextCurrent(windowHandle);
        GL.createCapabilities();
    }


    public void cleanup() {
        glfwDestroyWindow(windowHandle);
        glfwTerminate();
    }

    public void stop() {
        glfwSetWindowShouldClose(windowHandle, true);
    }

}
