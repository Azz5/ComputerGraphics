import java.awt.*;

public class Sphere extends Surface
{
    private double radius;
    private Vector3 center;

    public Sphere(Vector3 center,double radius,double reflective,Color color,double specular, double refraction_index, double transparency){
        super(color,specular,reflective,refraction_index,transparency);
        this.radius = radius;
        this.center = center;
    }
    public double getRadius() {
        return radius;
    }
    public  Vector3 getCenter() {
        return center;
    }
}
