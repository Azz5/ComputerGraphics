import java.awt.*;

public class RayCanvas extends Canvas {
    private int width;
    private int height;
    private Color color;
    private Color[][] pixels;

    public RayCanvas(int width, int height,Color color) {
        this.width = width;
        this.height = height;
        this.pixels = new Color[width][height];
        this.color = color;
        setSize(width, height);
        setMaximumSize(new Dimension(width,height));
        setMinimumSize(new Dimension(width,height));
        setBackground(color);
    }

    public void setPixel(int x, int y, Color color) {
        // Transformation to [0, 0]
        int px = (width / 2) + x;
        int py = (height / 2) - y;

        // Update the pixel array
        if (px >= 0 && px < width && py >= 0 && py < height) {
            pixels[px][py] = color;


        }
    }
    @Override
    public void paint(Graphics g) {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (pixels[x][y] != null) {
                    g.setColor(pixels[x][y]);
                    g.drawOval(x, y, 1, 1);
                }
            }
        }
    }

}
