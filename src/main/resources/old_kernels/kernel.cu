extern  "C"

__global__ void testKernel(uchar4* pbo, int width, int height) {
    int x = blockIdx.x * blockDim.x + threadIdx.x;
    int y = blockIdx.y * blockDim.y + threadIdx.y;

    if (x >= width || y >= height) return;

    int index = y * width + x;

    pbo[index].x = 255;
    pbo[index].y = (unsigned char)((float)x/width*255);
    pbo[index].z = (unsigned char)((float)y/height*255);
    pbo[index].w = 255; // alpha
}