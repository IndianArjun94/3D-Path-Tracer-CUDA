package arjun.path_tracer_cuda.geometry.BVH;

import arjun.path_tracer_cuda.GPUManager;
import arjun.path_tracer_cuda.geometry.PointLight;
import arjun.path_tracer_cuda.geometry.Vec3;

import java.util.ArrayList;

public class PLightBVHNode extends BVHNode { // bvh node for point lights
    public PLightBVHNode left = null; // children
    public PLightBVHNode right = null;
    public Vec3 lightPower = new Vec3(0,0,0);

    public static final int MAX_PLIGHTS_IN_NODE = 1;

    @Override
    public BVHNode build(int start, int end, int depth) {
        PLightBVHNode node = new PLightBVHNode();
        AABB centroidBounds = AABB.empty(); // bounds of the centroids of the primitives - we need this because we split the node using this

        GPUManager instance = GPUManager.instance;

        if (instance == null) return null;

        for (int i = start; i < end; i++) { // grow the bounds for whatever is inside
            int id = instance.pLightIDs[i]; // we reference pLightIDs here because the raw I could be incorrect - shuffling and sorting of the indexes happen down below
            node.bounds.grow(instance.pLightCentroids[id]);
            centroidBounds.grow(instance.pLightCentroids[id]);
        }

        node.firstPrimIndex = start;
        node.primCount = end - start;

        if (end - start <= MAX_PLIGHTS_IN_NODE) { // check if this should be a leaf
            for (int i = start; i < end; i++) {
                int id = instance.pLightIDs[i];
                PointLight light = instance.pointLights.get(id);
                node.lightPower.add(new Vec3(light.color).multiply(light.intensity));
            }
            return node;
        }

        int mid = splitAndBuildChildren(node, centroidBounds, instance.pLightCentroids, instance.pLightIDs, start, end, depth);

        node.left = (PLightBVHNode) build(start, mid, depth + 1);
        node.right = (PLightBVHNode) build(mid, end, depth + 1);

        node.lightPower = new Vec3(node.left.lightPower).add(node.right.lightPower);

        return node;
    }

    private static int nextFreeSlot = 0;

    // how many nodes flatten() has written so far
    public static int nodeCount() {
        return nextFreeSlot;
    }

    public static int flatten(PLightBVHNode node) {
        int myIndex = nextFreeSlot;
        nextFreeSlot++;

        GPUManager.instance.pLightMinBounds[myIndex*3] = node.bounds.min.x; // 9 floats
        GPUManager.instance.pLightMinBounds[myIndex*3+1] = node.bounds.min.y;
        GPUManager.instance.pLightMinBounds[myIndex*3+2] = node.bounds.min.z;
        GPUManager.instance.pLightMaxBounds[myIndex*3] = node.bounds.max.x;
        GPUManager.instance.pLightMaxBounds[myIndex*3+1] = node.bounds.max.y;
        GPUManager.instance.pLightMaxBounds[myIndex*3+2] = node.bounds.max.z;
        GPUManager.instance.pLightPower[myIndex*3] = node.lightPower.x;
        GPUManager.instance.pLightPower[myIndex*3+1] = node.lightPower.y;
        GPUManager.instance.pLightPower[myIndex*3+2] = node.lightPower.z;

        if (node.primCount == 0) { // interior node
            flatten(node.left);
            int rightIndex = flatten(node.right);

            GPUManager.instance.pLightCounts[myIndex] = 0; // 1 int
            GPUManager.instance.pLightIdx[myIndex] = rightIndex; // right child
        } else {
            GPUManager.instance.pLightCounts[myIndex] = node.primCount; // 1 int
            GPUManager.instance.pLightIdx[myIndex] = node.firstPrimIndex; // first light
        }

        return myIndex;
    }
}
