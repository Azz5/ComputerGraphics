import java.awt.*;

public class Surface {
    private Color color;
    private Double specular;
    private Double reflective;
    private Vector3 center;


    public Surface(Color color, Double specular,Double reflective, Vector3 center) {
        this.color = color;
        this.center = center;
        this.specular = specular;
        this.reflective = reflective;
    }

    public Vector3 getCenter() {
        return center;
    }

    public Color getColor() {
        return color;
    }

    public Double getReflective() {
        return reflective;
    }

    public Double getSpecular() {
        return specular;
    }
}
