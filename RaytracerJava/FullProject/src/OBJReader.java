import org.joml.Matrix4d;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class OBJReader {

    public static List<Surface.Triangle> readOBJFile(String filePath) throws IOException {
        List<Point3d> vertices = new ArrayList<>();
        List<Surface.Triangle> triangles = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("v ")) {
                    // Parse vertex
                    String[] parts = line.split("\\s+");
                    double x = Double.parseDouble(parts[1]);
                    double y = Double.parseDouble(parts[2]);
                    double z = Double.parseDouble(parts[3]);
                    vertices.add(new Point3d(x, y, z));
                } else if (line.startsWith("f ")) {
                    // Parse face (assuming triangles)
                    String[] parts = line.split("\\s+");
                    int v1 = Integer.parseInt(parts[1].split("/")[0]) - 1;
                    int v2 = Integer.parseInt(parts[2].split("/")[0]) - 1;
                    int v3 = Integer.parseInt(parts[3].split("/")[0]) - 1;

                    // Create a triangle from the vertices
                    Point3d pointA = vertices.get(v1);
                    Point3d pointB = vertices.get(v2);
                    Point3d pointC = vertices.get(v3);

                    // Assuming default material properties for the triangle
                    Surface.Triangle triangle = new Surface.Triangle(Color.RED, -1, 0., 1, 1, pointA, pointB, pointC);
                    triangle.scale(8,8,8);
                    Matrix4d translate = new Matrix4d().translate(-0.7,-1,3);
                    Matrix4d rotate = new Matrix4d().rotateY(Math.toRadians(180));
                    triangle.transform(translate.mul(rotate));
                    triangles.add(triangle);
                }
            }
        }

        return triangles;
    }

    public static void main(String[] args) {
        try {
            List<Surface.Triangle> triangles = readOBJFile("Data/bunny.obj");

            for (Surface.Triangle triangle : triangles) {
                System.out.println("Surface.Triangle: " + triangle.getPointA() + ", " + triangle.getPointB() + ", " + triangle.getPointC());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
