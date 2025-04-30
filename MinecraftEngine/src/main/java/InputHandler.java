// InputHandler.java

import static org.lwjgl.glfw.GLFW.*;

public class InputHandler {
    private static long window;
    private static final boolean[] keys    = new boolean[GLFW_KEY_LAST+1];
    private static final boolean[] buttons = new boolean[GLFW_MOUSE_BUTTON_LAST+1];
    private static double mouseX, mouseY;

    public static void init(long win) {
        window = win;
        glfwSetKeyCallback(win,    (w,k,s,a,m)-> keys[k]    = a != GLFW_RELEASE);
        glfwSetMouseButtonCallback(win, (w,b,a,m)-> buttons[b] = a != GLFW_RELEASE);
        glfwSetCursorPosCallback(win,(w,x,y)-> { mouseX = x; mouseY = y; });
        glfwSetInputMode(win, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
    }

    public static boolean isKeyPressed(int key)      { return keys[key]; }
    public static boolean isMouseButtonPressed(int b){ return buttons[b]; }
    public static double getMouseX()                { return mouseX; }
    public static double getMouseY()                { return mouseY; }
    public static void cleanup() {}
}