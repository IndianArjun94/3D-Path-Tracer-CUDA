package arjun.path_tracer_cuda.geometry;

public class Material { // 7 floats
    public Vec3 color; // 3 floats
    public float metallic, roughness, transmission, ior; // 4 floats

    public Material(Vec3 color, float metallic, float roughness, float transmission) {
        this.color = color;
        this.metallic = metallic;
        this.roughness = roughness;
        this.transmission = transmission;
        this.ior = 1.5f;
    }

    public Material(Vec3 color, float metallic, float roughness, float transmission, float IOR) {
        this.color = color;
        this.metallic = metallic;
        this.roughness = roughness;
        this.transmission = transmission;
        this.ior = IOR;
    }
}
