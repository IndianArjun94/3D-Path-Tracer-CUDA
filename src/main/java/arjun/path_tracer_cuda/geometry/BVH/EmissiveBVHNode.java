package arjun.path_tracer_cuda.geometry.BVH;

import arjun.path_tracer_cuda.GPUManager;
import arjun.path_tracer_cuda.geometry.SceneObject;
import arjun.path_tracer_cuda.geometry.Sphere;
import arjun.path_tracer_cuda.geometry.Triangle;
import arjun.path_tracer_cuda.geometry.Vec3;

public class EmissiveBVHNode extends BVHNode { // bvh node for emissive primitives - the point light bvh, but for triangles and spheres that give off light
    public EmissiveBVHNode left = null; // children
    public EmissiveBVHNode right = null;
    public Vec3 emissivePower = new Vec3(0,0,0);

    public static final int MAX_EMISSIVES_IN_NODE = 1;

    @Override
    public BVHNode build(int start, int end, int depth) {
        EmissiveBVHNode node = new EmissiveBVHNode();
        AABB centroidBounds = AABB.empty(); // bounds of the centroids of the primitives - we need this because we split the node using this

        GPUManager instance = GPUManager.instance;

        if (instance == null) return null;

        for (int i = start; i < end; i++) { // grow the bounds for whatever is inside
            int id = instance.emissiveIDs[i]; // ids into the objects arraylist, like primitiveIDs - we reference emissiveIDs here because the raw I could be incorrect - shuffling and sorting of the indexes happen down below
            node.bounds.grow(instance.primBounds[id]); // the whole primitive's box, not just its centroid like point lights - an emitter has size, and scoring a node by its centre alone can score 0 for a light that is partly in front of a surface
            centroidBounds.grow(instance.primCentroids[id]);
        }

        node.firstPrimIndex = start;
        node.primCount = end - start;

        if (end - start <= MAX_EMISSIVES_IN_NODE) { // check if this should be a leaf
            for (int i = start; i < end; i++) {
                int id = instance.emissiveIDs[i];
                node.emissivePower.add(powerOf(instance.objects.get(id)));
            }
            return node;
        }

        int mid = splitAndBuildChildren(node, centroidBounds, instance.primCentroids, instance.emissiveIDs, start, end, depth);

        node.left = (EmissiveBVHNode) build(start, mid, depth + 1);
        node.right = (EmissiveBVHNode) build(mid, end, depth + 1);

        node.emissivePower = new Vec3(node.left.emissivePower).add(node.right.emissivePower);

        return node;
    }

    /* how much light a primitive can throw at a point: linear emission * the most area it can show at once.
     * this is the emissive version of a point light's color * intensity - a point light gives color * intensity * cos / d^2,
     * a small emitter gives emission * area * cosLight * cos / d^2, and those match when the emitter faces the point head on.
     * a triangle shows its whole area face on; a sphere only ever shows a disc of radius r */
    private static Vec3 powerOf(SceneObject obj) {
        if (obj.getType() == 0) {
            Triangle tri = (Triangle) obj;
            float area = Vec3.cross(new Vec3(tri.p2).subtract(tri.p1), new Vec3(tri.p3).subtract(tri.p1)).length() * 0.5f;
            return tri.material.linearEmission().multiply(area);
        } else {
            Sphere sphere = (Sphere) obj;
            float area = (float) Math.PI * sphere.radius * sphere.radius;
            return sphere.material.linearEmission().multiply(area);
        }
    }

    private static int nextFreeSlot = 0;

    // how many nodes flatten() has written so far
    public static int nodeCount() {
        return nextFreeSlot;
    }

    public static int flatten(EmissiveBVHNode node) {
        int myIndex = nextFreeSlot;
        nextFreeSlot++;

        GPUManager.instance.emissiveMinBounds[myIndex*3] = node.bounds.min.x; // 9 floats
        GPUManager.instance.emissiveMinBounds[myIndex*3+1] = node.bounds.min.y;
        GPUManager.instance.emissiveMinBounds[myIndex*3+2] = node.bounds.min.z;
        GPUManager.instance.emissiveMaxBounds[myIndex*3] = node.bounds.max.x;
        GPUManager.instance.emissiveMaxBounds[myIndex*3+1] = node.bounds.max.y;
        GPUManager.instance.emissiveMaxBounds[myIndex*3+2] = node.bounds.max.z;
        GPUManager.instance.emissivePower[myIndex*3] = node.emissivePower.x;
        GPUManager.instance.emissivePower[myIndex*3+1] = node.emissivePower.y;
        GPUManager.instance.emissivePower[myIndex*3+2] = node.emissivePower.z;

        if (node.primCount == 0) { // interior node
            flatten(node.left);
            int rightIndex = flatten(node.right);

            GPUManager.instance.emissiveCounts[myIndex] = 0; // 1 int
            GPUManager.instance.emissiveIdx[myIndex] = rightIndex; // right child
        } else {
            GPUManager.instance.emissiveCounts[myIndex] = node.primCount; // 1 int
            GPUManager.instance.emissiveIdx[myIndex] = node.firstPrimIndex; // first emissive
        }

        return myIndex;
    }
}
