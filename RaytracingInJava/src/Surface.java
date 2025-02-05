import org.joml.Matrix4d;
import org.joml.Vector3d;

import java.awt.*;

public class Surface {
    private Color color;
    private double specular;
    private double reflective;
    private double refractiveIndex; // e.g., 1.0 for air, 1.5 for glass
    private double transparency;
    public Surface(Color color, double specular,double reflective,double refraction_index, double transparency) {
        this.color = color;
        this.specular = specular;
        this.reflective = reflective;
        this.refractiveIndex = refraction_index;
        this.transparency = transparency;
    }


    public double getRefractiveIndex() { return refractiveIndex; }
    public double getTransparency() { return transparency; }

    public Color getColor() {
        return color;
    }

    public double getReflective() {
        return reflective;
    }

    public double getSpecular() {
        return specular;
    }

    public static class Triangle extends Surface {
        private Point3d pointA;
        private Point3d pointB;
        private Point3d pointC;
        private Vector3 AtoB_edge;
        private Vector3 BtoC_edge;
        private Vector3 CtoA_edge;
        private Vector3 triEdgeNormal;

        public Triangle(Color color, double specular, double reflective,
                        double refraction_index, double transparency,
                        Point3d pointA, Point3d pointB, Point3d pointC) {
            super(color, specular, reflective, refraction_index, transparency);
            this.pointA = pointA;
            this.pointB = pointB;
            this.pointC = pointC;
            this.AtoB_edge = pointB.subtract(pointA);
            this.BtoC_edge = pointC.subtract(pointB);
            this.CtoA_edge = pointA.subtract(pointC);
            Vector3 triEdge1 = pointB.subtract(pointA);
            Vector3 triEdge2 = pointC.subtract(pointA);
            triEdgeNormal = triEdge1.cross(triEdge2);
        }

        public Point3d getPointA() {
            return pointA;
        }

        public Point3d getPointB() {
            return pointB;
        }

        public Point3d getPointC() {
            return pointC;
        }

        public Vector3 getAtoB_edge() {
            return AtoB_edge;
        }

        public Vector3 getBtoC_edge() {
            return BtoC_edge;
        }

        public Vector3 getCtoA_edge() {
            return CtoA_edge;
        }

        public Vector3 getTriEdgeNormal() {
            return triEdgeNormal;
        }

        // Scale the triangle by scaling factors for each axis
        public void scale(double sx, double sy, double sz) {
            pointA = scalePoint(pointA, sx, sy, sz);
            pointB = scalePoint(pointB, sx, sy, sz);
            pointC = scalePoint(pointC, sx, sy, sz);
            updateEdgesAndNormal();
        }

        // Helper method to scale a single point
        private Point3d scalePoint(Point3d point, double sx, double sy, double sz) {
            double x = point.getX() * sx;
            double y = point.getY() * sy;
            double z = point.getZ() * sz;
            return new Point3d(x, y, z);
        }

        // Update the edge vectors and normal based on the current vertex positions
        private void updateEdgesAndNormal() {
            this.AtoB_edge = pointB.subtract(pointA);
            this.BtoC_edge = pointC.subtract(pointB);
            this.CtoA_edge = pointA.subtract(pointC);
            Vector3 triEdge1 = pointB.subtract(pointA);
            Vector3 triEdge2 = pointC.subtract(pointA);
            this.triEdgeNormal = triEdge1.cross(triEdge2);
        }

        /**
         * Transforms the triangle using a JOML 4x4 matrix.
         *
         * @param matrix The JOML Matrix4d representing the transformation.
         */
        public void transform(Matrix4d matrix) {
            pointA = transformPoint(pointA, matrix);
            pointB = transformPoint(pointB, matrix);
            pointC = transformPoint(pointC, matrix);
            updateEdgesAndNormal();
        }

        /**
         * Helper method to transform a single Point3d using a JOML Matrix4d.
         * The point is converted to a Vector3d, transformed using the matrix, and
         * then converted back to a Point3d.
         *
         * @param point  The original point.
         * @param matrix The transformation matrix.
         * @return A new Point3d representing the transformed point.
         */
        private Point3d transformPoint(Point3d point, Matrix4d matrix) {
            // Convert the Point3d to a JOML Vector3d
            Vector3d vec = new Vector3d(point.getX(), point.getY(), point.getZ());

            // Apply the transformation.
            // transformPosition handles homogeneous coordinates, so it will apply the
            // matrix multiplication considering an implicit 1 for the w-component.
            matrix.transformPosition(vec);

            // Return the transformed point
            return new Point3d(vec.x, vec.y, vec.z);
        }
    }

    public static class Sphere extends Surface
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

    public static class Cylinder extends Surface{

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
}
