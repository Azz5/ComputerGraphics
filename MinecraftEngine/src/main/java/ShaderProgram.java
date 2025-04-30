
import static org.lwjgl.opengl.GL20.*;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL20;
import org.lwjgl.system.MemoryStack;
import java.nio.FloatBuffer;

public class ShaderProgram {
    private final int programId;

    public ShaderProgram(String vertSrc, String fragSrc) {
        int vs = compile(GL_VERTEX_SHADER, vertSrc);
        int fs = compile(GL_FRAGMENT_SHADER, fragSrc);
        programId = glCreateProgram();
        glAttachShader(programId, vs);
        glAttachShader(programId, fs);
        glLinkProgram(programId);
        if (glGetProgrami(programId, GL_LINK_STATUS) == GL_FALSE)
            throw new RuntimeException("Shader link error: " + glGetProgramInfoLog(programId));
        glDeleteShader(vs);
        glDeleteShader(fs);
    }

    private int compile(int type, String src) {
        int id = glCreateShader(type);
        glShaderSource(id, src);
        glCompileShader(id);
        if (glGetShaderi(id, GL_COMPILE_STATUS) == GL_FALSE)
            throw new RuntimeException("Shader compile error: " + glGetShaderInfoLog(id));
        return id;
    }

    public void bind()   { glUseProgram(programId); }
    public void unbind() { glUseProgram(0); }
    public void cleanup(){ glDeleteProgram(programId); }

    public void setUniformMat4(String name, Matrix4f m) {
        int loc = glGetUniformLocation(programId, name);
        try (MemoryStack stk = MemoryStack.stackPush()) {
            FloatBuffer fb = stk.mallocFloat(16);
            m.get(fb);
            glUniformMatrix4fv(loc, false, fb);
        }
    }

    public void setUniform1i(String name, int value) {
        int loc = glGetUniformLocation(programId, name);
        glUniform1i(loc, value);
    }

    public void setUniform3f(String name, float x, float y, float z) {
        int location = GL20.glGetUniformLocation(programId, name);
        GL20.glUniform3f(location, x, y, z);
    }

}
