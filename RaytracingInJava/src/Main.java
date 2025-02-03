import org.joml.Matrix4d;
import org.joml.Vector4d;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

public class Main {

    record ObjectAndT(Sphere sphereValue, Triangle triangleValue, Cylinder cylinderValue ,Double doubleValue) {}

    final static int Cw = 800; // Canvas width
    final static int Ch = 800; // Canvas height
    final static Color BACKGROUND_COLOR = Color.black;

    final static Sphere[] spheres = new Sphere[] {

            new Sphere(new Vector3(2, 0, 5), 1.,0.5,Color.RED,500.,1,0.),
            //new Sphere(new Vector3(0, 0, 7), 1.5,0.2,Color.WHITE,10.,0,0.0),


    };

    private static Triangle[] triangles = new Triangle[]{

    };

    private static Cylinder[] cylinders = new Cylinder[] {
            new Cylinder(new Vector3(0.,0.,3.),1.,1.,Color.YELLOW,10,0,0,0.0)
    };


    final static Light[] lights = new Light[] {
            new Light(0.1)/* This is Ambient */,
            new Light(0.6,new Vector3(2,1,0)) /* This is point */,
            new Light(new Vector3(1,4,4), 0.2) /* This is Directional */
    };

    public static void main(String[] args) {

        // Rabbit reading
        try {
            triangles = OBJReader.readOBJFile("Data/bunny.obj").toArray(new Triangle[0]);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }






        long startTime = System.nanoTime(); // Capture start time
        Matrix4d Rotation = new Matrix4d().translate(
                0,0,-5
        );
        Camera camera = new Camera(new Vector3(0,0,-2));
        BufferedImage img = new BufferedImage(Cw+ 1, Ch+ 1, BufferedImage.TYPE_INT_RGB);
        IntStream.range(-Cw/2, Cw/2)
                .parallel()  // process x values in parallel
                .forEach(x -> {
                    IntStream.range(-Ch/2, Ch/2)
                            .forEach(y -> {
                                Color color = SuperSampling(camera, x, y, 4);
                                putPixel(x, y, img, color);
                            });
                });


        long endTime = System.nanoTime(); // Capture end time
        long duration = (endTime - startTime); // Calculate duration in nanoseconds
        double durationInSeconds = duration / 1_000_000_000.0; // Convert to seconds

        System.out.println("Time taken: " + durationInSeconds + " seconds");
        // Save image
        try {
            ImageIO.write(img, "PNG", new File("output3.png"));
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

        for (int dx = 0; dx < samplingFactor; dx++) {
            for (int dy = 0; dy < samplingFactor; dy++) {
                // Jittered sampling with ThreadLocalRandom
                double jitterX = ThreadLocalRandom.current().nextDouble(0.5);
                double jitterY = ThreadLocalRandom.current().nextDouble(0.5);
                double sampleX = x + (dx + jitterX) / samplingFactor;
                double sampleY = y + (dy + jitterY) / samplingFactor;

                // Compute ray direction and trace
                Vector4d data  = camera.getRotation().transform(
                        CanvasToViewport(sampleX, sampleY)
                );
                Vector3 direction = new Vector3(data);
                Color color = traceRay(camera.getPosition(), direction, 1.0, Double.MAX_VALUE, 3);

                // Accumulate the color values
                red += color.getRed();
                green += color.getGreen();
                blue += color.getBlue();
            }
        }
        // Gamma correction parameters
        final double invTotal = 1.0 / totalSamples;

        // Process each channel with proper gamma correction
        int avgRed = processChannel(red, invTotal);
        int avgGreen = processChannel(green, invTotal);
        int avgBlue = processChannel(blue, invTotal);

        return new Color(clamp(avgRed), clamp(avgGreen), clamp(avgBlue));
    }

    private static int processChannel(int channelSum, double invTotal) {
        // Normalize -> Gamma correct -> Scale to 8-bit
        double normalized = channelSum * invTotal / 255.0;
        return (int) Math.round(normalized * 255);
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

    private static void putPixel(int x, int y, BufferedImage img, Color color) {
        int px = (Main.Cw / 2) + x;
        int py = (Main.Ch / 2) - y;
        img.setRGB(px, py, color.getRGB());
    }

    private static Color traceRay(Vector3 cameraPosition, Vector3 d, double t_min, double t_max, int recursion_depth) {
        ObjectAndT closestIntersection = ClosestIntersection(cameraPosition, d, t_min, t_max);
        double closest_t = closestIntersection.doubleValue;
        Sphere closest_sphere = closestIntersection.sphereValue;
        Cylinder closest_cylinder = closestIntersection.cylinderValue;
        Triangle closest_triangle= closestIntersection.triangleValue;

        if (closest_sphere == null && closest_triangle == null && closest_cylinder == null)
            return BACKGROUND_COLOR;
        if (closest_cylinder != null) {
            Vector3 P = cameraPosition.add(d.mul(closest_t));
            Vector3 N = P.subtract(closest_cylinder.getCenter()).normalize();

            // Compute lighting and local color
            double lighting = ComputeLighting(P, N, d.mul(-1), closest_cylinder.getSpecular());
            Color local_color = scaleColor(closest_cylinder.getColor(), lighting);


            // Base case: no recursion or non-reflective/transparent object
            double reflectivity = closest_cylinder.getReflective();
            double transparency = closest_cylinder.getTransparency();
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
                double eta = closest_cylinder.getRefractiveIndex();
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


            return final_color;
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
    private static ObjectAndT ClosestIntersection(Vector3 cameraPosition, Vector3 d, double t_min, double t_max) {
        double closest_t = Double.MAX_VALUE;
        Sphere closest_Sphere = null;
        Triangle closest_triangle = null;
        Cylinder closest_Cylinder = null;

        // Check intersections with spheres
        for (Sphere sphere : spheres) {
            ArrayList<Double> allT = IntersectRaySphere(cameraPosition, d, sphere);
            double t1 = allT.get(0);
            double t2 = allT.get(1);

            if (t_min < t1 && t1 < t_max && t1 < closest_t) {
                closest_t = t1;
                closest_Sphere = sphere;
                closest_triangle = null;
                closest_Cylinder = null;
            }
            if (t_min < t2 && t2 < t_max && t2 < closest_t) {
                closest_t = t2;
                closest_Sphere = sphere;
                closest_triangle = null;
                closest_Cylinder = null;
            }
        }

        // Check intersections with triangles
        for (Triangle triangle : triangles) {
            Double t = IntersectTriangle(cameraPosition, d, triangle);
            if (t != null && t_min < t && t < t_max && t < closest_t) {
                closest_t = t;
                closest_triangle = triangle;
                closest_Sphere = null;
                closest_Cylinder = null;
            }
        }

        // Check intersections with cylinders
        for (Cylinder cylinder : cylinders) {
            ArrayList<Double> allT = IntersectRayCylinder(cameraPosition, d, cylinder);
            double t1 = allT.get(0);
            double t2 = allT.get(1);
            if (t_min < t1 && t1 < t_max && t1 < closest_t) {
                closest_t = t1;
                closest_Cylinder = cylinder;
                closest_Sphere = null;
                closest_triangle = null;
            }
            if (t_min < t2 && t2 < t_max && t2 < closest_t) {
                closest_t = t2;
                closest_Cylinder = cylinder;
                closest_Sphere = null;
                closest_triangle = null;
            }
        }

        return new ObjectAndT(closest_Sphere, closest_triangle, closest_Cylinder, closest_t);
    }

    public static ArrayList<Double> IntersectRayCylinder(Vector3 rayOrigin, Vector3 rayDir, Cylinder cylinder) {
        ArrayList<Double> tCandidates = new ArrayList<>();

        double radius = cylinder.getRadius();
        double height = cylinder.getHeight();
        Vector3 center = cylinder.getCenter();

        // Extract coordinates for clarity.
        double cx = center.getX();
        double cy = center.getY();
        double cz = center.getZ();

        double ox = rayOrigin.getX();
        double oy = rayOrigin.getY();
        double oz = rayOrigin.getZ();

        double dx = rayDir.getX();
        double dy = rayDir.getY();
        double dz = rayDir.getZ();

        // Define the y-boundaries of the finite cylinder.
        double yTop = cy + height / 2.0;
        double yBottom = cy - height / 2.0;

        // ===========================================================
        // 1. Intersect with the infinite cylinder’s lateral surface.
        //
        // For a cylinder aligned along the y-axis, the equation is:
        //   (x - cx)^2 + (z - cz)^2 = radius^2
        // where the valid y coordinates are between yBottom and yTop.
        // ===========================================================
        double A = dx * dx + dz * dz;
        double B = 2 * ((ox - cx) * dx + (oz - cz) * dz);
        double Ccoef = (ox - cx) * (ox - cx) + (oz - cz) * (oz - cz) - radius * radius;

        // Only solve the quadratic if A is not nearly zero (i.e. ray not parallel to the cylinder’s side).
        if (Math.abs(A) > 1e-6) {
            double discriminant = B * B - 4 * A * Ccoef;
            if (discriminant >= 0) {
                double sqrtDisc = Math.sqrt(discriminant);
                double t1 = (-B - sqrtDisc) / (2 * A);
                double t2 = (-B + sqrtDisc) / (2 * A);

                // For each solution, check that the y coordinate lies within the cylinder’s height.
                double y1 = oy + t1 * dy;
                if (y1 >= yBottom && y1 <= yTop) {
                    tCandidates.add(t1);
                }

                double y2 = oy + t2 * dy;
                if (y2 >= yBottom && y2 <= yTop) {
                    tCandidates.add(t2);
                }
            }
        }

        // ===========================================================
        // 2. Intersect with the top and bottom caps.
        // Each cap is a horizontal plane (y = yTop and y = yBottom).
        // For each cap, if the ray is not parallel (dy not 0), compute t and check
        // whether the (x,z) coordinates of the intersection lie within the circle.
        // ===========================================================
        if (Math.abs(dy) > 1e-6) {
            // Top cap at y = yTop.
            double tTop = (yTop - oy) / dy;
            double xTop = ox + tTop * dx;
            double zTop = oz + tTop * dz;
            if ((xTop - cx) * (xTop - cx) + (zTop - cz) * (zTop - cz) <= radius * radius) {
                tCandidates.add(tTop);
            }

            // Bottom cap at y = yBottom.
            double tBottom = (yBottom - oy) / dy;
            double xBottom = ox + tBottom * dx;
            double zBottom = oz + tBottom * dz;
            if ((xBottom - cx) * (xBottom - cx) + (zBottom - cz) * (zBottom - cz) <= radius * radius) {
                tCandidates.add(tBottom);
            }
        }

        // ===========================================================
        // 3. Sort the candidate t-values in ascending order.
        // ===========================================================
        Collections.sort(tCandidates);

        // ===========================================================
        // 4. Prepare the result.
        // We mimic the sphere intersection method by returning exactly two values.
        // If fewer than two intersections were found, fill in with Double.POSITIVE_INFINITY.
        // ===========================================================
        ArrayList<Double> result = new ArrayList<>();
        if (tCandidates.size() >= 2) {
            result.add(tCandidates.get(0));
            result.add(tCandidates.get(1));
        } else if (tCandidates.size() == 1) {
            result.add(tCandidates.get(0));
            result.add(Double.POSITIVE_INFINITY);
        } else {
            result.add(Double.POSITIVE_INFINITY);
            result.add(Double.POSITIVE_INFINITY);
        }

        return result;
    }

    private static Double IntersectTriangle(Vector3 cameraPosition, Vector3 d, Triangle triangle){
        double n_dot_d = triangle.getTriEdgeNormal().dot(d);
        if (n_dot_d == 0){
            return Double.MAX_VALUE;
        }

        double n_dot_ps = triangle.getTriEdgeNormal().dot(triangle.getPointA().subtract(cameraPosition));
        double t = n_dot_ps / n_dot_d;

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

        boolean hitTriangle = AtestVec_matchesNormal && BtestVec_matchesNormal && CtestVec_matchesNormal;
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
        double i = 0.0;
        double t_max;
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