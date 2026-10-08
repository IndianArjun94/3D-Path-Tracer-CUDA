#include <cuda_runtime.h>
#include <math.h>
#include <cuda/std/utility>
#include <cfloat> // Required for FLT_MAX
#include <cuda/std/tuple>

#include "scene_types.cuh"
#include "math.cuh"

__constant__ float epsilon = 0.0001f;
__constant__ float roughness_scalar = 0.4f;
__constant__ float e = 2.7182f;

// Firefly clamp: the most light (brightest of r, g, b, linear, before tone mapping) that one
// bounced sample may add to a pixel. Rare paths that bounce into a tiny, fiercely lit surface
// (a spotlight bulb, the inside of its can) otherwise add thousands of times a pixel's normal
// value in one go, and take thousands of frames to average away. What matters is the cap
// relative to the darkest pixels: in a dark room (~0.01) a cap of 10 is still ~1000x and a speck
// lingers for hundreds of frames; 2 fades in a few dozen, while ordinary bounced light here stays
// well under it. Lower = fewer specks but more lost light; raise it if caustics or reflections of
// bright things look dulled, or for brightly lit scenes where there's more headroom.
__constant__ float indirectClamp = 2.0f;

__device__ unsigned int hash(unsigned int seed) {
    unsigned int state = seed * 747796405u + 2891336453u;
    unsigned int word = ((state >> ((state >> 28u) + 4u)) ^ state) * 277803737u;
    return (word >> 22u) ^ word;
}

__device__ float randomFloat(unsigned int& seed) {
    seed = hash(seed);
    return fmaxf(0.001, fminf(0.999, (float)seed / 4294967295.0f));
}


__device__ float hitTriangle(float3 rayPos, float3 rayDir, Triangle& triangle, float tMin, float tMax) {
    float3 edge0Vector = triangle.v1 - triangle.v0;
    float3 edge1Vector = triangle.v2 - triangle.v1;
    float3 edge2Vector = triangle.v0 - triangle.v2;

    float3 normal = cross(edge0Vector, edge1Vector);

    float nD_dot = dot(normal, rayDir);

    if (fabsf(nD_dot) < 0.0001) {
        return -1;
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

    if (t > tMin && t < tMax) {
        return t;
    } else {
        return -1;
    }
}

__device__ float hitSphere(float3 rayPos, float3 rayDir, Sphere& sphere, float tMin, float tMax) {
    float3 L = rayPos - sphere.pos;
    float a = dot(rayDir, rayDir);
    float b = 2.0f * dot(rayDir, L);
    float c = dot(L, L) - (sphere.radius * sphere.radius);

    float discriminant = b*b - 4*a*c;

    if (discriminant < 0) {
        return -1;
    }

    float solution0 = (-b - sqrtf(discriminant)) / (2*a);
    float solution1 = (-b + sqrtf(discriminant)) / (2*a);

    float t = -1;

    if (solution0 > 0) {
        t = solution0;
    } else if (solution1 > 0) {
        t = solution1;
    }

    if (t > tMin && t < tMax) {
        return t;
    } else {
        return -1;
    }
}

__device__ bool intersectAny(float3 rayPos, float3 rayDir, Triangle* triangles, int numTriangles, Sphere* spheres, int numSpheres, float tMin, float tMax) {
    for (int i = 0; i < numTriangles; i++) { // triangle intersection

        if (hitTriangle(rayPos, rayDir, triangles[i], tMin, tMax) >= 0) {
            return true;
        }
    }

    for (int i = 0; i < numSpheres; i++) { // sphere intersection
        if (hitSphere(rayPos, rayDir, spheres[i], tMin, tMax) >= 0) {
            return true;
        }
    }

    return false;

}


__device__ float hitAABB(float3 rayPos, float3 invDir,
                         float3 boxMin, float3 boxMax,
                         float tMin, float tMax) {

    float t_xMin = (boxMin.x - rayPos.x) * invDir.x;
    float t_xMax = (boxMax.x - rayPos.x) * invDir.x;
    float t_yMin = (boxMin.y - rayPos.y) * invDir.y;
    float t_yMax = (boxMax.y - rayPos.y) * invDir.y;
    float t_zMin = (boxMin.z - rayPos.z) * invDir.z;
    float t_zMax = (boxMax.z - rayPos.z) * invDir.z;

    float t_xEnter = fminf(t_xMin, t_xMax);
    float t_xExit  = fmaxf(t_xMin, t_xMax);
    float t_yEnter = fminf(t_yMin, t_yMax);
    float t_yExit  = fmaxf(t_yMin, t_yMax);
    float t_zEnter = fminf(t_zMin, t_zMax);
    float t_zExit  = fmaxf(t_zMin, t_zMax);

    float tEnter = fmaxf(tMin, fmaxf(t_xEnter, fmaxf(t_yEnter, t_zEnter)));
    float tExit  = fminf(tMax, fminf(t_xExit,  fminf(t_yExit,  t_zExit)));

    return (tExit >= tEnter) ? tEnter : -1.0f;
}

__device__ cuda::std::tuple<float, int, int> getBestPrimitive(float3 rayPos, float3 rayDir, float3 invDir,
                                                              Triangle* triangles, int numTriangles,
                                                              Sphere* spheres, int numSpheres,
                                                              float* nodeMin, float* nodeMax,
                                                              int* nodePrimCount, int* nodeIdx, int* primIDs,
                                                              float tMin, float tMax) {
    int node = 0;
    int stack[128];
    int stackPtr = 0;

    float best_t = tMax;
    int best_i = -1;
    auto type = 0;

    while (true) {
        if (nodePrimCount[node] == 0) { // interior node

            int leftChildIndex = node + 1;
            int rightChildIndex = nodeIdx[node]; // for interiors, this is the index of the right child;

            float3 leftAABBMin = make_float3(nodeMin[leftChildIndex*3],
                                              nodeMin[leftChildIndex*3+1],
                                              nodeMin[leftChildIndex*3+2]);

            float3 leftAABBMax = make_float3(nodeMax[leftChildIndex*3],
                                              nodeMax[leftChildIndex*3+1],
                                              nodeMax[leftChildIndex*3+2]);

            float3 rightAABBMin = make_float3(nodeMin[rightChildIndex*3],
                                              nodeMin[rightChildIndex*3+1],
                                              nodeMin[rightChildIndex*3+2]);

            float3 rightAABBMax = make_float3(nodeMax[rightChildIndex*3],
                                              nodeMax[rightChildIndex*3+1],
                                              nodeMax[rightChildIndex*3+2]);


            // check if ray hits left child

            float leftAABBTravel = hitAABB(rayPos, invDir, leftAABBMin, leftAABBMax, tMin, best_t);
            float rightAABBTravel = hitAABB(rayPos, invDir, rightAABBMin, rightAABBMax, tMin, best_t);

            bool hitL = leftAABBTravel  >= 0.0f;
            bool hitR = rightAABBTravel >= 0.0f;

            if (hitL && hitR) { // hit both
                node = leftChildIndex;
                stack[stackPtr++] = rightChildIndex;
            } else if (hitL) { // hit left
                node = leftChildIndex;
            } else if (hitR) { // hit right
                node = rightChildIndex;
            } else { // hit neither
                if (stackPtr == 0) break;
                node = stack[--stackPtr];
            }

        } else { // leaf node
            int childrenID = nodeIdx[node];

            for (int id = childrenID; id < childrenID + nodePrimCount[node]; id++) {
                float t = (primIDs[id] < numTriangles)
                    ? hitTriangle(rayPos, rayDir, triangles[primIDs[id]], tMin, best_t)
                    : hitSphere(rayPos, rayDir, spheres[primIDs[id] - numTriangles], tMin, best_t);

                if (t > 0) {
                    best_t = t; // best travel
                    type = (primIDs[id] < numTriangles) ? 0 : 1;
                    best_i = (type == 0) ? primIDs[id] : primIDs[id] - numTriangles; // index into triangles[] or spheres[]
                }
            }

            if (stackPtr > 0) {
                stackPtr--;
                node = stack[stackPtr];
            } else {break;}
        }

    }

    if (best_i < 0) {
        return cuda::std::make_tuple(-1.0f, 0, 0);
    }

    return cuda::std::make_tuple(best_t, best_i, type);
}


// Compute the local shading contribution (lambertian DIFFUSE) for the hit point.
// Inputs are typically position, normal, material, and light parameters; output is RGB.
// how far a light's position is jittered for soft shadows - the light tree has to allow for it too
#define LIGHT_JITTER 0.35f

/* How much light node i of the light BVH could deliver to point p with normal n - the score the
 * tree walk picks children by. Total power over squared distance, times the most favourable
 * cosine any light in the node could have with the normal: the node's box is treated as a sphere
 * (plus the jitter), and the cosine is taken to the near edge of that sphere rather than its
 * centre. That makes the score
 *   - never negative, so the branch probabilities always lie in 0..1
 *   - zero only when every light in the node is behind the surface, so dropping it is exact
 *   - smooth as the normal turns, so neighbouring facets of a flat-shaded curve pick lights the
 *     same way instead of flipping between branches facet by facet */
__device__ float lightNodeImportance(int i, float3 p, float3 n, const float* nodeMin, const float* nodeMax,
                                     const float* nodePower, float jitter) {
    float3 mn = make_float3(nodeMin[i*3], nodeMin[i*3+1], nodeMin[i*3+2]);
    float3 mx = make_float3(nodeMax[i*3], nodeMax[i*3+1], nodeMax[i*3+2]);
    float3 toCentre = (mn + mx) * 0.5f - p;
    float r = length((mx - mn) * 0.5f) + jitter; // bounding sphere of the box, plus the jitter
    float d2 = dot(toCentre, toCentre);
    float power = nodePower[i*3] + nodePower[i*3+1] + nodePower[i*3+2]; // 3 floats (r, g, b) per node

    float cosBound = 1.0f; // inside the sphere, a light could be in any direction
    if (d2 > r * r) {
        float d = sqrtf(d2);
        float cosT = dot(n, toCentre) / d;                       // normal to the sphere's centre
        float sinT = sqrtf(fmaxf(0.0f, 1.0f - cosT * cosT));
        float sinB = r / d, cosB = sqrtf(fmaxf(0.0f, 1.0f - sinB * sinB)); // the sphere's angular radius
        cosBound = cosT >= cosB ? 1.0f : cosT * cosB + sinT * sinB; // cos(angle to the centre - angular radius)
    }

    return power * fmaxf(cosBound, 0.0f) / fmaxf(d2, r * r);
}

__device__ float3 getLocalColor(float3 rayPos, float3 rayDir, Material material, float3 normal, Triangle* triangles, int numTriangles, Sphere* spheres, int numSpheres, PointLight* pointLights, int numPointLights, unsigned int& seed, float epsilon, float3 invDir, float* nodeMin, float* nodeMax, int* nodePrimCount, int* nodeIdx, int* primIDs, float* lightNodeMin, float* lightNodeMax, int* lightNodePrimCount, int* lightNodeIdx, int* lightIDs, float* lightNodePower, float* emissiveNodeMin, float* emissiveNodeMax, int* emissiveNodePrimCount, int* emissiveNodeIdx, int* emissiveIDs, float* emissiveNodePower, int numEmissives, float tMin, float tMax) {
    float3 totalLight = make_float3(0.0f, 0.0f, 0.0f);

    int node = 0;
    float pathProb = 1.0f;

    while (true) {
        if (lightNodePrimCount[node] == 0) { // interior node
            int leftChildIndex = node + 1;
            int rightChildIndex = lightNodeIdx[node]; // for interiors, this is the index of the right child;

            // find light probabilities
            float probLeft = lightNodeImportance(leftChildIndex, rayPos, normal, lightNodeMin, lightNodeMax, lightNodePower, LIGHT_JITTER);
            float probRight = lightNodeImportance(rightChildIndex, rayPos, normal, lightNodeMin, lightNodeMax, lightNodePower, LIGHT_JITTER);

            if (probLeft + probRight <= 0) { // nothing under this node can light the point (scores are never negative)
                break;
            }

            float p = probRight / (probLeft + probRight); // always within 0..1

            if (randomFloat(seed) < p) {
                // right child
                node = rightChildIndex;
                pathProb *= p;
            } else {
                // left child
                node = leftChildIndex;
                pathProb *= 1 - p;
            }

        } else { // leaf node
            const PointLight& light = pointLights[lightIDs[lightNodeIdx[node]]];

            seed = hash(seed);
            float rx = randomFloat(seed) * 2.0f - 1.0f;
            seed = hash(seed);
            float ry = randomFloat(seed) * 2.0f - 1.0f;
            seed = hash(seed);
            float rz = randomFloat(seed) * 2.0f - 1.0f;

            float light_radius = LIGHT_JITTER; // Increase for softer shadows, decrease for sharper (it's defined above the light tree's scoring, which allows for it)
            float3 jitteredLightPos = light.pos + (make_float3(rx, ry, rz) * light_radius);

            float3 lightDir = jitteredLightPos - rayPos;

            float distance = length(lightDir);
            lightDir = normalize(lightDir);

            float3 shadowRayPos = rayPos + (normal * epsilon);

            bool isShadowed = false;

            //        if (intersectAny(shadowRayPos, lightDir, triangles, numTriangles, spheres, numSpheres, epsilon, distance)) isShadowed = true;
            //        isShadowed = false; // the hardcode I was talking about Claude

            isShadowed = cuda::std::get<0>(
                getBestPrimitive(shadowRayPos, lightDir, make_float3(1.0f/lightDir.x, 1.0f/lightDir.y, 1.0f/lightDir.z),
                triangles, numTriangles, spheres, numSpheres,
                nodeMin, nodeMax, nodePrimCount,
                nodeIdx, primIDs, tMin, distance)) != -1;

            distance = fmaxf(distance, light_radius);

            if (!isShadowed) {
                float intensity = fmaxf(0, dot(normal, lightDir)) * light.intensity;
                totalLight += (light.color * intensity) / (distance * distance) / pathProb;
            }

            break;
        }
    }

    // old Pre-BVH code

//    for (int i = 0; i < numPointLights; i++) {
//        const PointLight& light = pointLights[i];
//
//        seed = hash(seed);
//        float rx = randomFloat(seed) * 2.0f - 1.0f;
//        seed = hash(seed);
//        float ry = randomFloat(seed) * 2.0f - 1.0f;
//        seed = hash(seed);
//        float rz = randomFloat(seed) * 2.0f - 1.0f;
//
//        float light_radius = 0.35f; // Increase for softer shadows, decrease for sharper
//        float3 jitteredLightPos = light.pos + (make_float3(rx, ry, rz) * light_radius);
//
//        float3 lightDir = jitteredLightPos - rayPos;
//
//        float distance = length(lightDir);
//        lightDir = normalize(lightDir);
//
//        float3 shadowRayPos = rayPos + (normal * epsilon);
//
//        bool isShadowed = false;
//
////        if (intersectAny(shadowRayPos, lightDir, triangles, numTriangles, spheres, numSpheres, epsilon, distance)) isShadowed = true;
////        isShadowed = false; // the hardcode I was talking about Claude
//
//        isShadowed = cuda::std::get<0>(
//            getBestPrimitive(shadowRayPos, lightDir, make_float3(1.0f/lightDir.x, 1.0f/lightDir.y, 1.0f/lightDir.z),
//            triangles, numTriangles, spheres, numSpheres,
//            nodeMin, nodeMax, nodePrimCount,
//            nodeIdx, primIDs, tMin, tMax)) != -1;
//
//        distance = fmaxf(distance, light_radius);
//
//        if (!isShadowed) {
//            float3 L = lightDir;
//            float intensity = fmaxf(0, dot(normal, L)) * light.intensity;
//            totalLight += (light.color * intensity) / (distance * distance);
//        }
//
//    }

    float3 finalColor = material.color / 3.14159265f;
    finalColor *= totalLight;
    finalColor *= 1-material.metallic;

    return finalColor;
}

// scales c down so its brightest channel is at most maxValue: the hue stays, the excess energy is dropped
__device__ float3 clampBrightness(float3 c, float maxValue) {
    float peak = fmaxf(c.x, fmaxf(c.y, c.z));
    return peak > maxValue ? c * (maxValue / peak) : c;
}

__device__ float3 toneMap(float3 c) {
    const float w  = 4.0f;        // the linear value that becomes pure white
    const float w2 = w * w;
    c.x = c.x * (1.0f + c.x / w2) / (1.0f + c.x);
    c.y = c.y * (1.0f + c.y / w2) / (1.0f + c.y);
    c.z = c.z * (1.0f + c.z / w2) / (1.0f + c.z);
    return c;
}

__device__ int linearToSRGB(float linear) {
//    linear = __fclamp_rn(linear, 0.0f, 1.0f);

    // 2. Apply the official two-part sRGB curve
    float srgb;
    if (linear <= 0.0031308f) {
        srgb = linear * 12.92f;
    } else {
        srgb = 1.055f * __powf(linear, 1.0f / 2.4f) - 0.055f;
    }

    // 3. Scale and round properly to nearest integer
    return __float2int_rn(srgb * 255.0f);
}


extern "C"
__global__ void sample(uchar4* pbo, double4* accumulationBuffer, int width, int height,
                       Triangle* triangles, int numTriangles,
                       Sphere* spheres, int numSpheres,
                       PointLight* pointLights, int numPointLights,
                       float* nodeMin,          // 3 floats per node
                       float* nodeMax,          // 3 floats per node
                       int* nodePrimCount,      // 0 = interior, >0 = leaf
                       int* nodeIdx,            // leaf: first position in primIDs; interior: right child index
                       int* primIDs,            // permuted unified ids
                       float* lightNodeMin,     // 3 floats per light node
                       float* lightNodeMax,     // 3 floats per light node
                       int* lightNodePrimCount, // 0 = interior, >0 = leaf
                       int* lightNodeIdx,       // leaf: first position in lightIDs; interior: right child index
                       int* lightIDs,           // permuted point light ids
                       float* lightNodePower,   // 3 floats per light node: summed power of the lights under it
                       float* emissiveNodeMin,     // 3 floats per emissive node
                       float* emissiveNodeMax,     // 3 floats per emissive node
                       int* emissiveNodePrimCount, // 0 = interior, >0 = leaf
                       int* emissiveNodeIdx,       // leaf: first position in emissiveIDs; interior: right child index
                       int* emissiveIDs,           // permuted unified ids of the emissive primitives, like primIDs (< numTriangles = triangle)
                       float* emissiveNodePower,   // 3 floats per emissive node: summed power of the emissives under it
                       int numEmissives,           // 0 = no emissives, and the emissive arrays above were never allocated
                       int frame_number) {

    int x = blockIdx.x * blockDim.x + threadIdx.x;
    int y = blockIdx.y * blockDim.y + threadIdx.y;
    if (x >= width || y >= height) return;
    int index = y * width + x;

    unsigned int seed = index ^ hash(frame_number * 719393);

    float3 throughput = make_float3(1.0f, 1.0f, 1.0f);
    float3 accumulated = make_float3(0.0f, 0.0f, 0.0f);

    // create primary ray ----------------------------------------

    float jitterX = (randomFloat(seed) * 2.0f - 1.0f) / (float)width;
    float jitterY = (randomFloat(seed) * 2.0f - 1.0f) / (float)height;

    // jitter the ray so sampling has an effect on antialiasing
    float3 rayDir = make_float3(
        ((float)x / width) * 2.0f - 1.0f + jitterX,
        1.0f - ((float)y / height) * 2.0f + jitterY,
        -1.0f);

    rayDir.x *= ((float)width / (float)height);

    rayDir = normalize(rayDir);

    float3 rayPos = make_float3(0.0f, 0.0f, 0.0f);

    bool inObject = false;
    float previousIOR = 1.0f;
    Material insideMaterial;

    // check if it hits any object

    for (int bounces = 0; bounces < 10; bounces++) {

        // ray travel to next object  ---------------------------------------

        float3 invDir = make_float3(1.0f / rayDir.x, 1.0f / rayDir.y, 1.0f / rayDir.z);

        auto closest = getBestPrimitive(
            rayPos, rayDir, invDir,
            triangles, numTriangles,
            spheres, numSpheres,
            nodeMin, nodeMax, nodePrimCount, nodeIdx, primIDs,
            epsilon, FLT_MAX);


        float best_t = cuda::std::get<0>(closest);
        if (best_t == -1) break; // -1 if no object was hit
        int best_i = cuda::std::get<1>(closest);
        int type = cuda::std::get<2>(closest);


        float3 normal;
        Material material;

        rayPos = rayDir * best_t + rayPos;

        if (type == 0) {
            Triangle tri = triangles[best_i];

            float3 edge0Vector = tri.v1 - tri.v0;
            float3 edge1Vector = tri.v2 - tri.v1;

            normal = normalize(cross(edge0Vector, edge1Vector));
            material = tri.material;
        } else if (type == 1) {
            Sphere sph = spheres[best_i];

            normal = normalize(rayPos - sph.pos);
            material = sph.material;
        }


        if (dot(rayDir, normal) > 0) {
            normal *= -1;
        }


        // color math ------------------------------------------------

        float3 localColor = getLocalColor(rayPos, rayDir, material, normal, triangles, numTriangles, spheres, numSpheres, pointLights, numPointLights, seed, epsilon, invDir, nodeMin, nodeMax, nodePrimCount, nodeIdx, primIDs, lightNodeMin, lightNodeMax, lightNodePrimCount, lightNodeIdx, lightIDs, lightNodePower, emissiveNodeMin, emissiveNodeMax, emissiveNodePrimCount, emissiveNodeIdx, emissiveIDs, emissiveNodePower, numEmissives, epsilon, best_t);

        float3 albedo = material.color;

        float F0_dielectric = (1 - material.ior) / (1 + material.ior);
        F0_dielectric *= F0_dielectric;
        float3 F0 = (albedo * material.metallic) + (F0_dielectric * (1-material.metallic)); // how much specular light is reflected when looking straight at the surface

        float3 F = F0 + (1 - F0) * std::pow(1 - dot(rayDir, normal)*-1, 5); // how much specular light is reflected at a certain angle (r,g,b) -> each represents how much energy is transmitted

        float specularWeight = fmaxf(F.x, fmaxf(F.y, F.z));
        float diffuseWeight = (1-material.metallic)*fmaxf(albedo.x, fmaxf(albedo.y, albedo.z));

        float P_spec;
        if (diffuseWeight <= 0) {
            P_spec = 1.0f;
        } else {
            P_spec = specularWeight / (specularWeight + diffuseWeight); // odds of going down the specular path: max specular light / (max specular light + max diffuse light)
            P_spec = fminf(0.95, fmaxf(0.05, P_spec)); // clamps P_spec between 0.05 and 0.95 to prevent diffuse's throughput calculation from being absurdly small or large: ... / (1.0 - P_spec when P_spec is 0.99), ... / 0.01 is 100 times the color
        }

        seed = hash(seed);
        float specularRandom = randomFloat(seed);

        if (inObject) { // transmission color math
            float3 remainingLightFraction = make_float3(
                __powf(fmaxf(insideMaterial.color.x, epsilon), best_t * insideMaterial.density),
                __powf(fmaxf(insideMaterial.color.y, epsilon), best_t * insideMaterial.density),
                __powf(fmaxf(insideMaterial.color.z, epsilon), best_t * insideMaterial.density)
            );

            throughput *= remainingLightFraction;

        }


        if (specularRandom < P_spec) { // specular
            // specular

            throughput *= F / P_spec; /* divide by P_spec because F is how much light truly needs to be reflected,
            and P_spec limits the amount of light that can be reflected, if P_spec is 0.2, and F = 0.8, 4 times out of 5,
            diffuse runs, and 1 time out of 5, specular runs. thats 0.2*0.8 = 0.16, so 16% of the time, specular runs.
            This is wrong, and so 0.16 / 0.2 = 0.8, so 80% of the time, specular runs. */

            rayDir = normalize(rayDir - normal*2*dot(rayDir, normal));
            rayPos += normal * epsilon;

            if (material.roughness != 0) {

                float3 random;
                for (int tries = 0; tries < 32; tries++) {
                    float rx = randomFloat(seed) * 2.0f - 1.0f;
                    float ry = randomFloat(seed) * 2.0f - 1.0f;
                    float rz = randomFloat(seed) * 2.0f - 1.0f;

                    random = make_float3(rx, ry, rz);

                    if (length(random) <= 1) break;
                }

                random *= material.roughness * roughness_scalar;

                rayDir = normalize(normalize(rayDir) + random);
            }

        } else { // diffuse OR transmission
            float transmissionRandom = randomFloat(seed);

            if (transmissionRandom < material.transmission) { // transmission
                // transmission

                /*

                CODE:
                "float3 sigma_a = make_float3(
                    __logf(material.color.x),
                    __logf(material.color.y),
                    __logf(material.color.z)
                ) * material.density;" // formula to get the the particle density per unit length

                __logf is log base e

                CODE:
                "float3 remainingLightFraction = __expf(sigma_a*distance);" // amount of light after transmission

                __expf is e^x, x being the parameter

                expanded out, the remainingLightFraction formula is just
                e^(ln(color) * distance * density)

                e is Euler's number
                ln is log base e

                they cancel each other out: e^ln(x) = x

                e^(ln(color) * distance * density)
                simplifies to:

                color^(distance*density)

                so

                remainingLightFraction = color ^ (distance*density)

                CODE:
                "float3 remainingLightFraction = __powf(color, distance*density);"

                */

                float ratioOfIor = previousIOR / material.ior;

                float cosI = -dot(rayDir, normal); // -(N*D)    the length of the normal part of the rayDir (if rayDir faces half way between the normal and parallel to the normal, this would be cos(45 deg) = around 0.707)

                /*

                tangential means perpendicular to the normal, parallel to the surface (normal is perpendicular to the surface)

                next, we need the tangential length of rayDir. we already have the normal part.
                I = rayDir, I_tangentialLength^2 + I_normalLength^2 = 1.0^2

                think of it like a triangle:
                 - the hypotenuse is the rayDir (I) which is normalized to 1.0.
                 - one leg is I_normalLength
                 - the other leg is I_tangentialLength, perpendicular to the normal, and exactly what we need.

                we just use pythagorean theorem for this

                a^2 + b^2 = c^2

                but remember, we also have to scale the tangential component length by the ratioOfIor
                this is the actual refraction part

                I_tangentialLength^2 + I_normalLength^2 = 1.0^2
                I_tangentialLength^2 = 1^2 - I_normalLength^2
                I_tangentialLength = sqrt(1 - I_normalLength^2)

                to implement the ratioOfIor scale, we replace that last line with:

                I_tangentialLength^2 = ratioOfIor^2 * (1 - I_normalLength^2)

                */

                float sinISquared = (ratioOfIor*ratioOfIor) * (1-(cosI*cosI)); // sinI is just I_tangentialLength

                /*

                since we scaled the I_tangentialLength by ratioOfIor without also modifying I_normalLength,
                our I_normalLength will no longer work, and the hypotenuse will no longer be 1.0^2 with the pythagorean theorem

                I_tangentialLength^2 + I_normalLength^2 != 1.0^2

                the solution is to recompute I_normalLength using the pythagorean theorem

                k = 1^2 - tangentialLength^2

                k is the new I_normalLength^2
                k also helps us check one thing:

                scaling I_tangentialLength^2 by ratioOfIor^2 adjusts the tangential for refraction, but in extreme cases,
                ratioOfIor could have pushed I_tangentialLength^2 a bit too far, above 1, leaving no room for I_normalLength^2,
                since  I_tangentialLength^2 + I_normalLength^2 = 1.0^2. we must check for this. k is just 1 - tangentialLength^2 so we reverse the check:

                if k >= 0, refraction is valid, continue.
                else, TIR

                TIR stands for total internal reflection. basically, the most a ray can refract is 90 degrees, where there is no I_normalLength.
                this is ok, but going any further than 90 degrees (I_tangentialLength^2 > 1.0) and you treat it as a reflection.
                TIR only happens when going from a dense medium (high ior) to a less dense medium (lower ior).
                treat a TIR as a regular specular.

                */

                float k = 1.0f - sinISquared;

                if (k >= 0) { // valid refraction, room for both I_normal and I_tangential
                    // these below are NOT LENGTHS, they are the actual tangential and normal direction vectors of rayDir
                    float3 I_normal = normal * -cosI; // negative because it made it negative in cosI, and we have to reverse it to get the direction the ray points opposite to the normal (pointing towards it)
                    float3 I_tangential = normalize(rayDir - I_normal); // subtract the normal component from the total to get the tangential

                    float3 refractedNormal = normal * sqrtf(k) * -1;
                    float3 refractedTangential = I_tangential * sqrtf(sinISquared);

                    rayDir = refractedNormal + refractedTangential;
                    rayPos += (normal*-1) * epsilon;

                    if (inObject) { // leaving
                        inObject = false;
                        previousIOR = 1.0f;
                    } else { // entering
                        inObject = true;
                        previousIOR = material.ior;
                        insideMaterial = material;
                    }

                    throughput *= (1-F)/(1-P_spec);

                } else {
                    throughput *= F; // color math for specular

                    rayDir = normalize(rayDir - normal*2*dot(rayDir, normal));
                    rayPos += normal * epsilon;
                }

            } else { // diffuse

                // diffuse
                float3 contribution = localColor * throughput * (1.0f - F) / (1.0f - P_spec); /* (1-F)/(1-P_spec) for the same reason as the throughput below:
                this branch only runs (1-P_spec) of the time, but should carry (1-F) of the light. without it, direct light comes out scaled by (1-P_spec)
                instead of (1-F), which is off either way: a dark surface (albedo 0.2) comes out 13% too dark head on and ~45% too dark at a glancing angle,
                while a bright one (albedo 0.8) is 1% too dark head on but over 2x too bright when the ray skims the surface */
                if (bounces > 0) { // the first hit's lighting is left exact - only bounced light is capped
                    contribution = clampBrightness(contribution, indirectClamp);
                }
                accumulated += contribution;
                throughput *= (1.0f - F) * albedo / (1.0f - P_spec); /* F/P_spec is the amount of specular light, (1-F)/(1-P_spec) is the amount of diffuse light.
                Since we are in the diffuse branch, F will always be on the gray scale (r, g, and b will be equal) because that is the case for all dielectrics */

                float rx = randomFloat(seed) * 2.0f - 1.0f;
                float ry = randomFloat(seed) * 2.0f - 1.0f;
                float rz = randomFloat(seed) * 2.0f - 1.0f;

                float3 random = make_float3(rx, ry, rz);

                while (length(random) > 1) {
                    rx = randomFloat(seed) * 2.0f - 1.0f;
                    ry = randomFloat(seed) * 2.0f - 1.0f;
                    rz = randomFloat(seed) * 2.0f - 1.0f;

                    random = make_float3(rx, ry, rz);
                }

                float3 dir = normalize(normal + normalize(random));
                rayDir = dir;

                rayPos += normal * epsilon;
            }



        }
    }

    // write to accumulation buffer, tone mapping, then to PBO

    accumulationBuffer[index].x += accumulated.x;
    accumulationBuffer[index].y += accumulated.y;
    accumulationBuffer[index].z += accumulated.z;

    float3 toneMapped = toneMap(
        make_float3(
            accumulationBuffer[index].x / frame_number,
            accumulationBuffer[index].y / frame_number,
            accumulationBuffer[index].z / frame_number
        )
    );

    pbo[index].w = 255;
    pbo[index].x = min(255, linearToSRGB(toneMapped.x));
    pbo[index].y = min(255, linearToSRGB(toneMapped.y));
    pbo[index].z = min(255, linearToSRGB(toneMapped.z));

}
