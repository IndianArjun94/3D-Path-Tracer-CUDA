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

<h2>
  Current Features
</h2>

<ul>
  <li>Triangles and Spheres, ray intersection math for both individually</li>
  <li>Specular and Diffuse pipelines</li>
  <li>CUDA Kernel backend for all the math</li>
  <li>Materials with color, roughness, and metallic</li>
  <li>Color Point lights</li>
</ul>

<h2>
  Prerequisites for Contribution
</h2>

<ul>
  <li>Java Development Kit JDK, preferrably with IntelliJ IDEA</li>
  <li>NVIDIA Geforce RTX GPU, preferrably 30 series or later (30 and 50 series are supported, anything else try at your own risk)</li>
  <li>CUDA Toolkit to compile .cu code</li>
  <li>C++ compiler (required by CUDA toolkit</li>
</ul>


<h2>
  Things to know before contributing
</h2>

<ul>
  <li>NVCC commands are used to compile .cu files into .ptx files. I am not an expert at using them. If the included NVCC commands don't work, your best bet is to use AI to generate one for you.</li>
  <li>Please do not hesitate to write all the comments you want. I especially encourage writing comments next to complicated math to help others understand your work.</li>
  <li>Refrain from copying large chunks of code from AI. Copying small chunks of code from AI is OK.</li>
</ul>

  
</body>
