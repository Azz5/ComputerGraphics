import java.awt.*;

public class Sphere extends Surface
{
    private Vector3 center;
    private double radius;
    private Color color;

    public Sphere(Vector3 center,double radius,Color color,double specular){
        super(color,specular,center);
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
