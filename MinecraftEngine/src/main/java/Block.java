import org.joml.Vector2f;

public enum Block {
    AIR(0), GRASS(1), DIRT(2), STONE(3);
    private final int id;
    Block(int id) { this.id = id; }
    public int getId() { return id; }
    public Vector2f getTextureUV(TextureAtlas atlas) {
        switch (this) {
            case GRASS: return atlas.getUV(0,0);
            case DIRT:  return atlas.getUV(1,0);
            case STONE: return atlas.getUV(2,0);
            default:    return atlas.getUV(3,0);
        }
    }
}
