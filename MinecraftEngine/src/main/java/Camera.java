import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3i;
import static org.lwjgl.glfw.GLFW.*;

public class Camera {
    private final Vector3f pos = new Vector3f(8f, 8f, 40f);
    private float pitch = -30f;
    private float yaw = 0f;
    private final Vector3f velocity = new Vector3f();
    private final Matrix4f projection;
    private final Matrix4f view = new Matrix4f();
    private double lastX, lastY;
    private static final float ACCEL = 0.1f;
    private static final float FRICTION = 0.9f;
    private static final float MOUSE_SENS = 0.1f;

    public Camera() {
        projection = new Matrix4f()
                .perspective((float)Math.toRadians(70f), Main.window.getAspect(), 0.1f, 1000f);
        lastX = InputHandler.getMouseX();
        lastY = InputHandler.getMouseY();
    }

    public void update() {
        // --- Mouse look ---
        double mouseX = InputHandler.getMouseX();
        double mouseY = InputHandler.getMouseY();
        float dx = (float)(mouseX - lastX) * MOUSE_SENS;
        float dy = (float)(mouseY - lastY) * MOUSE_SENS;
        yaw += dx;
        pitch = Math.max(-89f, Math.min(89f, pitch + dy));
        lastX = mouseX;
        lastY = mouseY;

        // --- Camera axes in world space ---
        Matrix4f rotation = new Matrix4f()
                .rotateY((float)Math.toRadians(yaw))
                .rotateX((float)Math.toRadians(pitch));

        // Forward = camera space -Z
        Vector3f forward = new Vector3f(0, 0, -1);
        rotation.transformDirection(forward).normalize();

        // Right = camera space +X
        Vector3f right = new Vector3f(-1, 0, 0);
        rotation.transformDirection(right).normalize();

        // Up = camera space +Y (optional for completeness)
        Vector3f up = new Vector3f(0, -1, 0);
        rotation.transformDirection(up).normalize();

        // --- Movement input in camera space ---
        if (InputHandler.isKeyPressed(GLFW_KEY_W)) velocity.add(new Vector3f(forward).mul(ACCEL));
        if (InputHandler.isKeyPressed(GLFW_KEY_S)) velocity.sub(new Vector3f(forward).mul(ACCEL));
        if (InputHandler.isKeyPressed(GLFW_KEY_A)) velocity.sub(new Vector3f(right).mul(ACCEL));
        if (InputHandler.isKeyPressed(GLFW_KEY_D)) velocity.add(new Vector3f(right).mul(ACCEL));
        if (InputHandler.isKeyPressed(GLFW_KEY_SPACE)) velocity.add(new Vector3f(up).mul(ACCEL));
        if (InputHandler.isKeyPressed(GLFW_KEY_LEFT_SHIFT)) velocity.sub(new Vector3f(up).mul(ACCEL));

        // Apply friction and move
        velocity.mul(FRICTION);
        pos.add(velocity);

        // --- Update view matrix ---
        view.identity()
                .lookAt(pos,
                        new Vector3f(pos).add(forward),
                        up);
    }

    public Matrix4f getProjectionMatrix() {
        return projection;
    }

    public Vector3f getPos() {
        return pos;
    }

    public float getPitch() {
        return pitch;
    }

    public float getYaw() {
        return yaw;
    }

    public Vector3f getVelocity() {
        return velocity;
    }

    public Matrix4f getProjection() {
        return projection;
    }

    public Matrix4f getView() {
        return view;
    }

    public double getLastX() {
        return lastX;
    }

    public double getLastY() {
        return lastY;
    }

    public Matrix4f getViewMatrix() {
        return view;
    }

    /**
     * Returns the block coordinate the camera is looking at (for removal).
     */
    public Vector3i getPickPosition(float maxDistance, Chunk chunk) {
        Vector3f dir = new Vector3f(0, 0, -1);
        new Matrix4f()
                .rotateY((float)Math.toRadians(yaw))
                .rotateX((float)Math.toRadians(pitch))
                .transformDirection(dir)
                .normalize();

        Vector3f current = new Vector3f(pos);
        for (int i = 0; i < (int)maxDistance; i++) {
            current.add(dir);
            int bx = (int)Math.floor(current.x);
            int by = (int)Math.floor(current.y);
            int bz = (int)Math.floor(current.z);
            // If this block is solid (not air), we've hit it
            if (!chunk.isAir(bx, by, bz)) {
                return new Vector3i(bx, by, bz);
            }
        }
        // Nothing hit within reach
        return null;
    }    /**
     * Returns the adjacent block coordinate for placement.
     */
    public Vector3i getPlacePosition(float maxDistance, Chunk chunk) {
        Vector3i hit = getPickPosition(maxDistance, chunk);
        if (hit == null) return null;

        Vector3f dir = new Vector3f(0, 0, -1);
        new Matrix4f()
                .rotateY((float)Math.toRadians(yaw))
                .rotateX((float)Math.toRadians(pitch))
                .transformDirection(dir)
                .normalize();

        int dx = (int)Math.signum(dir.x);
        int dy = (int)Math.signum(dir.y);
        int dz = (int)Math.signum(dir.z);
        return new Vector3i(hit.x + dx, hit.y + dy, hit.z + dz);
    }

    public Vector3f getPosition() {
        return pos;
    }
}
