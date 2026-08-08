![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=java&logoColor=white) ![CUDA](https://img.shields.io/badge/CUDA-76B900?style=for-the-badge&logo=nvidia&logoColor=white)


# Path Tracer


<img width="1568" height="908" alt="image" src="https://github.com/user-attachments/assets/1c13d91d-dd64-402d-83a2-d284e47ef15b" />
  
A 3D path tracer with a Java frontend and CUDA backend. Simulates light in reverse, starting from the camera. NOT real-time; a single image takes thousands of samples to fully render, similar to how blender does it.


## Current features

* Triangles and Spheres, ray intersection math for both individually
* Specular and Diffuse pipelines
* CUDA Kernel backend for all the math
* Materials with color, roughness, and metallic
* Color Point lights


## Prerequisites for contribution

* Java Development Kit JDK, preferrably with IntelliJ IDEA
* NVIDIA Geforce RTX GPU, preferrably 30 series or later (30 and 50 series are supported, anything else try at your own risk)
* CUDA Toolkit to compile .cu code
* C++ compiler (required by CUDA toolkit)


## Things to know before contributing

* NVCC commands are used to compile .cu files into .ptx files.
* Please do not hesitate to write all the comments you want. I especially encourage writing comments next to complicated math to help others understand your work.
* Refrain from copying large chunks of code from AI. Copying small chunks of code from AI is OK.


## Getting started

1. Clone the repo <code>git clone https://github.com/IndianArjun94/3D-Path-Tracer-CUDA.git</code>
2. Select a branch or create a new one <code>git checkout branch_name</code>
3. Recompile all .cu files using nvcc


## How it works

### Fundamentals

A path tracer simulates light ray bounces to form a photorealistic image. In real life, light starts from the source, with a color and intensity, then travels and bounces off of things. As it does this, the light color changes, which is what you see once the light enters your eye. Modern computers do not have enough power to simulate trillions of rays, and it would be a waste, since most of them wouldn't even hit the camera. Instead, the light rays start out at camera and travel in reverse. Each pixel on your monitor gets a ray, called primary rays. Each primary ray starts out with a <code>throughput</code>, the amount of light the the primary ray contributes to that pixel, starting at <code>(1.0f, 1.0f, 1.0f)</code>. As primary rays bounce and divide into other rays, the <code>throughput</code> of that ray naturally decreases. For example, if a primary ray hits a matt blue object, it has a very high chance for the <code>throughput</code> to decrease in red and green and stay mostly the same in blue <code>(0.2f, 0.2f, 0.9f)</code>. Now, if the ray hits any other object, the color of the object will first by multiplied by the <code>throughput</code> before being added to <code>accumulatedColor</code>, the final color of the pixel.

### Other kinds of rays

A scene will never be formed only by primary rays. As a primary ray travels down a path, it systematically creates rays and changes to different types of rays. There are several different types of rays, all commonly used in a scene:

#### Primary and Secondary
* Primary rays: start from the camera, one for each pixel, travel and bounce until they change type
* Shadow rays (secondary): created when finding how bright an object's diffuse color is, checks if there is a light source pointing at the object and if it is blocked by another object

#### Indirect
* Specular rays: transformed into on a bounce, when there needs to be a reflection (specular rays are for reflections)
* Refraction rays: transmormed into when a ray passes through a translucent or transparent object  

### Ray pathways

All rays start out as primary rays, but they change into different kinds as they hit and pass through different materials. In real life, when a ray hits a translucent surface, like tinted window glass, some will go down the diffuse path, some will go down the specular path, and the majority will travel through it. Path tracers cannot account for all three for each ray hit, because if we do, one ray every bounce turns into 3, then 9, then 27, and on. Instead, path tracers choose one path based on how much light is supposed to go through that path. For example, if that tinted glass has 75% of light going through it, we take that as a probability and take a 75% chance to send the ray through for transmission.

#### Specular
  
This is the reflection pathway. All materials, no matter how rough, will have some rays become specular rays and reflect on a hit. Two factors decide how much light is reflected: the material's metallic, and the angle the ray hit the object. Since objects that hit metals only go down the specular path (metals have no diffuse), we must compute the odds for metals and dielectrics (non metals) seperately. The second factor amplifies the first. Looking directly at a semi-reflective surface reflects some of the light, but if you look at a sharper angle, there is more of a reflection. Combining those gives us how much light reflects. For metals, you multiply that value by the material's color (looking at a gold ball, you will only see gold reflections). This color is multiplied into the <code>throughput</code>. <code>accumulatedColor</code> is not changed in specular. Also in the specular path, the reflected ray direction is altered by a random vector multiplied by <code>roughness</code>.
  

#### Diffuse
  
Diffuse is the simplest path. This is the default pathway when a ray rolls not to reflect or transmit through an object (if the object is translucent). No metals will ever go down this path. Diffuse happens when light enters the material just a little bit, going into the surface a little bit, then jumps out in a random direction. Metals have a property where the electrons in them absorb these rays that go into them, converting the energy into heat. The math behind diffuse is very simple. Take the albedo color of the object, then multiply it by <code>throughput</code> and the chance for diffuse and add it to <code>accumulatedColor</code>. You take a random direction vector for the new ray direction.
