import java.awt.*;

public class Cylinder extends Surface{

    private double radius;
    private double height;
    private Vector3 center;

    public Cylinder(Vector3 center ,Double radius, Double height,Color color, double specular, double reflective, double refraction_index, double transparency) {
        super(color, specular, reflective, refraction_index, transparency);
        this.radius = radius;
        this.height = height;
        this.center = center;
    }

    public double getRadius() {
        return radius;
    }

    public double getHeight() {
        return height;
    }

    public Vector3 getCenter() {
        return center;
    }
}
