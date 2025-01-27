import java.awt.*;

public class Surface {
    private Color color;
    private double specular;
    private double reflective;
    private double refractiveIndex; // e.g., 1.0 for air, 1.5 for glass
    private double transparency;
    public Surface(Color color, double specular,double reflective,double refraction_index, double transparency) {
        this.color = color;
        this.specular = specular;
        this.reflective = reflective;
        this.refractiveIndex = refraction_index;
        this.transparency = transparency;
    }


    public double getRefractiveIndex() { return refractiveIndex; }
    public double getTransparency() { return transparency; }

    public Color getColor() {
        return color;
    }

    public double getReflective() {
        return reflective;
    }

    public double getSpecular() {
        return specular;
    }
}
