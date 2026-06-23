package arjun.path_tracer_cuda.geometry;

public class PointLight { // 7 floats
    public Vec3 pos; // 3 floats
    public Vec3 color; // 3 floats
    public float intensity; // 1 float

    public PointLight(Vec3 pos, Vec3 color, float intensity) {
        this.pos = pos;
        this.color = color;
        this.intensity = intensity;
    }
}
