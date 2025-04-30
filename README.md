# Computer Graphics Projects
This repository contains a collection of computer graphics projects focusing on ray tracing techniques. The projects are implemented in Java and GLSL, offering both a traditional programming approach and a shader-based approach to rendering.

## Projects
### RaytracerJava
Overview:
A ray tracer implemented in Java that demonstrates classic ray tracing techniques. This project covers fundamentals such as:

Basic geometric primitives (spheres, planes, etc.)
Lighting and shading models
Reflection and shadow calculations
Getting Started:

Prerequisites:
Java JDK 8 or above.
Building & Running:
Compile the source code using your preferred Java IDE or via the command line.
Run the main class to see the ray tracing output.
### ShaderToyRayTracer
Overview:
A ray tracer implemented using GLSL, designed to run on ShaderToy or any compatible environment. This project leverages GPU shaders to achieve real-time rendering effects.

Getting Started:

Prerequisites:
A system supporting OpenGL 3.3 or higher.
Familiarity with GLSL and shader programming.
Running the Shader:
Upload the shader code to ShaderToy (or a similar platform) to view and interact with the ray traced scene.

### MinecraftEngine

Overview: A voxel-based engine inspired by Minecraft, implemented in Java using LWJGL (GLFW, OpenGL). Key features include:

Chunked world representation with dynamic mesh generation and TextureAtlas support

Raycast-based block picking and placement across chunk boundaries

Camera movement with pitch/yaw in camera space

Block adjacency updates and neighbor chunk mesh rebuilding for seamless edits

Getting Started:

Prerequisites: Java JDK 11 or above, Gradle, LWJGL 3.3.1.

Building & Running:

Clone the repository and navigate to the MinecraftEngine folder:
```
git clone <repo_url>
cd MinecraftEngine
```
Launch via Gradle:

```
./gradlew run
```

Controls: WASD to move, mouse to look, left-click to place blocks, right-click to remove.

Enjoy exploring and extending your own block-based world!*
