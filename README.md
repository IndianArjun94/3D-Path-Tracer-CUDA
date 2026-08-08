![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=java&logoColor=white) ![CUDA](https://img.shields.io/badge/CUDA-76B900?style=for-the-badge&logo=nvidia&logoColor=white)


# Path Tracer


<img width="1568" height="908" alt="image" src="https://github.com/user-attachments/assets/1c13d91d-dd64-402d-83a2-d284e47ef15b" />
  
A 3D path tracer with a Java frontend and CUDA backend. Simulates light in reverse, starting from the camera. NOT real-time; a single image takes thousands of samples to fully render, similar to how blender does it.


## Current features

* Triangles and Spheres, ray intersection math for both individually
* Specular, Diffuse, Transmission pipelines
* CUDA Kernel backend for all the math
* Materials with color, roughness, metallic, ior, transmission
* Color Point lights


## Prerequisites for contribution

* Java Development Kit JDK, preferrably with IntelliJ IDEA
* NVIDIA Geforce RTX gpu
* CUDA Toolkit to compile .cu code
* C++ compiler (required by CUDA toolkit)


## Things to know before contributing

* `nvcc` commands are used to compile .cu files into .ptx files.
* Please do not hesitate to write all the comments you want. I especially encourage writing comments next to complicated math to help others understand your work.
* Refrain from copying large chunks of code from AI. Copying small chunks of code from AI is OK.


## Getting started

Check out `CONTRIBUTING.md`



## How it works

### Fundamentals

A path tracer simulates light ray bounces to form a photorealistic image. In real life, light starts from the source, with a color and intensity, then travels and bounces off of things. As it does this, the light color changes, which is what you see once the light enters your eye. Modern computers do not have enough power to simulate trillions of rays, and it would be a waste, since most of them wouldn't even hit the camera. Instead, the light rays start out at camera and travel in reverse. Each pixel on your monitor gets a ray, called primary rays. Each primary ray starts out with a `throughput`, the amount of light the the primary ray contributes to that pixel, starting at `(1.0f, 1.0f, 1.0f)`. As primary rays bounce and divide into other rays, the `throughput` of that ray naturally decreases. For example, if a primary ray hits a matt blue object, it has a very high chance for the `throughput` to decrease in red and green and stay mostly the same in blue `(0.2f, 0.2f, 0.9f)`. Now, if the ray hits any other object, the color of the object will first by multiplied by the `throughput` before being added to `accumulatedColor`, the final color of the pixel.

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
  
This is the reflection pathway. All materials, no matter how rough, will have some rays become specular rays and reflect on a hit. Two factors decide how much light is reflected: the material's metallic, and the angle the ray hit the object. Since objects that hit metals only go down the specular path (metals have no diffuse), we must compute the odds for metals and dielectrics (non metals) seperately. The second factor amplifies the first. Looking directly at a semi-reflective surface reflects some of the light, but if you look at a sharper angle, there is more of a reflection. Combining those gives us how much light reflects. For metals, you multiply that value by the material's color (looking at a gold ball, you will only see gold reflections). This color is multiplied into the `throughput`. `accumulatedColor` is not changed in specular. Also in the specular path, the reflected ray direction is altered by a random vector multiplied by the roughness, blurring the reflection..
  

#### Diffuse
  
Diffuse is the simplest path. This is the default pathway when a ray rolls not to reflect or transmit through an object (if the object is translucent). No metals will ever go down this path. Diffuse happens when light enters the material just a little bit, going into the surface a little bit, then jumps out in a random direction. Metals have a property where the electrons in them absorb these rays that go into them, converting the energy into heat. The math behind diffuse is very simple. Take the albedo color of the object, then multiply it by `throughput` and the chance for diffuse and add it to `accumulatedColor`. You take a random direction vector for the new ray direction.


#### Transmission

Transmission is the path where rays go through and refract in a tranclucent object. The change in `throughput` is decided by a whole lot of math, which you can find broken down and explained in the code. It all simplifies down to <code>color<sup>(distance * density)</sup></code>. The ray refracts based on the `ratioOfIor` - ior of the previous medium, usually air, divided by the ior of the current object the ray is passing through. Before we apply this, we need to take the ray direction and split it into a tangential component and a normal component. The normal component the part of rayDir that points to the normal, so really is the -normal component. The tangential component is parallel to the normal. These two combined form rayDir. We first scale the tangential component by `ratioOfIor`. We must also change the normal component now so the refracted ray direction stays balanced. To do this, we must recompute the normal component. Check the length of the tangential component, to see if there is any space left for a normal component, the tangential component length should be less that 1. If that is so, there is still room for the normal component, and we find whats left to between the tangential component and 1 to get the normal component. Otherwise, `ratioOfIor` make the tangential component go too far, refracting out of the material, resulting in a TIR - total internal refraction. Path tracers treat this as a specular reflection.
