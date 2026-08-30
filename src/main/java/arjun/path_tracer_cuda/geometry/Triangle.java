package arjun.path_tracer_cuda.geometry;

public class Triangle implements SceneObject { // 17 floats
    public Vec3 p1, p2, p3; // 9 floats
    public Material material; // 8 floats

    public Triangle(Vec3 p1, Vec3 p2, Vec3 p3, Material material) {
        this.p1 = p1;
        this.p2 = p2;
        this.p3 = p3;
        this.material = material;
    }

    public int getType() {
        return 0;
    }
}
