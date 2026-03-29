package br.com.tupinikimtecnologia.entity;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

import br.com.tupinikimtecnologia.config.GameConfig;

public class PlayerEntity {

    public float x, y;
    public float width, height;
    private float stateTime;

    // Published APK sprite sheet rows:
    // Row 0 (frames 0-3): Normal front-facing
    // Row 1 (frames 4-7): Potion/invincibility effect
    // Row 2 (frames 8-11): Shield equipped
    // Row 3 (frame 12): Dead (X eyes)
    private final Animation<TextureRegion> normalAnim;
    private final Animation<TextureRegion> shieldAnim;
    private final TextureRegion deadFrame;

    private boolean dead;
    private boolean hasPotionInvisibility;
    private boolean hasShield;
    private boolean hitInvincible; // post-damage blinking
    private int lives;

    private final Rectangle bounds = new Rectangle();

    public PlayerEntity(float centerX, float centerY, Texture spriteSheet) {
        int frameW = spriteSheet.getWidth() / 4;
        int frameH = spriteSheet.getHeight() / 4;
        TextureRegion[][] frames = TextureRegion.split(spriteSheet, frameW, frameH);

        this.width = frameW;
        this.height = frameH;

        normalAnim = new Animation<>(GameConfig.FRAME_DURATION, frames[0]);
        normalAnim.setPlayMode(Animation.PlayMode.LOOP);
        shieldAnim = new Animation<>(GameConfig.FRAME_DURATION, frames[2]);
        shieldAnim.setPlayMode(Animation.PlayMode.LOOP);
        deadFrame = frames[3][0];

        this.x = centerX - width / 2;
        this.y = centerY - height / 2;
        this.stateTime = 0;
        this.dead = false;
        this.hasPotionInvisibility = false;
        this.hasShield = false;
        this.lives = GameConfig.PLAYER_INITIAL_LIVES;
    }

    public void update(float delta) {
        stateTime += delta;
    }

    public void draw(SpriteBatch batch) {
        TextureRegion frame;
        if (dead) {
            frame = deadFrame;
        } else if (hasShield) {
            // Shield: use shield animation (row 2), full opacity
            frame = shieldAnim.getKeyFrame(stateTime);
        } else {
            // Normal animation (row 0)
            frame = normalAnim.getKeyFrame(stateTime);
        }

        if (!dead && !hasShield) {
            if (hitInvincible) {
                // Post-damage: blinking effect
                float blink = (float) Math.sin(stateTime * 12) * 0.3f + 0.5f;
                batch.setColor(1, 1, 1, blink);
            } else if (hasPotionInvisibility) {
                // Potion: fixed semi-transparent
                batch.setColor(1, 1, 1, 0.5f);
            }
        }

        batch.draw(frame, x, y, width, height);
        batch.setColor(1, 1, 1, 1);
    }

    public void setPosition(float centerX, float centerY) {
        this.x = Math.max(0, Math.min(centerX - width / 2, GameConfig.CAMERA_WIDTH - width));
        this.y = Math.max(0, Math.min(centerY - height / 2, GameConfig.CAMERA_HEIGHT - height));
    }

    public void setShield(boolean active) { this.hasShield = active; }
    public void setPotionInvisibility(boolean active) { this.hasPotionInvisibility = active; }
    public void setHitInvincible(boolean active) { this.hitInvincible = active; }
    public void setDead(boolean dead) { this.dead = dead; }

    public boolean isDead() { return dead; }
    public boolean hasPotionInvisibility() { return hasPotionInvisibility; }
    public boolean hasShield() { return hasShield; }
    public boolean isHitInvincible() { return hitInvincible; }
    public int getLives() { return lives; }
    public void setLives(int lives) { this.lives = lives; }

    public float getCenterX() { return x + width / 2; }
    public float getCenterY() { return y + height / 2; }

    public Rectangle getBounds() {
        bounds.set(x, y, width, height);
        return bounds;
    }
}
