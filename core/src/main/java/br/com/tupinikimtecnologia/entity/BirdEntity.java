package br.com.tupinikimtecnologia.entity;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

import br.com.tupinikimtecnologia.config.GameConfig;

public class BirdEntity {

    public float x, y;
    public float velocityX, velocityY;
    public float width, height;

    private int health;
    private boolean immortal;
    private boolean dead;
    private boolean canDie;

    private float immortalTimer;
    private float deathTimer;
    private float stateTime;

    private Animation<TextureRegion> frontAnim;
    private Animation<TextureRegion> leftAnim;
    private Animation<TextureRegion> rightAnim;
    private TextureRegion deathFrame;
    private Animation<TextureRegion> currentAnim;

    private final BirdType type;
    private final Rectangle bounds = new Rectangle();

    public BirdEntity(float x, float y, Texture spriteSheet, BirdType type) {
        this.type = type;
        this.health = type.health;
        this.immortal = true;
        this.immortalTimer = GameConfig.BIRD_IMMORTAL_TIMER_DELAY;
        this.dead = false;
        this.canDie = false;
        this.stateTime = 0;

        // Split 4x4 sprite sheet
        int frameW = spriteSheet.getWidth() / 4;
        int frameH = spriteSheet.getHeight() / 4;
        TextureRegion[][] frames = TextureRegion.split(spriteSheet, frameW, frameH);

        this.width = frameW;
        this.height = frameH;

        // Row 0: front, Row 1: left, Row 2: right, Row 3: death (frame 0)
        frontAnim = new Animation<>(GameConfig.FRAME_DURATION, frames[0]);
        frontAnim.setPlayMode(Animation.PlayMode.LOOP);

        leftAnim = new Animation<>(GameConfig.FRAME_DURATION, frames[1]);
        leftAnim.setPlayMode(Animation.PlayMode.LOOP);

        rightAnim = new Animation<>(GameConfig.FRAME_DURATION, frames[2]);
        rightAnim.setPlayMode(Animation.PlayMode.LOOP);

        deathFrame = frames[3][0];

        currentAnim = frontAnim;

        // Center the bird at the given position
        this.x = x - width / 2;
        this.y = y - height / 2;

        // Initial velocity
        this.velocityX = GameConfig.BIRD_INITIAL_VX;
        this.velocityY = GameConfig.BIRD_INITIAL_VY;
    }

    public void update(float delta) {
        if (dead) return;

        stateTime += delta;

        // Immortality countdown
        if (immortal && health > 0) {
            immortalTimer -= delta;
            if (immortalTimer <= 0) {
                immortal = false;
            }
        }

        // Death countdown (after health reaches 0)
        if (health <= 0 && !canDie) {
            deathTimer += delta;
            if (deathTimer >= GameConfig.BIRD_DEATH_TIMER_DELAY) {
                canDie = true;
            }
        }

        // Don't move if health is 0
        if (health <= 0) return;

        // Move
        x += velocityX * delta;
        y += velocityY * delta;

        // Wall bouncing - can hit corner (2 walls) in one frame, matches original behavior
        if (x < 0) {
            if (health > 0) health--;
            velocityX = GameConfig.BIRD_BOUNCE_SPEED;
            currentAnim = rightAnim;
            x = 0;
        } else if (x + width > GameConfig.CAMERA_WIDTH) {
            if (health > 0) health--;
            velocityX = -GameConfig.BIRD_BOUNCE_SPEED;
            currentAnim = leftAnim;
            x = GameConfig.CAMERA_WIDTH - width;
        }

        if (y < 0) {
            if (health > 0) health--;
            velocityY = GameConfig.BIRD_BOUNCE_SPEED;
            y = 0;
        } else if (y + height > GameConfig.CAMERA_HEIGHT) {
            if (health > 0) health--;
            velocityY = -GameConfig.BIRD_BOUNCE_SPEED;
            currentAnim = frontAnim;
            y = GameConfig.CAMERA_HEIGHT - height;
        }

        // When health reaches 0, enter dying state
        if (health <= 0) {
            health = 0;
            immortal = true;
            deathTimer = 0;
        }
    }

    public void draw(SpriteBatch batch) {
        if (dead) return;

        TextureRegion frame;
        if (health <= 0) {
            frame = deathFrame;
        } else {
            frame = currentAnim.getKeyFrame(stateTime);
        }

        float alpha = (immortal || health <= 0) ? 0.5f : 1f;
        batch.setColor(1, 1, 1, alpha);
        batch.draw(frame, x, y, width, height);
        batch.setColor(1, 1, 1, 1);
    }

    public boolean overlaps(float otherX, float otherY, float otherW, float otherH) {
        return x < otherX + otherW && x + width > otherX
            && y < otherY + otherH && y + height > otherY;
    }

    public boolean overlaps(BirdEntity other) {
        return overlaps(other.x, other.y, other.width, other.height);
    }

    public float getCenterX() { return x + width / 2; }
    public float getCenterY() { return y + height / 2; }
    public boolean isImmortal() { return immortal; }
    public boolean isDead() { return dead; }
    public boolean canDie() { return canDie; }
    public int getHealth() { return health; }
    public BirdType getType() { return type; }

    public void markDead() { this.dead = true; }

    public Rectangle getBounds() {
        bounds.set(x, y, width, height);
        return bounds;
    }
}
