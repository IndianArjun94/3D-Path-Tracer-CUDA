![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=java&logoColor=white) ![CUDA](https://img.shields.io/badge/CUDA-76B900?style=for-the-badge&logo=nvidia&logoColor=white)


<h1>
  Path Tracer
</h1>



<img width="1568" height="908" alt="image" src="https://github.com/user-attachments/assets/1c13d91d-dd64-402d-83a2-d284e47ef15b" />

<body>
<p>
  <br>
  
  A 3D path tracer with a Java frontend and CUDA backend. Simulates light in reverse, starting from the camera. 
  NOT real-time; a single image takes thousands of samples to fully render, similar to how blender does it.
  
</p>

<br>

<h2>
  Current features
</h2>

<ul>
  <li>Triangles and Spheres, ray intersection math for both individually</li>
  <li>Specular and Diffuse pipelines</li>
  <li>CUDA Kernel backend for all the math</li>
  <li>Materials with color, roughness, and metallic</li>
  <li>Color Point lights</li>
</ul>

<br>

<h2>
  Prerequisites for contribution
</h2>

<ul>
  <li>Java Development Kit JDK, preferrably with IntelliJ IDEA</li>
  <li>NVIDIA Geforce RTX GPU, preferrably 30 series or later (30 and 50 series are supported, anything else try at your own risk)</li>
  <li>CUDA Toolkit to compile .cu code</li>
  <li>C++ compiler (required by CUDA toolkit)</li>
</ul>

<br>

<h2>
  Things to know before contributing
</h2>

<ul>
  <li>NVCC commands are used to compile .cu files into .ptx files.</li>
  <li>Please do not hesitate to write all the comments you want. I especially encourage writing comments next to complicated math to help others understand your work.</li>
  <li>Refrain from copying large chunks of code from AI. Copying small chunks of code from AI is OK.</li>
</ul>

<br>

<h2>
  Getting started
</h2>

<ol>
  <li>Clone the repo <code>git clone https://github.com/IndianArjun94/3D-Path-Tracer-CUDA.git</code></li>
  <li>Select a branch or create a new one <code>git checkout branch_name</code></li>
  <li>Recompile all .cu files using nvcc</li>
</ol>

<br>

<h2>
  How it works
</h2>

<h3>Fundamentals</h3>
<p>
  A path tracer simulates light ray bounces to form a photorealistic image. In real life, light starts from the source, with a color and intensity, then travels and bounces off of things. As it does this, the light color changes, which is what you see once the light enters your eye. Modern computers do not have enough power to simulate trillions of rays, and it would be a waste, since most of them wouldn't even hit the camera. Instead, the light rays start out at camera and travel in reverse. Each pixel on your monitor gets a ray, called primary rays. Each primary ray starts out with a <code>throughput</code>, the amount of light the the primary ray contributes to that pixel, starting at <code>(1.0f, 1.0f, 1.0f)</code>. As primary rays bounce and divide into other rays, the <code>throughput</code> of that ray naturally decreases. For example, if a primary ray hits a matt blue object, it has a very high chance for the <code>throughput</code> to decrease in red and green and stay mostly the same in blue <code>(0.2f, 0.2f, 0.9f)</code>. Now, if the ray hits any other object, the color of the object will first by multiplied by the <code>throughput</code> before being added to <code>accumulatedColor</code>, the final color of the pixel.
</p>

<h3>Other kinds of rays</h3>
<p>
  A scene will never be formed only by primary rays. As a primary ray travels down a path, it systematically creates rays and changes to different types of rays. There are several different types of rays, all commonly used in a scene:
  <h4>Primary and Secondary</h4>
  <ul>
    <li>Primary rays: start from the camera, one for each pixel, travel and bounce until they change type</li>
    <li>Shadow rays (secondary): created when finding how bright an object's diffuse color is, checks if there is a light source pointing at the object and if it is blocked by another object</li>
  </ul>
  <h4>Indirect</h4>
  <ul>
    <li>Specular rays: transformed into on a bounce, when there needs to be a reflection (specular rays are for reflections)</li>
    <li>Refraction rays: transmormed into when a ray passes through a translucent or transparent object</li>
  </ul>
</p>

</body>
