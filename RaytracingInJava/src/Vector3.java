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
    public Vector3 add(Vector3 vector){
        return new Vector3(x+vector.getX(),y+vector.getY(),z+vector.getZ());
    }

    public double dot(Vector3 vector) {
        return x * vector.x + y * vector.y + z * vector.z;
    }

    public double length() {
        return Math.sqrt(this.dot(this));
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

    public Vector3 mul(double closestT) {

        return new Vector3(x * closestT , y * closestT , z * closestT);
    }

    public Vector3 div(double length) {
        return new Vector3(x / length , y / length , z / length);
    }
}
