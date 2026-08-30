package arjun.path_tracer_cuda.geometry.BVH;

import arjun.path_tracer_cuda.GPUManager;
import arjun.path_tracer_cuda.geometry.SceneObject;
import arjun.path_tracer_cuda.geometry.Sphere;
import arjun.path_tracer_cuda.geometry.Triangle;
import arjun.path_tracer_cuda.geometry.Vec3;

public class AABB {
    public Vec3 min, max;

    public AABB(Vec3 min, Vec3 max) {
        this.min = new Vec3(min);
        this.max = new Vec3(max);
    }

    public static AABB empty() { // a box that is reversed: the min is the float max and the max is the float min
        return new AABB(
                new Vec3(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
                new Vec3(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY)
        );
    }

    public static AABB computeBounds(int id) { // grows the box to include a primitive by id
        SceneObject obj = GPUManager.instance.objects.get(id);
        AABB box = empty();

        if (obj.getType() == 0) {
            Triangle tri = (Triangle) obj;
            box.grow(tri.p1);
            box.grow(tri.p2);
            box.grow(tri.p3);
        } else if (obj.getType() == 1) {
            Sphere sphere = (Sphere) obj;
            box.grow(new Vec3(sphere.pos).subtract(sphere.radius));
            box.grow(new Vec3(sphere.pos).add(sphere.radius));
        }

        box.pad();
        return box;
    }

    public static Vec3 centroidOf(int id) { // literally the center of the primitive (object, triangle or sphere)
        SceneObject obj = GPUManager.instance.objects.get(id);

        if (obj.getType() == 0) {
            Triangle tri = (Triangle) obj;
            return new Vec3(tri.p1).add(tri.p2).add(tri.p3).divide(3);
        } else {
            Sphere sphere = (Sphere) obj;
            return new Vec3(sphere.pos);
        }
    }

    public void pad() {
        this.min.subtract(0.001f);
        this.max.add(0.001f);
    }

    public boolean includes(Vec3 p) {
        p = new Vec3(p);
        return p.x >= min.x && p.x <= max.x &&
                p.y >= min.y && p.y <= max.y &&
                p.z >= min.z && p.z <= max.z;
    }

    public void grow(Vec3 p) {
        p = new Vec3(p);
        min.x = Math.min(min.x, p.x);
        min.y = Math.min(min.y, p.y);
        min.z = Math.min(min.z, p.z);

        max.x = Math.max(max.x, p.x);
        max.y = Math.max(max.y, p.y);
        max.z = Math.max(max.z, p.z);
    }

    public void grow(AABB other) {
        grow(other.min);
        grow(other.max);
    }

    public float surfaceArea() {
        if (max.x < min.x) return 0;

        float width = max.x - min.x;
        float height = max.y - min.y;
        float depth = max.z - min.z;
        return 2 * (width * height + width * depth + height * depth);
    }


}
