import java.awt.*;

public class Triangle extends Surface{
    private Point3d pointA;
    private Point3d pointB;
    private Point3d pointC;
    private Vector3 AtoB_edge;
    private Vector3 BtoC_edge;
    private Vector3 CtoA_edge;
    private Vector3 triEdgeNormal;


    public Triangle(Color color, double specular, double reflective, double refraction_index, double transparency, Point3d pointA, Point3d pointB, Point3d pointC) {
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

        // Update edges and normal after scaling
        updateEdgesAndNormal();
    }

    // Helper method to scale a single point
    private Point3d scalePoint(Point3d point, double sx, double sy, double sz) {
        double x = point.getX() * sx;
        double y = point.getY() * sy;
        double z = point.getZ() * sz;
        return new Point3d(x, y, z);
    }

    // Update edges and normal after scaling
    private void updateEdgesAndNormal() {
        this.AtoB_edge = pointB.subtract(pointA);
        this.BtoC_edge = pointC.subtract(pointB);
        this.CtoA_edge = pointA.subtract(pointC);
        Vector3 triEdge1 = pointB.subtract(pointA);
        Vector3 triEdge2 = pointC.subtract(pointA);
        this.triEdgeNormal = triEdge1.cross(triEdge2);
    }
}


