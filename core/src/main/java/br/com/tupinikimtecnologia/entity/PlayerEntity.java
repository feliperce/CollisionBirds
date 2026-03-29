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

    private Animation<TextureRegion> frontAnim;
    private final Rectangle bounds = new Rectangle();

    public PlayerEntity(float centerX, float centerY, Texture spriteSheet) {
        int frameW = spriteSheet.getWidth() / 4;
        int frameH = spriteSheet.getHeight() / 4;
        TextureRegion[][] frames = TextureRegion.split(spriteSheet, frameW, frameH);

        this.width = frameW;
        this.height = frameH;

        // Front facing animation (row 0, frames 0-3)
        frontAnim = new Animation<>(GameConfig.FRAME_DURATION, frames[0]);
        frontAnim.setPlayMode(Animation.PlayMode.LOOP);

        // Center at given position
        this.x = centerX - width / 2;
        this.y = centerY - height / 2;
        this.stateTime = 0;
    }

    public void update(float delta) {
        stateTime += delta;
    }

    public void draw(SpriteBatch batch) {
        TextureRegion frame = frontAnim.getKeyFrame(stateTime);
        batch.draw(frame, x, y, width, height);
    }

    public void setPosition(float centerX, float centerY) {
        this.x = centerX - width / 2;
        this.y = centerY - height / 2;
    }

    public float getCenterX() { return x + width / 2; }
    public float getCenterY() { return y + height / 2; }

    public Rectangle getBounds() {
        bounds.set(x, y, width, height);
        return bounds;
    }
}
