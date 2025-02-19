public class Light {
    enum Type {
        Ambient,
        Point,
        Directional
    }
    private double intensity;
    private Vector3 position;
    private Vector3 direction;
    private Type type;

    public Light(double intensity) {
        this.type = Type.Ambient;
        this.intensity = intensity;
    }

    //point
    public Light(double intensity, Vector3 position) {
        this.type = Type.Point;
        this.position = position;
        this.intensity = intensity;
    }
    public Light(Vector3 direction, double intensity) {
        this.type = Type.Directional;
        this.direction = direction;
        this.intensity = intensity;
    }

    public double getIntensity() {
        return intensity;
    }

    public Type getType() {
        return type;
    }

    public Vector3 getDirection() {
        return direction;
    }

    public Vector3 getPosition() {
        return position;
    }
}
