package arjun.path_tracer_cuda.geometry.BVH;

import arjun.path_tracer_cuda.GPUManager;

import java.util.ArrayList;

public class BVHNode {
    public AABB bounds;
    public BVHNode left = null; // children
    public BVHNode right = null;
    public int firstPrimIndex, primCount; // the starting primitive id, and how many there are after in this bvh node
    public int splitAxis;

    public static final int MIN_PRIMS_IN_NODE = 4;
    public static final int MAX_DEPTH = 64;

    public BVHNode() {
        bounds = AABB.empty();
    }

    public static BVHNode build(int start, int end, int depth) {
        BVHNode node = new BVHNode();
        AABB centroidBounds = AABB.empty(); // bounds of the centroids of the primitives - we need this because we split the node using this

        GPUManager instance = GPUManager.instance;

        if (instance == null) return null;

        for (int i = start; i < end; i++) {
            int id = instance.primCount[i]; // we reference primIDs here because the raw I could be incorrect - shuffling and sorting of the indexes happen down below
            node.bounds.grow(instance.primBounds[id]);
            centroidBounds.grow(instance.primCentroids[id]);
        }

        node.firstPrimIndex = start;
        node.primCount = end - start;

        if (end - start <= MIN_PRIMS_IN_NODE || depth >= MAX_DEPTH) { // check if this should be a leaf
            return node;
        }

        // find the axis with the largest extent - determines what axis we split on (should be the longest axis)
        float xAxisLength = (centroidBounds.max.x - centroidBounds.min.x);
        float yAxisLength = (centroidBounds.max.y - centroidBounds.min.y);
        float zAxisLength = (centroidBounds.max.z - centroidBounds.min.z);

        float maxAxis = Math.max(xAxisLength, Math.max(yAxisLength, zAxisLength));

        if (maxAxis == xAxisLength) {
            node.splitAxis = 0;
        } else if (maxAxis == yAxisLength) {
            node.splitAxis = 1;
        } else {
            node.splitAxis = 2;
        }

        // pick the split pos
        float split = (centroidBounds.min.getAxis(node.splitAxis) + centroidBounds.max.getAxis(node.splitAxis)) / 2;

        // sort the primitives into left and right
        ArrayList<Integer> orderedIndices = new ArrayList<>(); // Todo, make this more efficient - get rid of arraylist because addFirst and addLast are O(n), and we turn that into O(n^2). Find a better solution without ArrayList entirely.

        int leftSize = 0;

        for (int i = start; i < end; i++) {
            int id = instance.primCount[i];

            if (instance.primCentroids[id].getAxis(node.splitAxis) < split) {
                orderedIndices.addFirst(id);
                leftSize++;
            } else {
                orderedIndices.addLast(id);
            }
        }

        for (int i = start; i < end; i++) {
            instance.primCount[i] = orderedIndices.get(i - start);
        }

        // recursion: build the left and right children
        int mid = start + leftSize;
        if (mid == start || mid == end) mid = (start + end) / 2;

        node.left = build(start, mid, depth + 1);
        node.right = build(mid, end, depth + 1);

        node.primCount = 0;
        /*
           Set to 0 because only leaf (smallest nodes with no children) nodes have primitives.
           Other nodes (interior nodes, with children) don't have any primitives, just children.
           Since we made it down this far, we know that this node is an interior node, so we set primCount to 0.
        */

        return node;

    }

    private static int nextFreeSlot = 0;

    // how many nodes flatten() has written so far - the kernel needs this as its node count
    public static int nodeCount() {
        return nextFreeSlot;
    }

    public static int flatten(BVHNode node) {
        int myIndex = nextFreeSlot;
        nextFreeSlot++;

        GPUManager.instance.minBounds[myIndex*3] = node.bounds.min.x; // 6 floats
        GPUManager.instance.minBounds[myIndex*3+1] = node.bounds.min.y;
        GPUManager.instance.minBounds[myIndex*3+2] = node.bounds.min.z;
        GPUManager.instance.maxBounds[myIndex*3] = node.bounds.max.x;
        GPUManager.instance.maxBounds[myIndex*3+1] = node.bounds.max.y;
        GPUManager.instance.maxBounds[myIndex*3+2] = node.bounds.max.z;

        if (node.primCount == 0) { // interior node
            flatten(node.left);
            int rightIndex = flatten(node.right);

            GPUManager.instance.primCounts[myIndex] = 0; // 1 int
            GPUManager.instance.idx[myIndex] = rightIndex; // right child
        } else {
            GPUManager.instance.primCounts[myIndex] = node.primCount; // 1 int
            GPUManager.instance.idx[myIndex] = node.firstPrimIndex; // first primitive
        }

        return myIndex;
    }


}
