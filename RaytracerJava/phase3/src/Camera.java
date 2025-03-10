public class Camera {
    private Vector3 position;
    private Matrix3 rotation; // Represent rotation as a 3x3 matrix

    // Default constructor: position at (0,0,0), rotation as identity matrix (no rotation)
    public Camera() {
        this.position = new Vector3(0, 0, 0);
        this.rotation = Matrix3.identity(); // Assume Matrix3 has an identity() method
    }

    // Constructor with position and default rotation
    public Camera(Vector3 position) {
        this.position = position;
        this.rotation = Matrix3.identity();
    }

    // Constructor with both position and rotation
    public Camera(Vector3 position, Matrix3 rotation) {
        this.position = position;
        this.rotation = rotation;
    }

    // Return the rotation matrix (not a Double!)
    public Matrix3 getRotation() {
        return rotation;
    }

    // Setter for rotation (optional)
    public void setRotation(Matrix3 rotation) {
        this.rotation = rotation;
    }

    // Getter for position
    public Vector3 getPosition() {
        return position;
    }
}