package arjun.path_tracer_cuda.geometry;

public class Vec3 { // 3 floats
    public float x;
    public float y;
    public float z;

    public Vec3(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vec3(Vec3 vec3) {
        this.x = vec3.x;
        this.y = vec3.y;
        this.z = vec3.z;
    }

    public Vec3 add(Vec3 vec3) {
        this.x += vec3.x;
        this.y += vec3.y;
        this.z += vec3.z;

        return this;
    }

    public Vec3 subtract(Vec3 vec3) {
        this.x -= vec3.x;
        this.y -= vec3.y;
        this.z -= vec3.z;

        return this;
    }

    public Vec3 multiply(Vec3 vec3) {
        this.x *= vec3.x;
        this.y *= vec3.y;
        this.z *= vec3.z;

        return this;
    }

    public Vec3 divide(Vec3 vec3) {
        this.x /= vec3.x;
        this.y /= vec3.y;
        this.z /= vec3.z;

        return this;
    }


    public Vec3 add(float x, float y, float z) {
        this.x += x;
        this.y += y;
        this.z += z;

        return this;
    }

    public Vec3 subtract(float x, float y, float z) {
        this.x -= x;
        this.y -= y;
        this.z -= z;

        return this;
    }

    public Vec3 multiply(float x, float y, float z) {
        this.x *= x;
        this.y *= y;
        this.z *= z;

        return this;
    }

    public Vec3 divide(float x, float y, float z) {
        this.x /= x;
        this.y /= y;
        this.z /= z;

        return this;
    }

    public Vec3 multiply(float scalar) {
        this.x *= scalar;
        this.y *= scalar;
        this.z *= scalar;

        return this;
    }

    public Vec3 divide(float scalar) {
        this.x /= scalar;
        this.y /= scalar;
        this.z /= scalar;

        return this;
    }


    public float dot(Vec3 normal) {
        return x*normal.x + y*normal.y + z*normal.z;
    }

    public static Vec3 cross(Vec3 n, Vec3 e) {
        return new Vec3(
                n.y*e.z - n.z*e.y,
                n.z*e.x - n.x*e.z,
                n.x*e.y - n.y*e.x
        );
    }

    public float length() {
        return (float) Math.sqrt(x*x + y*y + z*z);
    }

    public Vec3 normalize() {
        /*
         * keeps the directions equivalent to 1
         * the distance adding all 3 will give you is equivalent to 1 (via the pythagorean theorem)
         * divides by the total current length (via pythagorean theorem) to keep the length = 1 (example: 3/3 = 1)
         */

        divide(length());

        return this;
    }


    public Vec3 reflect(Vec3 normal) {
        /* D = (dx,dy,dz) | incoming direction
         * N = -> float[] normal | surface normal
         *
         * Formula: D - 2(D * N)N
         * Dot: D*N - how close D is to N (-1 means opposite direction, 1 is exact direction match)
         * NOTE: a dot of -1 (or similar) means the ray points TOWARDS the surface, because the normal points AWAY from the surface
         *
         * The Dot product only measures the axes of D and N that contribute to the bounce.
         * Example:
         * D = (0.6, -0.8, 0.0)
         * N = (0.0. 1.0, 0.0)
         * dot = -0.8, ignoring dx = 0.6 because N has no x value (0) and faces straight upward
         * when the ray bounces, the -0.8 will cause the ray to bounce back up, and the 0.6 will persist because the plane the Normal has no x value and is perfectly flat
         * imagine a ray bouncing off your desk at an angle, the dx or dz won't change, just the dy, because your desk's normal only has a ny.
         */

        float dot = dot(normal);

        x = x - 2 * dot * normal.x;
        y = y - 2 * dot * normal.y;
        z = z - 2 * dot * normal.z;

        normalize();

        return this;
    }



}
