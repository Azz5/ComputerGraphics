import org.joml.Vector3i;
import java.util.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL30.*;

public class Chunk {
    private final int width, height, depth;
    private final Block[][][] blocks;
    private int vao, vbo, vertexCount;
    private final TextureAtlas atlas;
    private final Vector3i offset;

    // Main constructor (default terrain generation)
    public Chunk(int ox, int oy, int oz,
                 int w, int h, int d,
                 TextureAtlas atlas) {
        this(ox, oy, oz, w, h, d, atlas, true);
    }

    /**
     * Allows creating either a terrain or empty chunk.
     * @param generateDefault if true populates dirt/grass, else fills with AIR.
     */
    public Chunk(int ox, int oy, int oz,
                 int w, int h, int d,
                 TextureAtlas atlas,
                 boolean generateDefault) {
        offset = new Vector3i(ox, oy, oz);
        width = w; height = h; depth = d;
        this.atlas = atlas;
        blocks = new Block[w][h][d];
        if (generateDefault) {
            generateBlocks();
        } else {
            // Fill entirely with AIR
            for (int x = 0; x < w; x++)
                for (int y = 0; y < h; y++)
                    for (int z = 0; z < d; z++)
                        blocks[x][y][z] = Block.AIR;
        }
        buildMesh();
    }

    private void generateBlocks() {
        for (int x = 0; x < width; x++)
            for (int y = 0; y < height; y++)
                for (int z = 0; z < depth; z++) {
                    if (y < height/4) blocks[x][y][z] = Block.DIRT;
                    else if (y < height/2) blocks[x][y][z] = Block.GRASS;
                    else blocks[x][y][z] = Block.AIR;
                }
    }

    void buildMesh() {
        List<Float> data = new ArrayList<>();
        for (int x = 0; x < width; x++)
            for (int y = 0; y < height; y++)
                for (int z = 0; z < depth; z++) {
                    Block b = blocks[x][y][z];
                    if (b == Block.AIR) continue;
                    for (Direction dir : Direction.values()) {
                        int nx = x + dir.dx;
                        int ny = y + dir.dy;
                        int nz = z + dir.dz;
                        if (isAir(nx, ny, nz)) {
                            appendFace(data, x, y, z, dir, b);
                        }
                    }
                }
        float[] arr = new float[data.size()];
        for (int i = 0; i < arr.length; i++) arr[i] = data.get(i);
        vertexCount = arr.length / 5;

        if (vao != 0) glDeleteVertexArrays(vao);
        if (vbo != 0) glDeleteBuffers(vbo);
        vao = glGenVertexArrays();
        vbo = glGenBuffers();
        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, arr, GL_STATIC_DRAW);
        glVertexAttribPointer(0,3,GL_FLOAT,false,5*4,0);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(1,2,GL_FLOAT,false,5*4,3*4);
        glEnableVertexAttribArray(1);
        glBindBuffer(GL_ARRAY_BUFFER,0);
        glBindVertexArray(0);
    }

    private void appendFace(List<Float> data, int x, int y, int z, Direction dir, Block b) {
        float[][] v = dir.vertices;
        org.joml.Vector2f uv = b.getTextureUV(atlas);
        float u0 = uv.x, v0 = uv.y, du = atlas.getUSize(), dv = atlas.getVSize();
        int[] idx = {0,1,2, 2,3,0};
        for (int i : idx) {
            data.add(x + offset.x + v[i][0]);
            data.add(y + offset.y + v[i][1]);
            data.add(z + offset.z + v[i][2]);
            data.add((i==0||i==3) ? u0 : u0+du);
            data.add(i<2 ? v0+dv : v0);
        }
    }

    public void render(ShaderProgram s) {
        atlas.bind();
        glBindVertexArray(vao);
        glDrawArrays(GL_TRIANGLES, 0, vertexCount);
        glBindVertexArray(0);
    }

    /**
     * Place a block using local chunk coordinates (0..width-1).
     */
    public void placeBlockLocal(int lx, int ly, int lz, Block t) {
        setLocalBlock(lx, ly, lz, t);
    }

    /**
     * Remove a block using local chunk coordinates.
     */
    public void removeBlockLocal(int lx, int ly, int lz) {
        setLocalBlock(lx, ly, lz, Block.AIR);
    }

    private void setLocalBlock(int x, int y, int z, Block t) {
        if (x>=0 && y>=0 && z>=0 && x<width && y<height && z<depth) {
            blocks[x][y][z] = t;
            buildMesh();
        }
    }

    /**
     * True if the block at local coords is AIR, or if the coords lie outside the chunk.
     */
    public boolean isAir(int x, int y, int z) {
        if (x < 0 || y < 0 || z < 0 || x >= width || y >= height || z >= depth) {
            return true;
        }
        return blocks[x][y][z] == Block.AIR;
    }

    enum Direction {
        NORTH(0,0,-1,new float[][]{{0,0,0},{1,0,0},{1,1,0},{0,1,0}}),
        SOUTH(0,0,1, new float[][]{{1,0,1},{0,0,1},{0,1,1},{1,1,1}}),
        WEST(-1,0,0, new float[][]{{0,0,1},{0,0,0},{0,1,0},{0,1,1}}),
        EAST(1,0,0,  new float[][]{{1,0,0},{1,0,1},{1,1,1},{1,1,0}}),
        DOWN(0,-1,0, new float[][]{{0,0,0},{0,0,1},{1,0,1},{1,0,0}}),
        UP(0,1,0,   new float[][]{{0,1,1},{0,1,0},{1,1,0},{1,1,1}});
        final int dx,dy,dz;
        final float[][] vertices;
        Direction(int dx,int dy,int dz,float[][] vs){this.dx=dx;this.dy=dy;this.dz=dz;this.vertices=vs;}
    }
}