package arjun.path_tracer_cuda.geometry;

public class Material { // 7 floats
    public Vec3 color; // 3 floats
    public float metallic, roughness, transmission, ior; // 4 floats

    public Material(Vec3 color, float metallic, float roughness) {
        this.color = color;
        this.metallic = metallic;
        this.roughness = roughness;
        this.transmission = 0.0f;
        this.ior = 1.5f;
    }
}
