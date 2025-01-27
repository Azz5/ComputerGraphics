import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

public class Main {

    record ObjectAndT(Sphere sphereValue, Triangle triangleValue, Double doubleValue) {}

    final static int Cw = 400; // Canvas width
    final static int Ch = 400; // Canvas height
    final static Color BACKGROUND_COLOR = Color.black;

    final static Sphere[] spheres = new Sphere[] {

          //  new Sphere(new Vector3(0, -1, 3), 1.,0.,Color.RED,500.,1,0.5),
           // new Sphere(new Vector3(2, 0, 4), 1.,0.3,Color.BLUE,500.,0,0.0),
            //new Sphere(new Vector3(-2, 0, 4), 1.,0.4,Color.GREEN,100.,0,0.0),
            //new Sphere(new Vector3(0, 0, 7), 1.5,0.2,Color.WHITE,10.,0,0.0),
            new Sphere(new Vector3(0,-5001,0),5000.,0.,Color.YELLOW,1000.,0,0.0),


    };

    private static Triangle[] triangles;


    final static Light[] lights = new Light[] {
            new Light(0.2)/* This is Ambient */,
            new Light(0.6,new Vector3(2,1,0)) /* This is point */,
            new Light(new Vector3(1,4,4), 0.2) /* This is Directional */
    };

    public static void main(String[] args) {
        Matrix3 rotation= new Matrix3();
        try {
            triangles = OBJReader.readOBJFile("Data/bunny.obj").toArray(new Triangle[0]);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Camera camera = new Camera(Matrix3.createRotationZ(Math.PI/6).transform(new Vector3(0,0.5,-1)));
        BufferedImage img = new BufferedImage(Cw+ 1, Ch+ 1, BufferedImage.TYPE_INT_RGB);
        IntStream.range(0, Cw * Ch).parallel().forEach(i -> {
            int x = (i / Ch) - (Cw / 2);
            int y = (i % Ch) - (Ch / 2);
            Color color = SuperSampling(camera,x,y,4);
            putPixel(Cw, x, Ch, y, img, color);
        });
        // Save image
        try {
            ImageIO.write(img, "PNG", new File("output2.png"));
            System.out.println("Image saved.");
        } catch (IOException _) {}

        /*
        // Display image in a Swing frame (thread-safe)
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Ray Tracing Example");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(new JLabel(new ImageIcon(img)));
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });

         */
    }

    private static Color SuperSampling(Camera camera, int x, int y, int samplingFactor) {
        int red = 0;
        int green = 0;
        int blue = 0;
        int totalSamples = samplingFactor * samplingFactor;

        // Stratified sampling with jitter
        ColorSum sum = IntStream.range(0, totalSamples)
                .parallel()
                .mapToObj(i -> {
                    int dx = i / samplingFactor;
                    int dy = i % samplingFactor;

                    // Jittered sampling with ThreadLocalRandom
                    double jitterX = ThreadLocalRandom.current().nextDouble(0.5);
                    double jitterY = ThreadLocalRandom.current().nextDouble(0.5);
                    double sampleX = x + (dx + jitterX) / samplingFactor;
                    double sampleY = y + (dy + jitterY) / samplingFactor;

                    // Compute ray direction and trace
                    Vector3 direction = camera.getRotation().transform(
                            CanvasToViewport(sampleX, sampleY)
                    );
                    Color color = traceRay(camera.getPosition(), direction, 1.0, Double.MAX_VALUE, 3);

                    return new ColorSum(color.getRed(), color.getGreen(), color.getBlue());
                })
                .reduce(new ColorSum(0, 0, 0), ColorSum::add);
        // Gamma correction parameters
        final double gamma = 1 ; /* 1.0 / 2.2; // Standard sRGB gamma Not needed */
        final double invTotal = 1.0 / totalSamples;

        // Process each channel with proper gamma correction
        int avgRed = processChannel(sum.red, invTotal, gamma);
        int avgGreen = processChannel(sum.green, invTotal, gamma);
        int avgBlue = processChannel(sum.blue, invTotal, gamma);

        return new Color(clamp(avgRed), clamp(avgGreen), clamp(avgBlue));
    }

    private record ColorSum(int red, int green, int blue) {

        public ColorSum add(ColorSum other) {
                return new ColorSum(
                        this.red + other.red,
                        this.green + other.green,
                        this.blue + other.blue
                );
            }
        }
    private static int processChannel(int channelSum, double invTotal, double gamma) {
        // Normalize -> Gamma correct -> Scale to 8-bit
        double normalized = channelSum * invTotal / 255.0;
        double gammaCorrected = Math.pow(normalized, gamma);
        return (int) Math.round(gammaCorrected * 255);
    }

    // Modified CanvasToViewport to support sub-pixel precision
    private static Vector3 CanvasToViewport(double x, double y) {
        final double aspectRatio = (double)Cw / Ch;
        final double viewportWidth = 1.0;
        final double viewportHeight = 1.0 / aspectRatio;
        final double distance = 1.0;

        return new Vector3(
                x * viewportWidth / Cw,
                y * viewportHeight / Ch,
                distance
        );
    }

    private static void putPixel(int width, int x, int height, int y, BufferedImage img, Color color) {
        int px = (width / 2) + x;
        int py = (height / 2) - y;
        img.setRGB(px, py, color.getRGB());
    }

    private static Color traceRay(Vector3 cameraPosition, Vector3 d, double t_min, double t_max, int recursion_depth) {
        ObjectAndT closestIntersection = ClosestIntersection(cameraPosition, d, t_min, t_max);
        double closest_t = closestIntersection.doubleValue;
        Sphere closest_sphere = closestIntersection.sphereValue;
        Triangle closest_triangle= closestIntersection.triangleValue;

        if (closest_sphere == null && closest_triangle == null)
            return BACKGROUND_COLOR;

        if (closest_triangle != null){
            Vector3 P = cameraPosition.add(d.mul(closest_t));
            Vector3 N = P.subtract(closest_triangle.getTriEdgeNormal()).normalize();

            // Compute lighting and local color
            double lighting = ComputeLighting(P, N, d.mul(-1), closest_triangle.getSpecular());
            Color local_color = scaleColor(closest_triangle.getColor(), lighting);


            // Base case: no recursion or non-reflective/transparent object
            double reflectivity = closest_triangle.getReflective();
            double transparency = closest_triangle.getTransparency();
            if (recursion_depth <= 0 || (reflectivity <= 0 && transparency <= 0)) {
                return local_color;
            }

            Color final_color = local_color;

            // Reflection
            if (reflectivity > 0) {
                Vector3 R = ReflectRay(d.mul(-1), N);
                Color reflected_color = traceRay(P.add(N.mul(0.001)), R, 0.001, Double.MAX_VALUE, recursion_depth - 1);
                final_color = blendColors(final_color, reflected_color, reflectivity);
            }

            // Refraction (Transparency)
            if (transparency > 0) {
                double eta = closest_triangle.getRefractiveIndex();
                Vector3 refractedDir = RefractRay(d, N, eta);
                if (refractedDir != null) {
                    // Offset origin to avoid self-intersection
                    Vector3 refractedOrigin = P.add(N.mul(-0.001)); // Flip normal for exit
                    Color refracted_color = traceRay(refractedOrigin, refractedDir, 0.001, Double.MAX_VALUE, recursion_depth - 1);
                    final_color = blendColors(final_color, refracted_color, transparency);
                }
            }


            return local_color;
        }

        Vector3 P = cameraPosition.add(d.mul(closest_t));
        Vector3 N = P.subtract(closest_sphere.getCenter()).normalize();

        // Compute lighting and local color
        double lighting = ComputeLighting(P, N, d.mul(-1), closest_sphere.getSpecular());
        Color local_color = scaleColor(closest_sphere.getColor(), lighting);

        // Base case: no recursion or non-reflective/transparent object
        double reflectivity = closest_sphere.getReflective();
        double transparency = closest_sphere.getTransparency();
        if (recursion_depth <= 0 || (reflectivity <= 0 && transparency <= 0)) {
            return local_color;
        }

        Color final_color = local_color;

        // Reflection
        if (reflectivity > 0) {
            Vector3 R = ReflectRay(d.mul(-1), N);
            Color reflected_color = traceRay(P.add(N.mul(0.001)), R, 0.001, Double.MAX_VALUE, recursion_depth - 1);
            final_color = blendColors(final_color, reflected_color, reflectivity);
        }

        // Refraction (Transparency)
        if (transparency > 0) {
            double eta = closest_sphere.getRefractiveIndex();
            Vector3 refractedDir = RefractRay(d, N, eta);
            if (refractedDir != null) {
                // Offset origin to avoid self-intersection
                Vector3 refractedOrigin = P.add(N.mul(-0.001)); // Flip normal for exit
                Color refracted_color = traceRay(refractedOrigin, refractedDir, 0.001, Double.MAX_VALUE, recursion_depth - 1);
                final_color = blendColors(final_color, refracted_color, transparency);
            }
        }
        return final_color;

    }

    private static ObjectAndT ClosestIntersection(Vector3 cameraPosition, Vector3 d, double t_min, double t_max){
        // TO DO take closest T and fix triangles not very clean
        Double closest_t = Double.MAX_VALUE;
        Sphere closest_Sphere = null;
        Triangle closest_triangle = null;
        for (Sphere sphere : spheres) {
            ArrayList<Double> AllT = IntersectRaySphere(cameraPosition, d, sphere);
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
        for (Triangle triangle : triangles){
            Double t1 = IntersectTriangle(cameraPosition,d,triangle);
            if (t_min < t1 && t1 < t_max && t1 < closest_t) {
                closest_t = t1;
                closest_triangle = triangle;
            }
        }
        return new ObjectAndT(closest_Sphere,closest_triangle,closest_t);

    }

    private static Double IntersectTriangle(Vector3 cameraPosition, Vector3 d, Triangle triangle){
        Double n_dot_d = triangle.getTriEdgeNormal().dot(d);
        if (n_dot_d == 0){
            return Double.MAX_VALUE;
        }

        Double n_dot_ps = triangle.getTriEdgeNormal().dot(triangle.getPointA().subtract(cameraPosition));
        Double t = n_dot_ps / n_dot_d;

        Point3d plane_point = new Point3d(cameraPosition.add(d.mul(t)));

        Vector3 AtoPoint = plane_point.subtract(triangle.getPointA());
        Vector3 BtoPoint = plane_point.subtract(triangle.getPointB());
        Vector3 CtoPoint = plane_point.subtract(triangle.getPointC());

        Vector3 AtestVec = triangle.getAtoB_edge().cross(AtoPoint);
        Vector3 BtestVec = triangle.getBtoC_edge().cross(BtoPoint);
        Vector3 CtestVec = triangle.getCtoA_edge().cross(CtoPoint);

        Boolean AtestVec_matchesNormal = AtestVec.dot(triangle.getTriEdgeNormal())  > 0.;
        Boolean BtestVec_matchesNormal = BtestVec.dot(triangle.getTriEdgeNormal())  > 0.;
        Boolean CtestVec_matchesNormal = CtestVec.dot(triangle.getTriEdgeNormal())  > 0.;

        Boolean hitTriangle = AtestVec_matchesNormal && BtestVec_matchesNormal && CtestVec_matchesNormal;
        if (hitTriangle) {
            return t;
        }
        return Double.MAX_VALUE;
    }

    private static ArrayList<Double> IntersectRaySphere(Vector3 cameraPosition, Vector3 d, Sphere sphere) {
        ArrayList<Double> AllT = new ArrayList<>();
        double r = sphere.getRadius();
        Vector3 CO = cameraPosition.subtract(sphere.getCenter());

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

    private static Color blendColors(Color base, Color added, double coefficient) {
        int r = (int) (base.getRed() * (1 - coefficient) + added.getRed() * coefficient);
        int g = (int) (base.getGreen() * (1 - coefficient) + added.getGreen() * coefficient);
        int b = (int) (base.getBlue() * (1 - coefficient) + added.getBlue() * coefficient);
        return new Color(clamp(r), clamp(g), clamp(b));
    }

    private static Color scaleColor(Color color, double factor) {
        int r = (int) (color.getRed() * factor);
        int g = (int) (color.getGreen() * factor);
        int b = (int) (color.getBlue() * factor);
        return new Color(clamp(r), clamp(g), clamp(b));
    }

    private static int clamp(int value) {
        return Math.min(255, Math.max(0, value));
    }

    private static Vector3 RefractRay(Vector3 incident, Vector3 normal, double eta) {
        double cosTheta = Math.min(incident.dot(normal.mul(-1)), 1.0);
        if (cosTheta < 0) {
            // Ray is exiting the surface (flip normal and eta)
            normal = normal.mul(-1);
            eta = 1.0 / eta;
            cosTheta = -cosTheta;
        }

        double sinTheta = Math.sqrt(1.0 - cosTheta * cosTheta);
        if (eta * sinTheta > 1.0) {
            // Total internal reflection (no refraction)
            return null;
        }

        Vector3 refractedPerp = incident.add(normal.mul(cosTheta)).mul(eta);
        Vector3 refractedParallel = normal.mul(-Math.sqrt(1.0 - refractedPerp.dot(refractedPerp)));
        return refractedPerp.add(refractedParallel).normalize();
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
                ObjectAndT shadowChecks = ClosestIntersection(P,L,0.001, t_max);
                //shadow_t = shadowChecks.doubleValue;
                shadow_sphere = shadowChecks.sphereValue;
                Triangle shadow_triangle = shadowChecks.triangleValue;
                if (shadow_sphere != null || shadow_triangle != null) {
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