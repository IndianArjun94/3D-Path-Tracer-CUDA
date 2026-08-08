package arjun.path_tracer_cuda.geometry;

public class Triangle { // 17 floats
    public Vec3 v1, v2, v3; // 9 floats
    public Material material; // 8 floats

    public Triangle(Vec3 v1, Vec3 v2, Vec3 v3, Material material) {
        this.v1 = v1;
        this.v2 = v2;
        this.v3 = v3;
        this.material = material;
    }
}
