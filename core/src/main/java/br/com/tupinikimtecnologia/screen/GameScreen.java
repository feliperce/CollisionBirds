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

public class GameScreen extends ScreenAdapter {

    private enum State { PLAYING, GAME_OVER }

    private final Main game;
    private OrthographicCamera camera;
    private FitViewport viewport;

    // Game entities
    private PlayerEntity player;
    private final Array<BirdEntity> birds = new Array<>();
    private final Array<ItemEntity> items = new Array<>();

    // Player state
    private int lives;
    private boolean shieldActive;   // from shield item pickup
    private boolean potionActive;   // from potion item pickup
    private boolean graceActive;    // brief invincibility after taking damage
    private float shieldTimer;
    private float potionTimer;
    private float graceTimer;

    // Timers
    private float addBirdTimer;
    private float gameTimerSeconds;
    private int minutes;
    private int seconds;
    private int birdsKilled;

    // Game state
    private State state;
    private boolean dragging;

    // Game over UI bounds
    private Rectangle playButtonBounds;
    private Rectangle facebookButtonBounds;

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
        gameTimerSeconds = 0;
        minutes = 0;
        seconds = 0;
        birdsKilled = 0;
        lives = GameConfig.PLAYER_INITIAL_LIVES;
        shieldActive = false;
        potionActive = false;
        graceActive = false;
        shieldTimer = 0;
        potionTimer = 0;
        graceTimer = 0;
        state = State.PLAYING;
        dragging = false;

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
                    game.setScreen(new LoadingScreen(game, new MenuScreen(game)));
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

        for (ItemEntity item : items) {
            item.draw(batch);
        }
        for (BirdEntity bird : birds) {
            bird.draw(batch);
        }
        player.draw(batch);

        if (state == State.PLAYING) {
            drawHUD(batch);
        }
        if (state == State.GAME_OVER) {
            drawGameOver(batch);
        }

        batch.end();
    }

    private void updateGame(float delta) {
        gameTimerSeconds += delta;
        int totalSeconds = (int) gameTimerSeconds;
        minutes = totalSeconds / 60;
        seconds = totalSeconds % 60;

        player.update(delta);

        // Countdown timers for power-ups and grace period
        if (shieldActive) {
            shieldTimer -= delta;
            if (shieldTimer <= 0) {
                shieldActive = false;
                if (game.assets.shieldLostSound != null) game.assets.shieldLostSound.play();
            }
        }
        if (potionActive) {
            potionTimer -= delta;
            if (potionTimer <= 0) {
                potionActive = false;
            }
        }
        if (graceActive) {
            graceTimer -= delta;
            if (graceTimer <= 0) {
                graceActive = false;
            }
        }

        // Bird spawn timer
        addBirdTimer += delta;
        if (addBirdTimer >= GameConfig.ADD_BIRD_TIMER_DELAY) {
            addBirdTimer -= GameConfig.ADD_BIRD_TIMER_DELAY;
            spawnBird();
        }

        // Item spawn - only 1/160 chance per frame AND only if no items on screen
        trySpawnItem();

        for (BirdEntity bird : birds) {
            bird.update(delta);
        }
        for (ItemEntity item : items) {
            item.update(delta);
        }

        checkBirdCollisions();
        checkBirdDeaths();
        cleanupItems();
        checkItemPickup();
        checkPlayerCollision();
    }

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

    private void trySpawnItem() {
        // Don't spawn if there's already an item on screen
        for (ItemEntity item : items) {
            if (!item.isCollected() && !item.isExpired()) return;
        }
        // Don't spawn if a power-up is already active
        if (shieldActive || potionActive) return;

        // 1/160 chance per frame (~once every 2.7 seconds at 60fps)
        if (MathUtils.random(GameConfig.ITEM_SPAWN_DENOMINATOR - 1) != 0) return;

        float x = MathUtils.random(GameConfig.SPAWN_MARGIN, GameConfig.CAMERA_WIDTH - GameConfig.SPAWN_MARGIN);
        float y = MathUtils.random(GameConfig.SPAWN_MARGIN, GameConfig.CAMERA_HEIGHT - GameConfig.SPAWN_MARGIN);

        // 60% shield, 40% potion
        if (MathUtils.random(9) < 6) {
            items.add(new ItemEntity(x, y, game.assets.shieldStatTexture, ItemEntity.ItemType.SHIELD));
        } else {
            items.add(new ItemEntity(x, y, game.assets.potionStatTexture, ItemEntity.ItemType.POTION));
        }
    }

    private void checkItemPickup() {
        for (ItemEntity item : items) {
            if (item.isCollected() || item.isExpired()) continue;
            if (item.overlapsPlayer(player.x, player.y, player.width, player.height)) {
                item.collect();
                switch (item.type) {
                    case SHIELD:
                        shieldActive = true;
                        shieldTimer = GameConfig.SHIELD_DURATION;
                        if (game.assets.shieldUseSound != null) game.assets.shieldUseSound.play();
                        break;
                    case POTION:
                        potionActive = true;
                        potionTimer = GameConfig.POTION_DURATION;
                        if (game.assets.potionUseSound != null) game.assets.potionUseSound.play();
                        break;
                    case LIFE:
                        lives++;
                        if (game.assets.oneUpSound != null) game.assets.oneUpSound.play();
                        break;
                }
            }
        }
    }

    private void cleanupItems() {
        Iterator<ItemEntity> it = items.iterator();
        while (it.hasNext()) {
            ItemEntity item = it.next();
            if (item.isCollected() || item.isExpired()) {
                it.remove();
            }
        }
    }

    private void checkBirdCollisions() {
        for (int i = 0; i < birds.size; i++) {
            BirdEntity a = birds.get(i);
            if (a.isDead() || a.isImmortal() || a.getHealth() <= 0) continue;

            for (int j = i + 1; j < birds.size; j++) {
                BirdEntity b = birds.get(j);
                if (b.isDead() || b.isImmortal() || b.getHealth() <= 0) continue;

                if (a.overlaps(b)) {
                    a.velocityX = a.velocityX >= 0 ? GameConfig.BIRD_BOUNCE_SPEED : -GameConfig.BIRD_BOUNCE_SPEED;
                    a.velocityY = a.velocityY >= 0 ? GameConfig.BIRD_BOUNCE_SPEED : -GameConfig.BIRD_BOUNCE_SPEED;
                    b.velocityX = b.velocityX >= 0 ? GameConfig.BIRD_BOUNCE_SPEED : -GameConfig.BIRD_BOUNCE_SPEED;
                    b.velocityY = b.velocityY >= 0 ? GameConfig.BIRD_BOUNCE_SPEED : -GameConfig.BIRD_BOUNCE_SPEED;

                    float dx = a.getCenterX() - b.getCenterX();
                    float dy = a.getCenterY() - b.getCenterY();
                    float dist = (float) Math.sqrt(dx * dx + dy * dy);
                    if (dist > 0) {
                        float overlap = (a.width / 2 + b.width / 2) - dist;
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

    private void checkBirdDeaths() {
        Iterator<BirdEntity> it = birds.iterator();
        while (it.hasNext()) {
            BirdEntity bird = it.next();
            if (bird.getHealth() <= 0 && bird.canDie()) {
                birdsKilled++;
                // Red birds drop 1-UP (extra life)
                if (bird.getType() == BirdType.VERMELHO) {
                    items.add(new ItemEntity(
                        bird.getCenterX(), bird.getCenterY(),
                        game.assets.lifeTexture, ItemEntity.ItemType.LIFE));
                }
                bird.markDead();
                it.remove();
            }
        }
    }

    private void checkPlayerCollision() {
        // Player is invincible if shield, potion, or grace period is active
        boolean isInvincible = shieldActive || potionActive || graceActive;

        for (BirdEntity bird : birds) {
            if (!bird.isImmortal() && !bird.isDead() && bird.getHealth() > 0) {
                if (bird.overlaps(player.x, player.y, player.width, player.height)) {
                    if (isInvincible) {
                        if (game.assets.punchSound != null) game.assets.punchSound.play();
                    } else {
                        // Take damage
                        lives--;
                        if (game.assets.playerDieSound != null) game.assets.playerDieSound.play();

                        if (lives <= 0) {
                            lives = 0;
                            displayGameOver();
                            return;
                        }
                        // Grace period after damage (separate from shield/potion)
                        graceActive = true;
                        graceTimer = 2f;
                    }
                    return;
                }
            }
        }
    }

    private void displayGameOver() {
        state = State.GAME_OVER;
        dragging = false;

        game.assets.stopAllMusic();
        if (game.assets.gameoverMusic != null) game.assets.gameoverMusic.play();

        float popupW = 450;
        float popupH = 300;
        float popupX = GameConfig.CENTER_X - popupW / 2;
        float popupY = GameConfig.CENTER_Y - popupH / 2 + 30;

        Texture playTex = game.assets.gameOverPlayButton;
        Texture fbTex = game.assets.facebookButton;

        float btnSpacing = 30;
        float btnY = popupY - playTex.getHeight() - 10;
        float playBtnX = GameConfig.CENTER_X - playTex.getWidth() - btnSpacing / 2;
        playButtonBounds = new Rectangle(playBtnX, btnY, playTex.getWidth(), playTex.getHeight());

        float fbBtnX = GameConfig.CENTER_X + btnSpacing / 2;
        facebookButtonBounds = new Rectangle(fbBtnX, btnY, fbTex.getWidth(), fbTex.getHeight());
    }

    private boolean handleGameOverTouch(float worldX, float worldY) {
        if (playButtonBounds != null && playButtonBounds.contains(worldX, worldY)) {
            if (game.assets.menuClickSound != null) game.assets.menuClickSound.play();
            game.assets.stopAllMusic();
            game.setScreen(new LoadingScreen(game, new GameScreen(game)));
            return true;
        }
        return false;
    }

    private void drawHUD(SpriteBatch batch) {
        BitmapFont font = game.assets.hudFont;
        if (font == null) return;

        // Time (top-left)
        font.draw(batch, "Time: " + minutes + "." + seconds, 10, GameConfig.CAMERA_HEIGHT - 10);

        // Lives (top-right) - heart icon + count
        Texture heart = game.assets.lifeStatTexture;
        if (heart != null) {
            float heartX = GameConfig.CAMERA_WIDTH - 110;
            float heartY = GameConfig.CAMERA_HEIGHT - 10 - heart.getHeight();
            batch.draw(heart, heartX, heartY);
            font.draw(batch, "x " + lives, heartX + heart.getWidth() + 5, GameConfig.CAMERA_HEIGHT - 10);
        }

        // Shield indicator (top-left, below time)
        if (shieldActive && game.assets.shieldStatTexture != null) {
            Texture st = game.assets.shieldStatTexture;
            batch.draw(st, 10, GameConfig.CAMERA_HEIGHT - 70);
            font.draw(batch, String.format("%.0f", Math.max(0, shieldTimer)),
                15 + st.getWidth(), GameConfig.CAMERA_HEIGHT - 45);
        }

        // Potion indicator
        if (potionActive && game.assets.potionStatTexture != null) {
            Texture pt = game.assets.potionStatTexture;
            float potY = shieldActive ? GameConfig.CAMERA_HEIGHT - 110 : GameConfig.CAMERA_HEIGHT - 70;
            batch.draw(pt, 10, potY);
            font.draw(batch, String.format("%.0f", Math.max(0, potionTimer)),
                15 + pt.getWidth(), potY + pt.getHeight() - 5);
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
            String timeStr = minutes + "." + seconds;
            layout.setText(font, timeStr);
            font.draw(batch, timeStr,
                GameConfig.CENTER_X - layout.width / 2,
                popupY + popupH * 0.52f);
        }

        if (playButtonBounds != null) {
            batch.draw(game.assets.gameOverPlayButton, playButtonBounds.x, playButtonBounds.y);
        }
        if (facebookButtonBounds != null) {
            batch.draw(game.assets.facebookButton, facebookButtonBounds.x, facebookButtonBounds.y);
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
