import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;

public class Window {
    protected static long window;

    private int width;
    private int height;

    public Window(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public float getAspect() {
        return ((float) width) / (float) height;
    }

    public void create(String title) {
        // Setup error callback
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW
        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");

        // Reset to default hints
        glfwDefaultWindowHints();

        // Window hints for OpenGL 3.3 core
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        // Forward compatibility for macOS, harmless on Windows
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);

        // Other window hints
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        // Create window
        window = glfwCreateWindow(width, height, title, NULL, NULL);
        if (window == NULL)
            throw new RuntimeException("Failed to create the GLFW window");

        // Center on screen
        GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        glfwSetWindowPos(window,
                (vidmode.width() - width) / 2,
                (vidmode.height() - height) / 2);

        // Make context current
        glfwMakeContextCurrent(window);
        // Enable v-sync
        glfwSwapInterval(1);
        // Show window
        glfwShowWindow(window);

        // Create OpenGL capabilities after context creation
        GL.createCapabilities();

        // Basic OpenGL state
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LEQUAL);
        glClearColor(0.5f, 0.8f, 1.0f, 1.0f);

        // Initial viewport
        glViewport(0, 0, width, height);
        // Adjust viewport on resize
        glfwSetFramebufferSizeCallback(window, (win, w, h) -> glViewport(0, 0, w, h));

        InputHandler.init(window);
    }

    public static boolean shouldClose() { return glfwWindowShouldClose(window); }
    public static void pollEvents()   { glfwPollEvents(); }
    public static void swapBuffers()  { glfwSwapBuffers(window); }
    public static long getWindow()    { return window; }
    public void destroy() {
        InputHandler.cleanup();
        glfwDestroyWindow(window);
        glfwTerminate();
        GLFWErrorCallback callback = GLFWErrorCallback.createPrint(System.err);
        if (callback != null) callback.free();
    }

    public int getWidth()  { return width; }
    public int getHeight() { return height; }
}
