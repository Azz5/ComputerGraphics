import java.awt.*;

public class Main {
    public static void main(String[] args) {
        Camera camera = new Camera();
        World world = new World();
        Surface.Sphere GroundSphere = new Surface.Sphere(new Vector3(0.0,-1000.0,0.0),1000,0.1, Color.RED,4,0,0);

        world.add(GroundSphere);



        RayTracer renderer = new RayTracer(800,800,4,camera,world);
        renderer.render();
    }
}
