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
