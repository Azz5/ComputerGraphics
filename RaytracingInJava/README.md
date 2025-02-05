This project is a simple implementation of ray tracing in Java. It demonstrates how rays can be traced in a 3D space to detect intersections with spheres and render the scene using a basic canvas.

Project Structure RayTracer.java: The main program that initializes the Buffered Img, sets up the scene, and performs ray tracing. Surface.Sphere.java: Represents a 3D sphere with a center, radius, and color. The sphere is used for intersection checks in the ray tracing process. Vector3.java: A class representing a 3D vector. It includes methods for vector subtraction and dot product calculations, which are used in the ray-sphere intersection logic. Features Renders a simple 3D scene with three spheres of different colors: Red, Blue, and Green. Uses basic ray-sphere intersection tests to calculate the color of each pixel based on the ray's path. The canvas is rendered using AWT, with each pixel being drawn individually as the rays are traced. Background color of the canvas is white by default. Setup Instructions Prerequisites:

Java Development Kit (JDK) version 8 or later. IDE or text editor (e.g., IntelliJ IDEA, Eclipse, VSCode) to edit the code. Running the Project:

Clone the repository or copy the source code into your local project. Compile the Java files:
```
javac RayTracer.java Surface.Sphere.java Vector3.java
```
Run the RayTracer class:
```
java RayTracer
```
As this project is written in java it works in mac, linux, and windows without needing instructions.
