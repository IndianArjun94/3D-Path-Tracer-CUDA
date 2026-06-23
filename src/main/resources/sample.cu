#include <cuda_runtime.h>
#include <math.h>
#include <cuda/std/utility>
#include <cfloat> // Required for FLT_MAX

#include "scene_types.cuh"
#include "math.cuh"

__constant__ float epsilon = 0.0001f;
__constant__ float roughness_scalar = 0.4f;

__device__ unsigned int hash(unsigned int seed) {
    unsigned int state = seed * 747796405u + 2891336453u;
    unsigned int word = ((state >> ((state >> 28u) + 4u)) ^ state) * 277803737u;
    return (word >> 22u) ^ word;
}

__device__ cuda::std::pair<float, Triangle> getLowestTravelTriangles(float3 rayPos, float3 rayDir, Triangle* triangles, int numTriangles) {
    float lowest_t = FLT_MAX;
    bool hit = false;
    Triangle hitTriangle;

    for (int i = 0; i < numTriangles; i++) { // triangle intersection
        const Triangle& triangle = triangles[i];

        // calculate triangle ray travel

        float3 edge0Vector = triangle.v1 - triangle.v0;
        float3 edge1Vector = triangle.v2 - triangle.v1;
        float3 edge2Vector = triangle.v0 - triangle.v2;

        float3 normal = cross(edge0Vector, edge1Vector);

        float nD_dot = dot(normal, rayDir);

        if (fabsf(nD_dot) < 0.0001) {
            continue;
        }

        float t = dot(normal*-1, rayPos - triangle.v0) / nD_dot;

        float3 pos = rayDir * t + rayPos;

        float3 edge0InsideNormal = cross(normal, edge0Vector);
        float3 edge1InsideNormal = cross(normal, edge1Vector);
        float3 edge2InsideNormal = cross(normal, edge2Vector);

        int inHalves = 0;

        if (dot(pos-triangle.v0, edge0InsideNormal) > 0) {
            inHalves++;
        } if (dot(pos-triangle.v1, edge1InsideNormal) > 0) {
            inHalves++;
        } if (dot(pos-triangle.v2, edge2InsideNormal) > 0) {
            inHalves++;
        }

        if (inHalves != 3) {
            t = -1;
        }

        if (t > 0 && t < lowest_t) {
            lowest_t = t;
            hit = true;
            hitTriangle = triangle;
        }

    }

    if (hit) {
        return {lowest_t, hitTriangle};
    } else {
        return {-1, {{0, 0, 0}, 0, 0}};
    }
}

__device__ cuda::std::pair<float, Sphere> getLowestTravelSpheres(float3 rayPos, float3 rayDir, Sphere* spheres, int numSpheres) {
    float lowest_t = FLT_MAX;
    bool hit = false;
    Sphere hitSphere;

    for (int i = 0; i < numSpheres; i++) { // sphere intersection
        const Sphere& sphere = spheres[i];

        float3 L = rayPos - sphere.pos;
        float a = dot(rayDir, rayDir);
        float b = 2.0f * dot(rayDir, L);
        float c = dot(L, L) - (sphere.radius * sphere.radius);

//         if (b > 0) {
//             continue;
//         }

        double discriminant = b*b - 4*a*c;

        if (discriminant < 0) {
            continue;
        }

        float solution0 = (-b - sqrtf(discriminant)) / (2*a);
        float solution1 = (-b + sqrtf(discriminant)) / (2*a);

        float t = -1;

        if (solution0 > 0) {
            t = solution0;
        } else if (solution1 > 0) {
            t = solution1;
        }

        if (t > 0 && t < lowest_t) {
            hit = true;
            lowest_t = t;
            hitSphere = sphere;
        }

    }

    if (hit) {
        return {lowest_t, hitSphere};
    } else {
        return {-1, {{0, 0, 0}, 0, 0}};
    }
}

__device__ float3 getLocalColor(float3 rayPos, float3 rayDir, Material material, float3 normal, Triangle* triangles, int numTriangles, Sphere* spheres, int numSpheres, PointLight* pointLights, int numPointLights, int seed) {
    float3 totalLight = make_float3(0.0f, 0.0f, 0.0f);

    for (int i = 0; i < numPointLights; i++) {
        const PointLight& light = pointLights[i];

        seed = hash(seed);
        float rx = ((float)seed / 4294967295.0f) * 2.0f - 1.0f;
        seed = hash(seed);
        float ry = ((float)seed / 4294967295.0f) * 2.0f - 1.0f;
        seed = hash(seed);
        float rz = ((float)seed / 4294967295.0f) * 2.0f - 1.0f;

        float light_radius = 0.35f; // Increase for softer shadows, decrease for sharper
        float3 jitteredLightPos = light.pos + (make_float3(rx, ry, rz) * light_radius);

        float3 lightDir = jitteredLightPos - rayPos;

        float distance = length(lightDir);
        lightDir = normalize(lightDir);

        float3 shadowRayPos = rayPos + (lightDir * epsilon);

        bool isShadowed = false;

        auto smallestTriangleTravel = getLowestTravelTriangles(shadowRayPos, lightDir, triangles, numTriangles);
        auto smallestSphereTravel = getLowestTravelSpheres(shadowRayPos, lightDir, spheres, numSpheres);

        if (smallestTriangleTravel.first > 0 && smallestTriangleTravel.first < distance) {
            isShadowed = true;
        } else if (smallestSphereTravel.first > 0 && smallestSphereTravel.first < distance) {
            isShadowed = true;
        }

        if (!isShadowed) {
            float3 L = lightDir;

            float intensity = std::fmaxf(0, dot(normal, L)) * light.intensity;

            totalLight += light.color * intensity;
        }

    }

    totalLight /= 255;

    float3 ambient = material.color * 0.1;
    float3 finalColor = material.color;

    finalColor *= totalLight;
    finalColor += ambient;
    finalColor = make_float3(std::fminf(255, finalColor.x), std::fminf(255, finalColor.y), std::fminf(255, finalColor.z));

    finalColor *= 1-material.metallic;

    return finalColor;
}

extern "C"
__global__ void sample(uchar4* pbo, int width, int height, Triangle* triangles, int numTriangles, Sphere* spheres, int numSpheres, PointLight* pointLights, int numPointLights, int frame_number) {
    int x = blockIdx.x * blockDim.x + threadIdx.x;
    int y = blockIdx.y * blockDim.y + threadIdx.y;
    if (x >= width || y >= height) return;
    int index = y * width + x;

    unsigned int seed = index ^ hash(frame_number * 719393);

    float3 throughput = make_float3(1.0f, 1.0f, 1.0f);
    float3 accumulated = make_float3(0.0f, 0.0f, 0.0f);

    // create ray

    seed = hash(seed);
    float jitterX = (((float)seed / 4294967295.0f) * 2.0f - 1.0f) / (float)width;

    seed = hash(seed);
    float jitterY = (((float)seed / 4294967295.0f) * 2.0f - 1.0f) / (float)height;

    // 3. Apply the jitter to the ray direction
    float3 rayDir = make_float3(
        ((float)x / width) * 2.0f - 1.0f + jitterX,
        1.0f - ((float)y / height) * 2.0f + jitterY,
        -1.0f);

    rayDir.x *= ((float)width / (float)height);

    rayDir = normalize(rayDir);

    float3 rayPos = make_float3(0.0f, 0.0f, 0.0f);

    // check if it hits any object

    for (int bounces = 0; bounces < 10; bounces++) {
        float lowest_t = FLT_MAX;
        bool hit = false;
        Triangle hitTriangle;
        Sphere hitSphere;
        int type;

        auto lowestTriangleTravel = getLowestTravelTriangles(rayPos, rayDir, triangles, numTriangles);
        auto lowestSphereTravel = getLowestTravelSpheres(rayPos, rayDir, spheres, numSpheres);

        if (lowestTriangleTravel.first > 0) {
            lowest_t = lowestTriangleTravel.first;
            hitTriangle = lowestTriangleTravel.second;
            hit = true;
            type = 0;
        } if (lowestSphereTravel.first > 0 && lowestSphereTravel.first < lowest_t) {
            lowest_t = lowestSphereTravel.first;
            hitSphere = lowestSphereTravel.second;
            hit = true;
            type = 1;
        }

        if (!hit) {
            break;
        }

        float3 normal;
        Material material;

        rayPos = rayDir * lowest_t + rayPos;

        if (type == 0) {
            float3 edge0Vector = hitTriangle.v1 - hitTriangle.v0;
            float3 edge1Vector = hitTriangle.v2 - hitTriangle.v1;

            normal = normalize(cross(edge0Vector, edge1Vector));
            material = hitTriangle.material;
        } else if (type == 1) {
            normal = normalize(rayPos - hitSphere.pos);
            material = hitSphere.material;
        }

        float3 localColor = getLocalColor(rayPos, rayDir, material, normal, triangles, numTriangles, spheres, numSpheres, pointLights, numPointLights, seed);

        localColor *= throughput;
        accumulated += localColor;

        float defaultDielectric = 255*0.04*1 - material.metallic;

        float3 metallic = material.color * material.metallic;

        float3 combined = (metallic + defaultDielectric) / 255;

        throughput *= combined;

        rayPos += normal * epsilon;

        float normalDot = dot(rayDir, normal);

        rayDir = normalize(rayDir - normal*2*normalDot);

        if (material.roughness != 0) {

            seed = hash(seed);
            float randomFloatX = ((float)seed / 4294967295.0f) * 2.0f - 1.0f;
            seed = hash(seed);
            float randomFloatY = ((float)seed / 4294967295.0f) * 2.0f - 1.0f;
            seed = hash(seed);
            float randomFloatZ = ((float)seed / 4294967295.0f) * 2.0f - 1.0f;

            float3 random = make_float3(randomFloatX, randomFloatY, randomFloatZ);
            random *= material.roughness * roughness_scalar;

            rayDir = normalize(normalize(rayDir) + random);

        }

    }

    // 1. Read the old color currently stored in the PBO
    float old_r = (float)pbo[index].x;
    float old_g = (float)pbo[index].y;
    float old_b = (float)pbo[index].z;

    // 2. Clamp your NEW incoming color to ensure it's safe
    float new_r = std::fminf(255.0f, std::fmaxf(0.0f, accumulated.x));
    float new_g = std::fminf(255.0f, std::fmaxf(0.0f, accumulated.y));
    float new_b = std::fminf(255.0f, std::fmaxf(0.0f, accumulated.z));

    // 3. Calculate the progressive average
    // Formula: NewAvg = (OldAvg * (frames - 1) + NewColor) / frames
    float final_r = ((old_r * (frame_number - 1)) + new_r) / frame_number;
    float final_g = ((old_g * (frame_number - 1)) + new_g) / frame_number;
    float final_b = ((old_b * (frame_number - 1)) + new_b) / frame_number;

    // 4. Write the new average back to the PBO as 8-bit integers
    pbo[index].w = 255;
    pbo[index].x = (unsigned char)final_r;
    pbo[index].y = (unsigned char)final_g;
    pbo[index].z = (unsigned char)final_b;
}