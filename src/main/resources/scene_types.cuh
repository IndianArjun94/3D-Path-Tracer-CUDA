#pragma once

struct Material {
    float3 color;
    float metallic;
    float roughness;
    float transmission;
    float ior;
    float density;
    float3 emission; // light given off: linear colour * intensity, (0, 0, 0) for surfaces that aren't lights
};

struct Sphere {
    float3 pos;
    float radius;
    Material material;
};

struct Triangle {
    float3 v0;
    float3 v1;
    float3 v2;
    Material material;
};

struct PointLight {
    float3 pos;
    float3 color;
    float intensity;
};