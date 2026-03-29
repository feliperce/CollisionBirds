package br.com.tupinikimtecnologia.entity;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import br.com.tupinikimtecnologia.config.GameConfig;

public class ItemEntity {

    public enum ItemType { SHIELD, POTION, LIFE }

    public float x, y;
    public float width, height;
    public final ItemType type;
    private float lifetime;
    private boolean collected;
    private final Texture texture;

    public ItemEntity(float x, float y, Texture texture, ItemType type) {
        this.texture = texture;
        this.type = type;
        this.width = texture.getWidth();
        this.height = texture.getHeight();
        this.x = x - width / 2;
        this.y = y - height / 2;
        this.lifetime = GameConfig.ITEM_LIFETIME;
        this.collected = false;
    }

    public void update(float delta) {
        lifetime -= delta;
    }

    public void draw(SpriteBatch batch) {
        if (!collected && lifetime > 0) {
            // Blink when about to expire (last 2 seconds)
            if (lifetime < 2f) {
                float blink = (float) Math.sin(lifetime * 10) * 0.5f + 0.5f;
                batch.setColor(1, 1, 1, blink);
            }
            batch.draw(texture, x, y, width, height);
            batch.setColor(1, 1, 1, 1);
        }
    }

    public boolean overlapsPlayer(float px, float py, float pw, float ph) {
        return x < px + pw && x + width > px && y < py + ph && y + height > py;
    }

    public boolean isExpired() { return lifetime <= 0; }
    public boolean isCollected() { return collected; }
    public void collect() { this.collected = true; }
}
