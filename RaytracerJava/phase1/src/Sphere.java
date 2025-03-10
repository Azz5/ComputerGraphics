import java.awt.*;

public class Sphere {
    private Vector3 center;
    private double radius;
    private Color color;

    public Sphere(Vector3 center,double radius,Color color){
        this.center = center;
        this.radius = radius;
        this.color = color;
    }

    public Color getColor() {
        return color;
    }

    public double getRadius() {
        return radius;
    }

    public Vector3 getCenter() {
        return center;
    }
}
