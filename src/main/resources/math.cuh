#pragma once

// ==========================================
// BASIC MATH OPERATORS
// ==========================================

// Addition: a + b
__device__ inline float3 operator+(const float3& a, const float3& b) {
    return make_float3(a.x + b.x, a.y + b.y, a.z + b.z);
}

__device__ inline float3 operator+=(float3& a, const float3& b) {
    a.x += b.x;
    a.y += b.y;
    a.z += b.z;
    return a;
}

__device__ inline float3 operator+(float3& a, const float b) {
    return make_float3(a.x + b, a.y + b, a.z + b);
}

// Subtraction: a - b
__device__ inline float3 operator-(const float3& a, const float3& b) {
    return make_float3(a.x - b.x, a.y - b.y, a.z - b.z);
}

__device__ inline float3 operator-(const float a, const float3& b) {
    return make_float3(a - b.x, a - b.y, a - b.z);
}

__device__ inline float3 operator-=(float3& a, const float3& b) {
    a.x -= b.x;
    a.y -= b.y;
    a.z -= b.z;
    return a;
}

// Multiplication (Vector * Scalar): a * 5.0f
__device__ inline float3 operator*(const float3& a, float s) {
    return make_float3(a.x * s, a.y * s, a.z * s);
}

__device__ inline float3 operator*=(float3& a, float s) {
    a.x *= s;
    a.y *= s;
    a.z *= s;
    return a;
}


// Multiplication (Vector * Vector): a * b (Component-wise, used for Colors)
__device__ inline float3 operator*(const float3& a, const float3& b) {
    return make_float3(a.x * b.x, a.y * b.y, a.z * b.z);
}

__device__ inline float3 operator*=(float3& a, const float3& b) {
    a.x *= b.x;
    a.y *= b.y;
    a.z *= b.z;
    return a;
}

// Division (Vector / Scalar): a / 2.0f
__device__ inline float3 operator/(const float3& a, float s) {
    float inv = 1.0f / s; // Multiplying by inverse is faster on GPU than dividing
    return make_float3(a.x * inv, a.y * inv, a.z * inv);
}

__device__ inline float3 operator/=(float3& a, float s) {
    a.x /= s;
    a.y /= s;
    a.z /= s;
    return a;
}

// ==========================================
// PATH TRACING SPECIFIC MATH
// ==========================================

// Dot Product
__device__ inline float dot(const float3& a, const float3& b) {
    return a.x * b.x + a.y * b.y + a.z * b.z;
}

// Cross Product
__device__ inline float3 cross(const float3& a, const float3& b) {
    return make_float3(
        a.y * b.z - a.z * b.y,
        a.z * b.x - a.x * b.z,
        a.x * b.y - a.y * b.x
    );
}

// Vector Length
__device__ inline float length(const float3& v) {
    return sqrtf(dot(v, v));
}

// Normalize (Make length exactly 1.0)
__device__ inline float3 normalize(const float3& v) {
    float len2 = dot(v, v);

    if (len2 < 1e-8f) {
        return make_float3(0.0f, 0.0f, 0.0f);
    }

    return v * rsqrtf(len2);
}