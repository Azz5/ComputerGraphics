import java.awt.*;

public class Sphere extends Surface
{
    private double radius;

    public Sphere(Vector3 center,double radius,double reflective,Color color,double specular, double refraction_index, double transparency){
        super(color,specular,reflective,refraction_index,transparency,center);
        this.radius = radius;
    }
    public double getRadius() {
        return radius;
    }
}
