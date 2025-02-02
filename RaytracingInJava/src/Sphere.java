import org.joml.Matrix4d;

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

    public void transform(Matrix4d transformationMatrix) {
        double scale = transformationMatrix.get(0,0);

        transformationMatrix.m00(1);
        transformationMatrix.m11(1);
        transformationMatrix.m22(1);

        radius *= scale;
        center = new Vector3(transformationMatrix.transform(center));

    }

}
