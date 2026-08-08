# Contributing

Thank you for showing interest in contributing! This is a Java + CUDA passion project, and all contributions - large or small are welcome.



## Prerequisites

- Java Development Kit (JDK) 24 with Intellij IDEA
- NVIDIA GeForce RTX GPU, preferrably 30 series or later, older cards untested
- CUDA Toolkit (to compile .cu files)
- A C++ compiler (required by CUDA toolkit)


## Getting started

1. Fork the repo, then clone your fork:
```
   git clone https://github.com/<your-username>/3D-Path-Tracer-CUDA.git
```

2. Create a branch for your change:
```
   git checkout -b your-branch-name
```

3. Recompile all `.cu` files with `nvcc` - the compiled `.ptx` files are GPU specific, so existing ones may not work for you. (backwards compatible; new GPUs can run old files, old GPUs cannot run new files)

4. Code your changes, then create a pull request (see below).


## Ways to contribute

You don't have to code CUDA to contribute. There are several areas in the Java frontend that you can help improve. Useful contributions include:
- **Code**: new features listed in the roadmap, bugfixes, improving code efficiency
- **Documentation**: updating `README.md` with overviews of new features
- **Comments**: add comments to any complicated code or math - ***very important if you coded in a new complicated feature***
- **Testing**: find bugs in the code and list them in the issue tracker
- **Good first issues**: check the issue tracker for issues labeled `good first issue`


## Before you start coding

- Don't hesitate to list any non-trivial issues on the issue tracker
- **Check the roadmap if you don't know what to do next**
- Small fixes such as typos do not need an issue and can go straight to PR


## Coding Guidelines

- **Comment your complicated code and math**: this is required so others can understand what it does and how it works
- **Minimize AI copy-paste**: small AI patches or OK, do NOT use AI to *write* any large chunks of code (hopwever, using AI to *explain* you code and concepts is highly encouraged)
- **Code styling**: match the naming conventions and indentations of the project or scope.


## Pull requests (PR)

1. Push your branch and open a PR against `master`.
2. In the PR description, include:
   - What the change does and why
   - Any incomplete parts or limitations
3. Respond to comments, back and forth is normal
