package arjun.path_tracer_cuda.geometry;

public class Sphere implements SceneObject { // 12 floats
    public Vec3 pos; // 3 floats
    public float radius; // 1 float
    public Material material; // 8 floats

    public Sphere(Vec3 pos, float radius, Material material) {
        this.pos = pos;
        this.radius = radius;
        this.material = material;
    }

    public int getType() {
        return 1;
    }
}
