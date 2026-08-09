# Roadmap

Here are the planned features for the path tracer. If you don't know what to contribute, anything under **Next** is fair game - just make sure to check the issue tracker to see if someone is already working on it.

## Next

 - [ ] **CUDA**: BVH (Bounding Volume Hierarchy) for finding the `lowest_t` object
 - [ ] **CUDA + Java**: Add area lights and emissive objects
 - [ ] **CUDA + Java**: Stream compaction between bounces - prevents a minority of cores in a warp from holding back the others

## Later

 - [ ] **CUDA + Java**: Adaptive sampling - take more samples in rough areas of a render
 - [ ] **CUDA + Java**: Raytracing + Rasterization Base alternative for seeing what a render will look like before fully committing

