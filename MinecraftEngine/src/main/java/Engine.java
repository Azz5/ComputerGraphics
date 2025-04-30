import static org.lwjgl.opengl.GL11.*;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3i;
import java.util.HashMap;
import java.util.Map;

public class Engine {
    private static final float MAX_REACH = 3f;
    private static final int CHUNK_SIZE = 16;

    private ShaderProgram shader;
    private ShaderProgram crossShader;
    private final Camera camera;
    private final TextureAtlas atlas;
    private final Map<String, Chunk> chunks = new HashMap<>();
    private final Crosshair crosshair;

    public Engine() {
        camera    = new Camera();
        atlas     = new TextureAtlas("Dirt.png");
        crosshair = new Crosshair();
        // preload origin chunk
        //System.out.println("[DEBUG] Initializing origin chunk at 0:0:0");
        chunks.put(chunkKey(0,0,0), new Chunk(0,0,0, CHUNK_SIZE,CHUNK_SIZE,CHUNK_SIZE, atlas));
    }

    public void run() {
        shader      = new ShaderProgram(VERT_SRC, FRAG_SRC);
        crossShader = new ShaderProgram(CROSS_VS, CROSS_FS);

        while (!Window.shouldClose()) {
            Window.pollEvents();
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            camera.update();

            if (InputHandler.isMouseButtonPressed(0) || InputHandler.isMouseButtonPressed(1)) {
                HitResult result = raycastBlock(MAX_REACH);
                if (result != null) {
                    boolean placing = InputHandler.isMouseButtonPressed(0);
                    //System.out.println("[DEBUG] Ray hit at " + result.hit + ", normal " + result.normal + ", placing=" + placing);
                    modifyBlockAdjacent(result, placing);
                }
            }

            // render world
            shader.bind();
            shader.setUniform1i("atlas", 0);
            shader.setUniformMat4("projection", camera.getProjectionMatrix());
            shader.setUniformMat4("view", camera.getViewMatrix());
            glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
            for (Chunk c : chunks.values()) c.render(shader);
            shader.unbind();

            crossShader.bind();
            crosshair.render();
            crossShader.unbind();

            Window.swapBuffers();
        }

        shader.cleanup();
        crossShader.cleanup();
    }

    private String chunkKey(int cx, int cy, int cz) {
        return cx+":"+cy+":"+cz;
    }

    /**
     * Raycasts and returns both the hit block position and the face-normal of the hit.
     */
    private HitResult raycastBlock(float maxDist) {
        Vector3f dir = new Vector3f(0,0,-1);
        new Matrix4f()
                .rotateY((float)Math.toRadians(camera.getYaw()))
                .rotateX((float)Math.toRadians(camera.getPitch()))
                .transformDirection(dir)
                .normalize();
        Vector3f pos = camera.getPosition();
        Vector3f cur = new Vector3f(pos);

        //System.out.println("[DEBUG] Raycasting from " + pos + " dir " + dir);
        for (int i=0; i<(int)maxDist; i++) {
            Vector3f prev = new Vector3f(cur);
            cur.add(dir);
            int wx = (int)Math.floor(cur.x);
            int wy = (int)Math.floor(cur.y);
            int wz = (int)Math.floor(cur.z);

            //System.out.println("[DEBUG] Ray step " + i + ": world pos ("+wx+","+wy+","+wz+")");

            int px = (int)Math.floor(prev.x);
            int py = (int)Math.floor(prev.y);
            int pz = (int)Math.floor(prev.z);

            int cx = Math.floorDiv(wx, CHUNK_SIZE);
            int cy = Math.floorDiv(wy, CHUNK_SIZE);
            int cz = Math.floorDiv(wz, CHUNK_SIZE);
            String key = chunkKey(cx,cy,cz);
            //System.out.println("[DEBUG] Checking chunk " + key);
            Chunk chunk = chunks.get(key);
            if (chunk == null) {
                //System.out.println("[DEBUG] No chunk loaded at " + key);
                continue;
            }

            int lx = wx - cx*CHUNK_SIZE;
            int ly = wy - cy*CHUNK_SIZE;
            int lz = wz - cz*CHUNK_SIZE;
            //System.out.println("[DEBUG] Local coords in chunk: ("+lx+","+ly+","+lz+")");
            if (!chunk.isAir(lx,ly,lz)) {
                int nx = wx - px;
                int ny = wy - py;
                int nz = wz - pz;
                //System.out.println("[DEBUG] Hit block at " + wx + "," + wy + "," + wz + ", normal ("+nx+","+ny+","+nz+")");
                return new HitResult(new Vector3i(wx,wy,wz), new Vector3i(nx,ny,nz));
            }
        }
        //System.out.println("[DEBUG] Raycast found no block within reach");
        return null;
    }

    /**
     * Places/removes a block adjacent to the hit face (placing in front of hit, removal at hit).
     */
    private void modifyBlockAdjacent(HitResult r, boolean place) {
        Vector3i hit = r.hit;
        Vector3i face = r.normal;
        Vector3i target;
        if (place) {
            // place block on the face closer to camera (opposite normal)
            target = new Vector3i(hit).
                    sub(face.x, face.y, face.z);
            //System.out.println("[DEBUG] Placing block in front at " + target);
        } else {
            // remove the hit block
            target = new Vector3i(hit);
            //System.out.println("[DEBUG] Removing block at " + target);
        }

        int cx = Math.floorDiv(target.x, CHUNK_SIZE);
        int cy = Math.floorDiv(target.y, CHUNK_SIZE);
        int cz = Math.floorDiv(target.z, CHUNK_SIZE);
        String key = chunkKey(cx,cy,cz);
        //System.out.println("[DEBUG] Target chunk key: " + key);

        Chunk chunk = chunks.get(key);
        if (chunk == null) {
            //System.out.println("[DEBUG] Creating new chunk at " + key);
            chunk = new Chunk(cx*CHUNK_SIZE, cy*CHUNK_SIZE, cz*CHUNK_SIZE,
                    CHUNK_SIZE, CHUNK_SIZE, CHUNK_SIZE,
                    atlas, false);
            chunks.put(key, chunk);
        }

        int lx = target.x - cx*CHUNK_SIZE;
        int ly = target.y - cy*CHUNK_SIZE;
        int lz = target.z - cz*CHUNK_SIZE;
        //System.out.println("[DEBUG] Local target coords: ("+lx+","+ly+","+lz+")");

        if (place) {
            chunk.placeBlockLocal(lx,ly,lz, Block.GRASS);
        } else {
            chunk.removeBlockLocal(lx,ly,lz);
        }

        // rebuild both affected chunks
        chunk.buildMesh();
        int ncx = cx + (place ? -face.x : 0);
        int ncy = cy + (place ? -face.y : 0);
        int ncz = cz + (place ? -face.z : 0);
        String nKey = chunkKey(ncx,ncy,ncz);
        //System.out.println("[DEBUG] Rebuilding neighbor chunk " + nKey);
        Chunk neighbor = chunks.get(nKey);
        if (neighbor != null) neighbor.buildMesh();
    }

    private static final String VERT_SRC = "#version 330 core\n" +
            "layout(location=0) in vec3 aPos;\n" +
            "layout(location=1) in vec2 aTex;\n" +
            "uniform mat4 projection, view;\n" +
            "out vec2 vTex;\n" +
            "void main() { vTex = aTex; gl_Position = projection * view * vec4(aPos,1.0); }";
    private static final String FRAG_SRC = "#version 330 core\n" +
            "in vec2 vTex; out vec4 FragColor;\n" +
            "uniform sampler2D atlas;\n" +
            "void main() { FragColor = texture(atlas, vTex); }";
    private static final String CROSS_VS = "#version 330 core\n" +
            "layout(location=0) in vec3 aPos;\n" +
            "void main() { gl_Position = vec4(aPos,1.0); }";
    private static final String CROSS_FS = "#version 330 core\n" +
            "out vec4 FragColor;\n" +
            "void main() { FragColor = vec4(0,0,0,1); }";

    private static class HitResult {
        final Vector3i hit, normal;
        HitResult(Vector3i h, Vector3i n) { hit=h; normal=n; }
    }
}
