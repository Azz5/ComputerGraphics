import java.awt.*;
import java.util.ArrayList;

public class Main {
    final static int Cw = 300; // Canvas width
    final static int Ch = 300; // Canvas height
    final static Color BACKGROUND_COLOR = Color.WHITE;

    final static Sphere[] spheres = new Sphere[] {
            new Sphere(new Vector3(0, -1, 3), 1, Color.RED),
            new Sphere(new Vector3(2, 0, 4), 1, Color.BLUE),
            new Sphere(new Vector3(-2, 0, 4), 1, Color.GREEN),
    };

    public static void main(String[] args) {
        Vector3 origin = new Vector3(0, 0, 0);
        RayCanvas canvas = new RayCanvas(Cw, Ch, BACKGROUND_COLOR);

        // Trace rays and set pixels
        for (int x = -Cw / 2; x < Cw / 2; x++) {
            for (int y = -Ch / 2; y < Ch / 2; y++) {
                Vector3 directionVector = CanvasToViewport(x, y);
                Color color = traceRay(origin, directionVector, 1.0, Double.MAX_VALUE);
                canvas.setPixel(x, y, color);
            }
        }

        // Create a frame to display the canvas
        Frame frame = new Frame("RayCanvas Example");
        frame.add(canvas);
        frame.setSize(Cw, Ch);
        frame.setVisible(true);

        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                System.exit(0);
            }
        });
    }

    private static Color traceRay(Vector3 origin, Vector3 d, double t_min, double t_max) {
        double closest_t = Double.MAX_VALUE;
        Sphere closest_Sphere = null;

        for (Sphere sphere : spheres) {
            ArrayList<Double> AllT = IntersectRaySphere(origin, d, sphere);
            double t1 = AllT.get(0);
            double t2 = AllT.get(1);

            if (t_min < t1 && t1 < t_max && t1 < closest_t) {
                closest_t = t1;
                closest_Sphere = sphere;
            }
            if (t_min < t2 && t2 < t_max && t2 < closest_t) {
                closest_t = t2;
                closest_Sphere = sphere;
            }
        }

        if (closest_Sphere == null) {
            return BACKGROUND_COLOR;
        }
        return closest_Sphere.getColor();
    }

    private static ArrayList<Double> IntersectRaySphere(Vector3 origin, Vector3 d, Sphere sphere) {
        ArrayList<Double> AllT = new ArrayList<>();
        double r = sphere.getRadius();
        Vector3 CO = origin.subtract(sphere.getCenter());

        double a = d.dot(d);
        double b = 2 * CO.dot(d); // Note the factor of 2
        double c = CO.dot(CO) - r * r;

        double discriminant = b * b - 4 * a * c;

        if (discriminant < 0) {
            AllT.add(Double.MAX_VALUE);
            AllT.add(Double.MAX_VALUE);
            return AllT;
        }

        double sqrtDisc = Math.sqrt(discriminant);
        AllT.add((-b + sqrtDisc) / (2 * a));
        AllT.add((-b - sqrtDisc) / (2 * a));

        return AllT;
    }

    private static Vector3 CanvasToViewport(int x, int y) {
        double Vw = 1.0; // Viewport width in 3D space
        double Vh = 1.0; // Viewport height in 3D space
        double d = 1.0;  // Distance from the canvas to the viewport

        return new Vector3((x * Vw) / Cw, (y * Vh) / Ch, d);
    }
}
