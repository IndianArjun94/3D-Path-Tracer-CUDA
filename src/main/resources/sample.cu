#include <cuda_runtime.h>
#include <math.h>
#include <cuda/std/utility>
#include <cfloat> // Required for FLT_MAX

#include "scene_types.cuh"
#include "math.cuh"

__constant__ float epsilon = 0.0001f;
__constant__ float roughness_scalar = 0.4f;
__constant__ float e = 2.7182f;

__device__ unsigned int hash(unsigned int seed) {
    unsigned int state = seed * 747796405u + 2891336453u;
    unsigned int word = ((state >> ((state >> 28u) + 4u)) ^ state) * 277803737u;
    return (word >> 22u) ^ word;
}

__device__ float randomFloat(unsigned int& seed) {
    seed = hash(seed);
    return fmaxf(0.001, fminf(0.999, (float)seed / 4294967295.0f));
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

        float discriminant = b*b - 4*a*c;

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

__device__ float3 getLocalColor(float3 rayPos, float3 rayDir, Material material, float3 normal, Triangle* triangles, int numTriangles, Sphere* spheres, int numSpheres, PointLight* pointLights, int numPointLights, unsigned int& seed) {
    float3 totalLight = make_float3(0.0f, 0.0f, 0.0f);

    for (int i = 0; i < numPointLights; i++) {
        const PointLight& light = pointLights[i];

        seed = hash(seed);
        float rx = randomFloat(seed) * 2.0f - 1.0f;
        seed = hash(seed);
        float ry = randomFloat(seed) * 2.0f - 1.0f;
        seed = hash(seed);
        float rz = randomFloat(seed) * 2.0f - 1.0f;

        float light_radius = 0.35f; // Increase for softer shadows, decrease for sharper
        float3 jitteredLightPos = light.pos + (make_float3(rx, ry, rz) * light_radius);

        float3 lightDir = jitteredLightPos - rayPos;

        float distance = length(lightDir);
        lightDir = normalize(lightDir);

        float3 shadowRayPos = rayPos + (normal * epsilon);

        bool isShadowed = false;

        auto smallestTriangleTravel = getLowestTravelTriangles(shadowRayPos, lightDir, triangles, numTriangles);
        auto smallestSphereTravel = getLowestTravelSpheres(shadowRayPos, lightDir, spheres, numSpheres);

        if (smallestTriangleTravel.first > 0 && smallestTriangleTravel.first < distance) {
            isShadowed = true;
        } else if (smallestSphereTravel.first > 0 && smallestSphereTravel.first < distance) {
            isShadowed = true;
        }

        distance = fmaxf(distance, light_radius);

        if (!isShadowed) {
            float3 L = lightDir;
            float intensity = fmaxf(0, dot(normal, L)) * light.intensity;
            totalLight += (light.color * intensity) / (distance * distance);
        }

    }

    float3 finalColor = material.color / 3.14159265f;
    finalColor *= totalLight;
    finalColor *= 1-material.metallic;

    return finalColor;
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
__global__ void sample(uchar4* pbo, double4* accumulationBuffer, int width, int height, Triangle* triangles, int numTriangles, Sphere* spheres, int numSpheres, PointLight* pointLights, int numPointLights, int frame_number) {
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

    // check if it hits any object

    for (int bounces = 0; bounces < 10; bounces++) {

        // finding the next object AND getting a new rayPos at the hit pos, rayDir, normal, and material ---------------------------------------


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


        if (dot(rayDir, normal) > 0) {
            normal *= -1;
        }


        // color math ------------------------------------------------

        float3 localColor = getLocalColor(rayPos, rayDir, material, normal, triangles, numTriangles, spheres, numSpheres, pointLights, numPointLights, seed);

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

        if (inObject) {
            float3 remainingLightFraction = make_float3(
                __powf(material.color.x, lowest_t),
                __powf(material.color.y, lowest_t),
                __powf(material.color.z, lowest_t)
            ); // lowest_t = distance

            throughput *= remainingLightFraction * (1.0f - F) * albedo / (1.0f - P_spec);;

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
                );" // formula to get the the particle density per unit length

                __logf is log base e

                CODE:
                "float3 remainingLightFraction = __expf(sigma_a*distance);" // amount of light after transmission

                __expf is e^x, x being the parameter

                expanded out, the remainingLightFraction formula is just
                e^(ln(color) * distance)

                e is Euler's number
                ln is log base e

                they cancel each other out: e^ln(x) = x

                e^(ln(color) * distance)
                simplifies to:

                color^distance

                so

                remainingLightFraction = color ^ distance

                CODE:
                "float3 remainingLightFraction = __powf(color, distance);"

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
                            previousIOR = material.ior;
                        } else { // entering
                            inObject = true;
                        }

                    } else {
                        throughput *= F; // color math for specular

                        rayDir = normalize(rayDir - normal*2*dot(rayDir, normal));
                        rayPos += normal * epsilon;
                    }



            } else { // diffuse

                // diffuse
                accumulated += localColor * throughput;
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