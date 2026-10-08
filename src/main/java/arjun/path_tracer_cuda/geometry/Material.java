package arjun.path_tracer_cuda.geometry;

import arjun.path_tracer_cuda.GPUManager;

public class Material { // 11 floats
    public Vec3 color; // 3 floats
    public float metallic, roughness, transmission, ior, density; // 5 floats
    public Vec3 emission = new Vec3(0, 0, 0); // 3 floats - the light this surface gives off, an sRGB colour like color. (0, 0, 0) = not a light
    public float emissionIntensity = 0; // 0 floats - multiplied into emission before it is sent, like a point light's intensity

    public Material(Vec3 color, float metallic, float roughness, float transmission) {
        this.color = color;
        this.metallic = metallic;
        this.roughness = roughness;
        this.transmission = transmission;
        this.ior = 1.5f;
        this.density = 1.0f;
    }

    public Material(Vec3 color, float metallic, float roughness, float transmission, float IOR, float density) {
        this.color = color;
        this.metallic = metallic;
        this.roughness = roughness;
        this.transmission = transmission;
        this.ior = IOR;
        this.density = density;
    }

    // makes this material give off light. returns itself so it chains onto a constructor: new Material(...).emitting(...)
    public Material emitting(Vec3 emission, float intensity) {
        this.emission = emission;
        this.emissionIntensity = intensity;
        return this;
    }

    public boolean isEmissive() {
        return emissionIntensity > 0 && (emission.x > 0 || emission.y > 0 || emission.z > 0);
    }

    // the emission the kernel sees: linear, with the intensity multiplied in
    public Vec3 linearEmission() {
        return new Vec3(
                GPUManager.sRGBtoLinear(emission.x) * emissionIntensity,
                GPUManager.sRGBtoLinear(emission.y) * emissionIntensity,
                GPUManager.sRGBtoLinear(emission.z) * emissionIntensity
        );
    }
}
