
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import org.joml.Vector2f;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.BufferUtils;
import java.io.InputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import org.joml.Vector2f;

public class TextureAtlas {
    private final int id;
    private final int cols = 4, rows = 4;

    public TextureAtlas(String resourceName) {
        id = glGenTextures();
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, id);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(resourceName)) {
            if (in == null) throw new RuntimeException("Resource not found: " + resourceName);
            byte[] bytes = in.readAllBytes();
            ByteBuffer buffer = BufferUtils.createByteBuffer(bytes.length);
            buffer.put(bytes).flip();
            try (MemoryStack stk = MemoryStack.stackPush()) {
                IntBuffer w = stk.mallocInt(1), h = stk.mallocInt(1), c = stk.mallocInt(1);
                ByteBuffer img = STBImage.stbi_load_from_memory(buffer, w, h, c, 4);
                if (img == null) throw new RuntimeException("Failed to load image");
                glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, w.get(0), h.get(0), 0, GL_RGBA, GL_UNSIGNED_BYTE, img);
                STBImage.stbi_image_free(img);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void bind() {
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, id);
    }

    public Vector2f getUV(int x, int y) { return new Vector2f((float)x/cols, (float)y/rows); }
    public float getUSize() { return 1f/cols; }
    public float getVSize() { return 1f/rows; }
}
