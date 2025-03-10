public class Vector3 {
    private final double x;
    private final double y;
    private final double z;

    public Vector3(double x,double y,double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3 subtract(Vector3 vector){
        return new Vector3(x-vector.getX(),y-vector.getY(),z-vector.getZ());
    }

    public double dot(Vector3 vector) {
        return x * vector.x + y * vector.y + z * vector.z;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }
}
