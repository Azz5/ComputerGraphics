import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

public class Main {

    record SphereAndClosestT(Sphere sphereValue, Double doubleValue) {}

    final static int Cw = 500; // Canvas width
    final static int Ch = 500; // Canvas height
    final static Color BACKGROUND_COLOR = Color.BLACK;

    final static Sphere[] spheres = new Sphere[] {
            new Sphere(new Vector3(0, -1, 3), 1,0.2,Color.RED,500),
            new Sphere(new Vector3(2, 0, 4), 1,0.3,Color.BLUE,500),
            new Sphere(new Vector3(-2, 0, 4), 1,0.4,Color.GREEN,10),
            new Sphere(new Vector3(0,-5001,0),5000,0.5,Color.YELLOW,1000),
    };

    final static Light[] lights = new Light[] {
            new Light(0.2)/* This is Ambient */,
            new Light(0.6,new Vector3(2,1,0)) /* This is point */,
            new Light(new Vector3(1,4,4), 0.2) /* This is Directional */
    };

    public static void main(String[] args) {
        Vector3 origin = new Vector3(0, 0, 0);
        int width = Cw; // Ensure Cw/Ch are defined as even integers
        int height = Ch;
        BufferedImage img = new BufferedImage(width+1, height + 1, BufferedImage.TYPE_INT_ARGB);

        // Ray tracing loop
        for (int x = -width / 2; x < width / 2; x++) {
            for (int y = -height / 2; y < height / 2; y++) {
                Vector3 direction = CanvasToViewport(x, y);
                Color color = traceRay(origin, direction, 1.0, Double.MAX_VALUE,3);
                int px = (width / 2) + x;
                int py = (height / 2) - y;
                img.setRGB(px, py, color.getRGB());
            }
        }

        // Save image
        try {
            ImageIO.write(img, "PNG", new File("output.png"));
            System.out.println("Image saved.");
        } catch (IOException _) {
        }

        // Display image in a Swing frame (thread-safe)
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Ray Tracing Example");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(new JLabel(new ImageIcon(img)));
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    private static Color traceRay(Vector3 origin, Vector3 d, double t_min, double t_max, int recursion_depth) {
        SphereAndClosestT closestIntersection = ClosestIntersection(origin,d,t_min,t_max);

        double closest_t = closestIntersection.doubleValue;
        Sphere closest_Sphere = closestIntersection.sphereValue;

        if (closest_Sphere == null) {
            return BACKGROUND_COLOR;
        }

        Vector3 P = origin.add(d.mul(closest_t));
        Vector3 N = P.subtract(closest_Sphere.getCenter());
        N = N.div(N.length());

        double lighting = ComputeLighting(P,N,d.mul(-1),closest_Sphere.getSpecular());
        double ColorR = Math.min(255,closest_Sphere.getColor().getRed() * lighting);
        double ColorG = Math.min(255,closest_Sphere.getColor().getGreen() * lighting);
        double ColorB = Math.min(255,closest_Sphere.getColor().getBlue() * lighting);

        Color local_color = new Color((int) ColorR, (int) ColorG, (int) ColorB);

        // If we hit the  recursion limit or the object is not reflective we are done.
        double r = closest_Sphere.getReflective();
        if (recursion_depth <= 0 || r <= 0){
            return local_color;
        }

        Vector3 R = ReflectRay(d.mul(-1),N);
        Color reflectedColor = traceRay(P,R,0.001,Double.MAX_VALUE,recursion_depth-1);
        ColorR = Math.min(255,reflectedColor.getRed() * r + (1 - r) * local_color.getRed());
        ColorG = Math.min(255,reflectedColor.getGreen() * r + (1 - r) * local_color.getGreen() );
        ColorB = Math.min(255,reflectedColor.getBlue() * r + (1 - r) * local_color.getBlue());

        return new Color((int) ColorR, (int) ColorG, (int) ColorB);

    }

    private static SphereAndClosestT ClosestIntersection(Vector3 origin, Vector3 d, double t_min, double t_max){
        Double closest_t = Double.MAX_VALUE;
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

        return new SphereAndClosestT(closest_Sphere,closest_t);

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

    private static Double ComputeLighting(Vector3 P, Vector3 N,Vector3 V, double s) {
        Double i = 0.0;
        Double t_max;
        Double shadow_t;
        Sphere shadow_sphere;
        Vector3 L;
        for (Light light : lights){
            if (light.getType() == Light.Type.Ambient) {
                i += light.getIntensity();
            } else {
                if (light.getType() == Light.Type.Point) {
                    L = light.getPosition().subtract(P);
                    t_max = 1.0;
                } else {
                    L = light.getDirection();
                    t_max = Double.MAX_VALUE;
                }

                // Shadow Check
                SphereAndClosestT shadowChecks = ClosestIntersection(P,L,0.001, t_max);
                shadow_t = shadowChecks.doubleValue;
                shadow_sphere = shadowChecks.sphereValue;
                if (shadow_sphere != null) {
                    continue;
                }

                // Diffuse
                double n_dot_l = N.dot(L);
                if (n_dot_l > 0) {
                    i += light.getIntensity() * n_dot_l/(N.length() * L.length());
                }

                if (s != -1) {
                    Vector3 R = ReflectRay(L,N);
                    double r_dot_v = R.dot(V);
                    if (r_dot_v > 0) {
                        i += light.getIntensity() * Math.pow(r_dot_v/(R.length() * V.length()),s);
                    }
                }
            }
        }
        return i;
    }


    private static Vector3 ReflectRay(Vector3 L, Vector3 N){
        return N.mul(2).mul(N.dot(L)).subtract(L);
    }


}