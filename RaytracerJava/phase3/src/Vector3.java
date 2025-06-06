import java.util.Objects;

public class Vector3 {
    private final double x;
    private final double y;
    private final double z;

    // Static constants for common vectors
    public static final Vector3 ZERO = new Vector3(0, 0, 0);
    public static final Vector3 ONE = new Vector3(1, 1, 1);
    public static final Vector3 UP = new Vector3(0, 1, 0);
    public static final Vector3 RIGHT = new Vector3(1, 0, 0);
    public static final Vector3 FORWARD = new Vector3(0, 0, 1);

    public Vector3(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    // ===== Core Vector Operations =====
    public Vector3 subtract(Vector3 vector) {
        return new Vector3(x - vector.x, y - vector.y, z - vector.z);
    }

    public Vector3 add(Vector3 vector) {
        return new Vector3(x + vector.x, y + vector.y, z + vector.z);
    }

    public double dot(Vector3 vector) {
        return x * vector.x + y * vector.y + z * vector.z;
    }

    public Vector3 cross(Vector3 other) {
        return new Vector3(
                y * other.z - z * other.y,
                z * other.x - x * other.z,
                x * other.y - y * other.x
        );
    }

    public double length() {
        return Math.sqrt(this.dot(this));
    }

    public Vector3 normalize() {
        double len = length();
        if (len == 0) throw new ArithmeticException("Cannot normalize a zero vector");
        return this.div(len);
    }

    // ===== Scalar Operations =====
    public Vector3 mul(double scalar) {
        return new Vector3(x * scalar, y * scalar, z * scalar);
    }

    public Vector3 div(double scalar) {
        if (scalar == 0) throw new ArithmeticException("Division by zero");
        return new Vector3(x / scalar, y / scalar, z / scalar);
    }

    public Vector3 negate() {
        return this.mul(-1);
    }

    // ===== Utility Methods =====
    public double distanceTo(Vector3 other) {
        return this.subtract(other).length();
    }

    public double[] toArray() {
        return new double[]{x, y, z};
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Vector3 vector = (Vector3) obj;
        return Double.compare(vector.x, x) == 0 &&
                Double.compare(vector.y, y) == 0 &&
                Double.compare(vector.z, z) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }

    @Override
    public String toString() {
        return String.format("Vector3(%.2f, %.2f, %.2f)", x, y, z);
    }

    // ===== Getters =====
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