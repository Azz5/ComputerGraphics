import org.joml.Matrix4d;

public class Camera {
    private Vector3 position;
    private Matrix4d rotation; // Represent rotation as a 3x3 matrix

    // Default constructor: position at (0,0,0), rotation as identity matrix (no rotation)
    public Camera() {
        this.position = new Vector3(0, 0, 0);
        this.rotation = new Matrix4d(); // Assume Matrix3 has an identity() method
    }

    // Constructor with position and default rotation
    public Camera(Vector3 position) {
        this.position = position;
        this.rotation = new Matrix4d(); // Assume Matrix3 has an identity() method
    }

    // Constructor with both position and rotation
    public Camera(Vector3 position, Matrix4d rotation) {
        this.position = position;
        this.rotation = rotation;
    }

    // Return the rotation matrix (not a Double!)
    public Matrix4d getRotation() {
        return rotation;
    }

    // Setter for rotation (optional)
    public void setRotation(Matrix4d rotation) {
        this.rotation = rotation;
    }

    // Getter for position
    public Vector3 getPosition() {
        return position;
    }
}