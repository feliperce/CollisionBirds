package br.com.tupinikimtecnologia.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;

import br.com.tupinikimtecnologia.Main;
import br.com.tupinikimtecnologia.config.GameConfig;
import br.com.tupinikimtecnologia.entity.BirdEntity;
import br.com.tupinikimtecnologia.entity.BirdType;
import br.com.tupinikimtecnologia.entity.ItemEntity;
import br.com.tupinikimtecnologia.entity.PlayerEntity;

import java.util.Iterator;

/**
 * Core gameplay screen - replicates exact logic from published APK.
 */
public class GameScreen extends ScreenAdapter {

    private enum State { PLAYING, GAME_OVER }

    private final Main game;
    private OrthographicCamera camera;
    private FitViewport viewport;

    // Entities
    private PlayerEntity player;
    private final Array<BirdEntity> birds = new Array<>();
    private final Array<ItemEntity> items = new Array<>();

    // Item state flags (match original: only one of each on screen at a time)
    private boolean shieldItemOnScreen;
    private boolean potionItemOnScreen;
    private boolean oneUpItemOnScreen;

    // Power-up timers
    private float shieldDurationTimer;
    private float potionDurationTimer;
    private float postHitInvincibilityTimer;

    // Game timers
    private float addBirdTimer;
    private float itemSpawnTimer; // 1-second tick for item spawn checks
    private int minutes;
    private int seconds;
    private int birdsKilled;

    // Game state
    private State state;
    private boolean dragging;
    private boolean gameStopped; // stops collision/spawn logic

    // Game over UI
    private Rectangle playButtonBounds;

    private final Vector3 touchPoint = new Vector3();
    private final GlyphLayout layout = new GlyphLayout();

    public GameScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {
        camera = new OrthographicCamera();
        viewport = new FitViewport(GameConfig.CAMERA_WIDTH, GameConfig.CAMERA_HEIGHT, camera);
        camera.position.set(GameConfig.CENTER_X, GameConfig.CENTER_Y, 0);
        camera.update();

        birds.clear();
        items.clear();
        addBirdTimer = 0;
        itemSpawnTimer = 0;
        minutes = 0;
        seconds = 0;
        birdsKilled = 0;
        shieldItemOnScreen = false;
        potionItemOnScreen = false;
        oneUpItemOnScreen = false;
        shieldDurationTimer = 0;
        potionDurationTimer = 0;
        postHitInvincibilityTimer = 0;
        state = State.PLAYING;
        dragging = false;
        gameStopped = false;

        player = new PlayerEntity(GameConfig.CENTER_X, GameConfig.CENTER_Y, game.assets.playerSheet);

        game.assets.stopAllMusic();
        if (game.assets.gameMusic != null) game.assets.gameMusic.play();

        Gdx.input.setCatchKey(Input.Keys.BACK, true);
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                viewport.unproject(touchPoint.set(screenX, screenY, 0));
                if (state == State.GAME_OVER) {
                    return handleGameOverTouch(touchPoint.x, touchPoint.y);
                }
                if (player.getBounds().contains(touchPoint.x, touchPoint.y)) {
                    dragging = true;
                    player.setPosition(touchPoint.x, touchPoint.y);
                    return true;
                }
                return false;
            }

            @Override
            public boolean touchDragged(int screenX, int screenY, int pointer) {
                if (dragging && state == State.PLAYING) {
                    viewport.unproject(touchPoint.set(screenX, screenY, 0));
                    player.setPosition(touchPoint.x, touchPoint.y);
                    return true;
                }
                return false;
            }

            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                dragging = false;
                return false;
            }

            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.BACK || keycode == Input.Keys.ESCAPE) {
                    game.assets.stopAllMusic();
                    game.setScreen(new MenuScreen(game));
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void render(float delta) {
        if (state == State.PLAYING) {
            updateGame(delta);
        }

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        SpriteBatch batch = game.batch;
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        batch.draw(game.assets.gameBackground, 0, 0, GameConfig.CAMERA_WIDTH, GameConfig.CAMERA_HEIGHT);

        for (ItemEntity item : items) item.draw(batch);
        for (BirdEntity bird : birds) bird.draw(batch);
        player.draw(batch);

        if (state == State.PLAYING) drawHUD(batch);
        if (state == State.GAME_OVER) drawGameOver(batch);

        batch.end();
    }

    // ========== GAME UPDATE ==========

    private void updateGame(float delta) {
        player.update(delta);

        // Power-up duration timers
        updatePowerUpTimers(delta);

        // Time counter (1-second ticks) + item spawn
        updateTimeAndItemSpawn(delta);

        // Bird spawn (every 5 seconds)
        addBirdTimer += delta;
        if (addBirdTimer >= GameConfig.ADD_BIRD_TIMER_DELAY && !gameStopped) {
            addBirdTimer -= GameConfig.ADD_BIRD_TIMER_DELAY;
            spawnBird();
        }

        // Update birds and items
        for (BirdEntity bird : birds) bird.update(delta);
        for (ItemEntity item : items) item.update(delta);

        if (!gameStopped) {
            checkBirdLifeAndRemoval();
            checkBirdCollisions();
            checkPlayerBirdCollisions();
        }

        // Item pickup always checked (even during transitions)
        checkItemPickup();
        cleanupItems();
    }

    private void updatePowerUpTimers(float delta) {
        // Shield duration
        if (player.hasShield()) {
            shieldDurationTimer -= delta;
            if (shieldDurationTimer <= 0) {
                player.setShield(false);
                if (game.assets.shieldLostSound != null) game.assets.shieldLostSound.play();
            }
        }

        // Potion duration (13 seconds)
        if (player.hasPotionInvisibility() && potionDurationTimer > 0) {
            potionDurationTimer -= delta;
            if (potionDurationTimer <= 0) {
                player.setPotionInvisibility(false);
                potionDurationTimer = 0;
            }
        }

        // Post-hit invincibility (4 seconds)
        if (player.isHitInvincible() && postHitInvincibilityTimer > 0) {
            postHitInvincibilityTimer -= delta;
            if (postHitInvincibilityTimer <= 0) {
                player.setHitInvincible(false);
                postHitInvincibilityTimer = 0;
            }
        }
    }

    private void updateTimeAndItemSpawn(float delta) {
        itemSpawnTimer += delta;
        if (itemSpawnTimer >= 1f) {
            itemSpawnTimer -= 1f;

            // Increment game time
            seconds++;
            if (seconds >= 60) {
                minutes++;
                seconds = 0;
            }

            // Item spawn check (every 1-second tick, matching original)
            if (!gameStopped && !potionItemOnScreen && !shieldItemOnScreen
                && !player.hasPotionInvisibility() && !player.hasShield()) {

                int roll = MathUtils.random(1, GameConfig.ITEM_SPAWN_DENOMINATOR);
                if (roll >= 1 && roll <= 8) {
                    // Spawn POTION (5% chance per second)
                    spawnPotionItem();
                } else if (roll >= 9 && roll <= 13) {
                    // Spawn SHIELD (3.1% chance per second)
                    spawnShieldItem();
                }
            }
        }
    }

    // ========== SPAWNING ==========

    private void spawnBird() {
        float x = MathUtils.random(GameConfig.SPAWN_MARGIN, GameConfig.CAMERA_WIDTH - GameConfig.SPAWN_MARGIN);
        float y = MathUtils.random(GameConfig.SPAWN_MARGIN, GameConfig.CAMERA_HEIGHT - GameConfig.SPAWN_MARGIN);

        int randomValue = MathUtils.random(99);
        BirdType type = BirdType.fromRandom(randomValue);

        Texture sheet;
        switch (type) {
            case VERMELHO: sheet = game.assets.birdVermelhoSheet; break;
            case AMARELO:  sheet = game.assets.birdAmareloSheet; break;
            case VIOLETA:  sheet = game.assets.birdVioletaSheet; break;
            default:       sheet = game.assets.birdVerdeSheet; break;
        }

        birds.add(new BirdEntity(x, y, sheet, type));
    }

    private void spawnPotionItem() {
        float x = MathUtils.random(GameConfig.SPAWN_MARGIN, GameConfig.CAMERA_WIDTH - GameConfig.SPAWN_MARGIN);
        float y = MathUtils.random(GameConfig.SPAWN_MARGIN, GameConfig.CAMERA_HEIGHT - GameConfig.SPAWN_MARGIN);
        items.add(new ItemEntity(x, y, game.assets.potionStatTexture, ItemEntity.ItemType.POTION));
        potionItemOnScreen = true;
    }

    private void spawnShieldItem() {
        float x = MathUtils.random(GameConfig.SPAWN_MARGIN, GameConfig.CAMERA_WIDTH - GameConfig.SPAWN_MARGIN);
        float y = MathUtils.random(GameConfig.SPAWN_MARGIN, GameConfig.CAMERA_HEIGHT - GameConfig.SPAWN_MARGIN);
        items.add(new ItemEntity(x, y, game.assets.shieldStatTexture, ItemEntity.ItemType.SHIELD));
        shieldItemOnScreen = true;
    }

    private void spawn1UpItem(float x, float y) {
        if (oneUpItemOnScreen) return;
        // life-stat.png is a 4-frame horizontal sprite sheet (animated hearts)
        items.add(new ItemEntity(x, y, game.assets.lifeStatTexture, ItemEntity.ItemType.LIFE, 4));
        oneUpItemOnScreen = true;
    }

    // ========== COLLISION & REMOVAL ==========

    private void checkBirdLifeAndRemoval() {
        Iterator<BirdEntity> it = birds.iterator();
        while (it.hasNext()) {
            BirdEntity bird = it.next();

            // Bird with 0 bounces and marked as hit -> remove
            if (bird.getRemainingBounces() <= 0 && bird.hasBeenHit()) {
                // If RED bird, drop 1-UP at death position
                if (bird.getType() == BirdType.VERMELHO && !oneUpItemOnScreen) {
                    spawn1UpItem(bird.getCenterX(), bird.getCenterY());
                }
                bird.markForRemoval();
                it.remove();
            }
        }
    }

    /**
     * Player-bird collision logic (EXACT match of original published APK):
     * - If player has POTION (hasPotionInvisibility): NO collision at all, birds pass through
     * - If player has SHIELD: bird DIES, punch sound, shield stays, player takes no damage
     * - If UNPROTECTED: bird DIES, punch sound, player loses 1 life, gets 4s invincibility
     */
    private void checkPlayerBirdCollisions() {
        for (BirdEntity bird : birds) {
            // Only collide with visible, alive, not-already-hit birds
            if (bird.isInvisible() || bird.isMarkedForRemoval() || bird.hasBeenHit()
                || bird.getRemainingBounces() <= 0) continue;

            if (!bird.overlaps(player.getHitboxX(), player.getHitboxY(), player.getHitboxW(), player.getHitboxH())) continue;

            // --- Collision detected ---

            if (!player.isDead() && !player.hasPotionInvisibility() && !player.isHitInvincible() && !player.hasShield()) {
                // UNPROTECTED: player takes damage, bird dies
                birdsKilled++;
                if (game.assets.punchSound != null) game.assets.punchSound.play();

                bird.kill(); // set bounces to 0

                // Player gets post-hit invincibility (4s) with blinking
                player.setHitInvincible(true);
                postHitInvincibilityTimer = GameConfig.POST_HIT_INVINCIBILITY;

                // Lose 1 life
                player.setLives(player.getLives() - 1);

                if (player.getLives() <= 0) {
                    // Player dies
                    if (game.assets.playerDieSound != null) game.assets.playerDieSound.play();
                    player.setDead(true);
                    triggerGameOver();
                }
                return; // only one collision per frame
            }

            if (player.hasShield()) {
                // SHIELDED: bird dies, shield STAYS active, no damage
                birdsKilled++;
                if (game.assets.punchSound != null) game.assets.punchSound.play();
                bird.kill(); // set bounces to 0
                return;
            }

            // If player has potion: NO collision processing (birds pass through)
            // This is handled by the !hasPotionInvisibility check above
        }
    }

    private void checkItemPickup() {
        Iterator<ItemEntity> it = items.iterator();
        while (it.hasNext()) {
            ItemEntity item = it.next();
            if (item.isCollected() || item.isExpired()) continue;
            if (!item.overlapsPlayer(player.x, player.y, player.width, player.height)) continue;

            item.collect();
            switch (item.type) {
                case POTION:
                    if (game.assets.potionUseSound != null) game.assets.potionUseSound.play();
                    player.setPotionInvisibility(true);
                    potionDurationTimer = GameConfig.POTION_DURATION; // 13 seconds
                    potionItemOnScreen = false;
                    break;
                case SHIELD:
                    if (game.assets.shieldUseSound != null) game.assets.shieldUseSound.play();
                    player.setShield(true);
                    shieldDurationTimer = GameConfig.SHIELD_DURATION; // 6 seconds
                    shieldItemOnScreen = false;
                    break;
                case LIFE:
                    if (game.assets.oneUpSound != null) game.assets.oneUpSound.play();
                    player.setLives(player.getLives() + 1);
                    oneUpItemOnScreen = false;
                    break;
            }
        }
    }

    private void cleanupItems() {
        Iterator<ItemEntity> it = items.iterator();
        while (it.hasNext()) {
            ItemEntity item = it.next();
            if (item.isCollected()) {
                it.remove();
            } else if (item.isExpired()) {
                // Clear the on-screen flags when items expire
                switch (item.type) {
                    case POTION: potionItemOnScreen = false; break;
                    case SHIELD: shieldItemOnScreen = false; break;
                    case LIFE: oneUpItemOnScreen = false; break;
                }
                it.remove();
            }
        }
    }

    private void checkBirdCollisions() {
        for (int i = 0; i < birds.size; i++) {
            BirdEntity a = birds.get(i);
            if (a.isInvisible() || a.getRemainingBounces() <= 0) continue;

            for (int j = i + 1; j < birds.size; j++) {
                BirdEntity b = birds.get(j);
                if (b.isInvisible() || b.getRemainingBounces() <= 0) continue;

                if (a.overlaps(b)) {
                    // Bird-bird collision: fixed bounce speed (10 * PPM)
                    a.velocityX = a.velocityX >= 0 ? GameConfig.BIRD_COLLISION_BOUNCE_SPEED : -GameConfig.BIRD_COLLISION_BOUNCE_SPEED;
                    a.velocityY = a.velocityY >= 0 ? GameConfig.BIRD_COLLISION_BOUNCE_SPEED : -GameConfig.BIRD_COLLISION_BOUNCE_SPEED;
                    b.velocityX = b.velocityX >= 0 ? GameConfig.BIRD_COLLISION_BOUNCE_SPEED : -GameConfig.BIRD_COLLISION_BOUNCE_SPEED;
                    b.velocityY = b.velocityY >= 0 ? GameConfig.BIRD_COLLISION_BOUNCE_SPEED : -GameConfig.BIRD_COLLISION_BOUNCE_SPEED;

                    // Separate using hitbox radii
                    float dx = a.getCenterX() - b.getCenterX();
                    float dy = a.getCenterY() - b.getCenterY();
                    float dist = (float) Math.sqrt(dx * dx + dy * dy);
                    if (dist > 0) {
                        float overlap = (a.getHitboxW() / 2 + b.getHitboxW() / 2) - dist;
                        if (overlap > 0) {
                            float nx = dx / dist;
                            float ny = dy / dist;
                            a.x += nx * overlap / 2;
                            a.y += ny * overlap / 2;
                            b.x -= nx * overlap / 2;
                            b.y -= ny * overlap / 2;
                        }
                    }
                }
            }
        }
    }

    // ========== GAME OVER ==========

    private void triggerGameOver() {
        state = State.GAME_OVER;
        gameStopped = true;
        dragging = false;

        game.assets.stopAllMusic();
        if (game.assets.gameoverMusic != null) game.assets.gameoverMusic.play();

        float popupW = 450;
        float popupH = 300;
        float popupX = GameConfig.CENTER_X - popupW / 2;
        float popupY = GameConfig.CENTER_Y - popupH / 2 + 30;

        Texture playTex = game.assets.gameOverPlayButton;
        float btnSpacing = 30;
        float btnY = popupY - playTex.getHeight() - 10;
        float playBtnX = GameConfig.CENTER_X - playTex.getWidth() - btnSpacing / 2;
        playButtonBounds = new Rectangle(playBtnX, btnY, playTex.getWidth(), playTex.getHeight());
    }

    private boolean handleGameOverTouch(float worldX, float worldY) {
        if (playButtonBounds != null && playButtonBounds.contains(worldX, worldY)) {
            if (game.assets.menuClickSound != null) game.assets.menuClickSound.play();
            game.assets.stopAllMusic();
            game.setScreen(new GameScreen(game));
            return true;
        }
        return false;
    }

    // ========== DRAWING ==========

    private void drawHUD(SpriteBatch batch) {
        BitmapFont font = game.assets.hudFont;
        if (font == null) return;

        // Time (top-left)
        font.draw(batch, "Time: " + String.format("%d:%02d", minutes, seconds), 10, GameConfig.CAMERA_HEIGHT - 10);

        // Lives (top-right)
        Texture lifeIcon = game.assets.lifeTexture;
        if (lifeIcon != null) {
            float iconX = GameConfig.CAMERA_WIDTH - 110;
            float iconY = GameConfig.CAMERA_HEIGHT - 10 - lifeIcon.getHeight();
            batch.draw(lifeIcon, iconX, iconY);
            font.draw(batch, "x " + player.getLives(), iconX + lifeIcon.getWidth() + 5, GameConfig.CAMERA_HEIGHT - 10);
        }

        // Shield timer indicator
        if (player.hasShield()) {
            Texture st = game.assets.shieldStatTexture;
            if (st != null) {
                batch.draw(st, 10, GameConfig.CAMERA_HEIGHT - 70);
                font.draw(batch, String.format("%.0f", Math.max(0, shieldDurationTimer)),
                    15 + st.getWidth(), GameConfig.CAMERA_HEIGHT - 45);
            }
        }

        // Potion timer indicator
        if (player.hasPotionInvisibility() && potionDurationTimer > 0) {
            Texture pt = game.assets.potionStatTexture;
            if (pt != null) {
                float py = player.hasShield() ? GameConfig.CAMERA_HEIGHT - 110 : GameConfig.CAMERA_HEIGHT - 70;
                batch.draw(pt, 10, py);
                font.draw(batch, String.format("%.0f", Math.max(0, potionDurationTimer)),
                    15 + pt.getWidth(), py + pt.getHeight() - 5);
            }
        }
    }

    private void drawGameOver(SpriteBatch batch) {
        float popupW = 450;
        float popupH = 300;
        float popupX = GameConfig.CENTER_X - popupW / 2;
        float popupY = GameConfig.CENTER_Y - popupH / 2 + 30;
        batch.draw(game.assets.gameoverPopup, popupX, popupY, popupW, popupH);

        BitmapFont font = game.assets.gameOverFont;
        if (font != null) {
            String timeStr = String.format("%d:%02d", minutes, seconds);
            layout.setText(font, timeStr);
            font.draw(batch, timeStr,
                GameConfig.CENTER_X - layout.width / 2,
                popupY + popupH * 0.52f);
        }

        if (playButtonBounds != null) {
            batch.draw(game.assets.gameOverPlayButton, playButtonBounds.x, playButtonBounds.y);
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }
}
