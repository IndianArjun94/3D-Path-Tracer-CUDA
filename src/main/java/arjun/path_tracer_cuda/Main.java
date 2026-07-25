package arjun.path_tracer_cuda;

public class Main {
    public static void main(String[] args) {
        Window window = new Window(1920, 1080);
        GPUManager gpuManager = new GPUManager(window);
        gpuManager.loop();
    }
}
