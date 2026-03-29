package br.com.tupinikimtecnologia.entity;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import br.com.tupinikimtecnologia.config.GameConfig;

public class BirdEntity {

    public float x, y;
    public float velocityX, velocityY;
    public float width, height;

    private int remainingBounces;
    private final float speed; // pixels per second (per bird type)
    private boolean invisible;  // starts true, becomes false after 3s
    private boolean markedForRemoval;
    private boolean hasBeenHit; // set 4s after bounces reach 0
    private float stateTime;
    private float appearTimer;   // 3s countdown to become visible
    private float removableTimer; // 4s countdown after bounces=0 to become removable
    private boolean removableTimerStarted;

    private final Animation<TextureRegion> frontAnim;
    private final Animation<TextureRegion> leftAnim;
    private final Animation<TextureRegion> rightAnim;
    private final TextureRegion deathFrame;
    private Animation<TextureRegion> currentAnim;

    private final BirdType type;

    public BirdEntity(float x, float y, Texture spriteSheet, BirdType type) {
        this.type = type;
        this.remainingBounces = type.bounces;
        this.speed = type.getPixelSpeed();
        this.invisible = true;
        this.markedForRemoval = false;
        this.hasBeenHit = false;
        this.stateTime = 0;
        this.appearTimer = GameConfig.BIRD_APPEAR_DELAY;
        this.removableTimer = GameConfig.BIRD_REMOVABLE_DELAY;
        this.removableTimerStarted = false;

        int frameW = spriteSheet.getWidth() / 4;
        int frameH = spriteSheet.getHeight() / 4;
        TextureRegion[][] frames = TextureRegion.split(spriteSheet, frameW, frameH);

        this.width = frameW;
        this.height = frameH;

        frontAnim = new Animation<>(GameConfig.FRAME_DURATION, frames[0]);
        frontAnim.setPlayMode(Animation.PlayMode.LOOP);
        leftAnim = new Animation<>(GameConfig.FRAME_DURATION, frames[1]);
        leftAnim.setPlayMode(Animation.PlayMode.LOOP);
        rightAnim = new Animation<>(GameConfig.FRAME_DURATION, frames[2]);
        rightAnim.setPlayMode(Animation.PlayMode.LOOP);
        deathFrame = frames[3][0];

        currentAnim = frontAnim;

        // Center at given position
        this.x = x - width / 2;
        this.y = y - height / 2;

        // Initial velocity: (-speed, 10*PPM) - bird moves left and slightly down
        this.velocityX = -speed;
        this.velocityY = GameConfig.BIRD_INITIAL_VY;
    }

    public void update(float delta) {
        if (markedForRemoval) return;

        stateTime += delta;

        // Appear timer (bird becomes visible after 3s)
        if (invisible) {
            appearTimer -= delta;
            if (appearTimer <= 0) {
                invisible = false;
            }
            return; // Don't move while invisible
        }

        // If bounces reached 0, start removable timer
        if (remainingBounces <= 0) {
            if (!removableTimerStarted) {
                removableTimerStarted = true;
                removableTimer = GameConfig.BIRD_REMOVABLE_DELAY;
            }
            removableTimer -= delta;
            if (removableTimer <= 0) {
                hasBeenHit = true;
            }
            return; // Don't move when dead
        }

        // Move
        x += velocityX * delta;
        y += velocityY * delta;

        // Wall bouncing with per-bird speed
        if (x < 0) {
            if (remainingBounces > 0) remainingBounces--;
            velocityX = speed;
            currentAnim = rightAnim;
            x = 0;
        } else if (x + width > GameConfig.CAMERA_WIDTH) {
            if (remainingBounces > 0) remainingBounces--;
            velocityX = -speed;
            currentAnim = leftAnim;
            x = GameConfig.CAMERA_WIDTH - width;
        }

        if (y < 0) {
            if (remainingBounces > 0) remainingBounces--;
            velocityY = speed;
            y = 0;
        } else if (y + height > GameConfig.CAMERA_HEIGHT) {
            if (remainingBounces > 0) remainingBounces--;
            velocityY = -speed;
            currentAnim = frontAnim;
            y = GameConfig.CAMERA_HEIGHT - height;
        }
    }

    public void draw(SpriteBatch batch) {
        if (markedForRemoval) return;

        TextureRegion frame;
        if (remainingBounces <= 0) {
            frame = deathFrame;
        } else {
            frame = currentAnim.getKeyFrame(stateTime);
        }

        float alpha = (invisible || remainingBounces <= 0) ? 0.5f : 1f;
        batch.setColor(1, 1, 1, alpha);
        batch.draw(frame, x, y, width, height);
        batch.setColor(1, 1, 1, 1);
    }

    /** Kill the bird instantly (set bounces to 0) */
    public void kill() {
        this.remainingBounces = 0;
    }

    public float getHitboxX() { return x + width  * (1f - GameConfig.HITBOX_SCALE) / 2f; }
    public float getHitboxY() { return y + height * (1f - GameConfig.HITBOX_SCALE) / 2f; }
    public float getHitboxW() { return width  * GameConfig.HITBOX_SCALE; }
    public float getHitboxH() { return height * GameConfig.HITBOX_SCALE; }

    public boolean overlaps(float otherX, float otherY, float otherW, float otherH) {
        float hx = getHitboxX(), hy = getHitboxY(), hw = getHitboxW(), hh = getHitboxH();
        return hx < otherX + otherW && hx + hw > otherX
            && hy < otherY + otherH && hy + hh > otherY;
    }

    public boolean overlaps(BirdEntity other) {
        return overlaps(other.getHitboxX(), other.getHitboxY(), other.getHitboxW(), other.getHitboxH());
    }

    public float getCenterX() { return x + width / 2; }
    public float getCenterY() { return y + height / 2; }
    public boolean isInvisible() { return invisible; }
    public boolean isMarkedForRemoval() { return markedForRemoval; }
    public boolean hasBeenHit() { return hasBeenHit; }
    public int getRemainingBounces() { return remainingBounces; }
    public BirdType getType() { return type; }

    public void markForRemoval() { this.markedForRemoval = true; }
}
