package arjun.path_tracer_cuda.geometry;

public class Material { // 5 floats
    public Vec3 color; // 3 floats
    public float metallic, roughness; // 2 floats

    public Material(Vec3 color, float metallic, float roughness) {
        this.color = color;
        this.metallic = metallic;
        this.roughness = roughness;
    }
}
