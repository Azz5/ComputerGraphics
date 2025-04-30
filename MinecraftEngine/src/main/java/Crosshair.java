
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL30.*;

public class Crosshair {
    private final int vao, vbo;
    public Crosshair() {
        float[] verts = {
                -0.01f,  0.0f, 0.0f,
                0.01f,  0.0f, 0.0f,
                0.0f, -0.01f, 0.0f,
                0.0f,  0.01f, 0.0f
        };
        vao = glGenVertexArrays();
        vbo = glGenBuffers();
        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, verts, GL_STATIC_DRAW);
        glVertexAttribPointer(0,3,GL_FLOAT,false,3*4,0);
        glEnableVertexAttribArray(0);
        glBindVertexArray(0);
    }
    public void render() {
        glDisable(GL_DEPTH_TEST);
        glBindVertexArray(vao);
        glDrawArrays(GL_LINES, 0, 4);
        glBindVertexArray(0);
        glEnable(GL_DEPTH_TEST);
    }
}
