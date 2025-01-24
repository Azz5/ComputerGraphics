import java.awt.*;

public class Surface {
    private Color color;
    Double specular;
    Vector3 center;

    public Surface(Color color, Double specular, Vector3 center) {
        this.color = color;
        this.center = center;
        this.specular = specular;
    }
}
