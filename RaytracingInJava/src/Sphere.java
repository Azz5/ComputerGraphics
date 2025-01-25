import java.awt.*;

public class Sphere extends Surface
{
    private double radius;

    public Sphere(Vector3 center,double radius,Double reflective,Color color,double specular){
        super(color,specular,reflective,center);
        this.radius = radius;
    }
    public double getRadius() {
        return radius;
    }
}
