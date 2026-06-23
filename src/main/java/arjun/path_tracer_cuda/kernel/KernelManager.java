package arjun.path_tracer_cuda.kernel;

import java.util.ArrayList;

public class KernelManager {

    public ArrayList<ArrayList<Kernel>> kernels;
    public int stage = 0;

    public KernelManager() {
        kernels = new ArrayList<>();
        kernels.add(new ArrayList<>());
    }

    public void addKernel(Kernel kernel) {
        kernels.get(stage).add(kernel);
    }

    public void nextAddStage() {
        stage++;
        kernels.add(new ArrayList<>());
    }

    public void endKernelSetup() {
        stage = 0;
    }

    public Kernel[] getKernelsInStage() {
        stage++;
        return kernels.get(stage-1).toArray(new Kernel[0]);
    }

    public void resetRunStage() {
        stage = 0;
    }
}
