package br.com.tupinikimtecnologia.entity;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import br.com.tupinikimtecnologia.config.GameConfig;

public class ItemEntity {

    public enum ItemType { SHIELD, POTION, LIFE }

    public float x, y;
    public float width, height;
    public final ItemType type;
    private float lifetime;
    private float stateTime;
    private boolean collected;

    // Static items use texture directly, animated items use animation
    private final Texture texture;
    private final Animation<TextureRegion> animation;

    /** Create a static (non-animated) item */
    public ItemEntity(float x, float y, Texture texture, ItemType type) {
        this.texture = texture;
        this.animation = null;
        this.type = type;
        this.width = texture.getWidth();
        this.height = texture.getHeight();
        this.x = x - width / 2;
        this.y = y - height / 2;
        this.lifetime = GameConfig.ITEM_DESPAWN_TIME;
        this.stateTime = 0;
        this.collected = false;
    }

    /** Create an animated item (e.g., 1-UP heart with 4 frames in a horizontal strip) */
    public ItemEntity(float x, float y, Texture spriteSheet, ItemType type, int columns) {
        this.texture = spriteSheet;
        this.type = type;

        int frameW = spriteSheet.getWidth() / columns;
        int frameH = spriteSheet.getHeight();
        TextureRegion[] frames = new TextureRegion[columns];
        for (int i = 0; i < columns; i++) {
            frames[i] = new TextureRegion(spriteSheet, i * frameW, 0, frameW, frameH);
        }
        this.animation = new Animation<>(GameConfig.FRAME_DURATION, frames);
        this.animation.setPlayMode(Animation.PlayMode.LOOP);

        this.width = frameW;
        this.height = frameH;
        this.x = x - width / 2;
        this.y = y - height / 2;
        this.lifetime = GameConfig.ITEM_DESPAWN_TIME;
        this.stateTime = 0;
        this.collected = false;
    }

    public void update(float delta) {
        lifetime -= delta;
        stateTime += delta;
    }

    public void draw(SpriteBatch batch) {
        if (collected || lifetime <= 0) return;

        // Blink when about to expire (last 2 seconds)
        if (lifetime < 2f) {
            float blink = (float) Math.sin(lifetime * 10) * 0.5f + 0.5f;
            batch.setColor(1, 1, 1, blink);
        }

        if (animation != null) {
            batch.draw(animation.getKeyFrame(stateTime), x, y, width, height);
        } else {
            batch.draw(texture, x, y, width, height);
        }

        batch.setColor(1, 1, 1, 1);
    }

    public boolean overlapsPlayer(float px, float py, float pw, float ph) {
        return x < px + pw && x + width > px && y < py + ph && y + height > py;
    }

    public boolean isExpired() { return lifetime <= 0; }
    public boolean isCollected() { return collected; }
    public void collect() { this.collected = true; }
}
