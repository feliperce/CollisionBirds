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

    // Timers
    private float addBirdTimer;
    private float gameTimerSeconds;
    private int minutes;
    private int seconds;

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

        // Initialize game state
        birds.clear();
        addBirdTimer = 0;
        gameTimerSeconds = 0;
        minutes = 0;
        seconds = 0;
        state = State.PLAYING;
        dragging = false;

        // Create player at center
        player = new PlayerEntity(GameConfig.CENTER_X, GameConfig.CENTER_Y, game.assets.playerSheet);

        // Catch Android back key
        Gdx.input.setCatchKey(Input.Keys.BACK, true);

        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                viewport.unproject(touchPoint.set(screenX, screenY, 0));

                if (state == State.GAME_OVER) {
                    return handleGameOverTouch(touchPoint.x, touchPoint.y);
                }

                // Start drag if touching player
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

        // Background
        batch.draw(game.assets.gameBackground, 0, 0, GameConfig.CAMERA_WIDTH, GameConfig.CAMERA_HEIGHT);

        // Draw birds
        for (BirdEntity bird : birds) {
            bird.draw(batch);
        }

        // Draw player
        player.draw(batch);

        // Draw HUD
        if (state == State.PLAYING) {
            drawHUD(batch);
        }

        // Draw game over overlay
        if (state == State.GAME_OVER) {
            drawGameOver(batch);
        }

        batch.end();
    }

    private void updateGame(float delta) {
        // Update game timer
        gameTimerSeconds += delta;
        int totalSeconds = (int) gameTimerSeconds;
        minutes = totalSeconds / 60;
        seconds = totalSeconds % 60;

        // Update player
        player.update(delta);

        // Bird spawn timer
        addBirdTimer += delta;
        if (addBirdTimer >= GameConfig.ADD_BIRD_TIMER_DELAY) {
            addBirdTimer -= GameConfig.ADD_BIRD_TIMER_DELAY;
            spawnBird();
        }

        // Update birds
        for (BirdEntity bird : birds) {
            bird.update(delta);
        }

        // Bird-bird collision
        checkBirdCollisions();

        // Remove dead birds
        checkBirdDeaths();

        // Check player-bird collision
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

    private void checkBirdCollisions() {
        for (int i = 0; i < birds.size; i++) {
            BirdEntity a = birds.get(i);
            if (a.isDead() || a.isImmortal() || a.getHealth() <= 0) continue;

            for (int j = i + 1; j < birds.size; j++) {
                BirdEntity b = birds.get(j);
                if (b.isDead() || b.isImmortal() || b.getHealth() <= 0) continue;

                if (a.overlaps(b)) {
                    // Bounce: preserve direction, normalize speed
                    a.velocityX = a.velocityX >= 0 ? GameConfig.BIRD_BOUNCE_SPEED : -GameConfig.BIRD_BOUNCE_SPEED;
                    a.velocityY = a.velocityY >= 0 ? GameConfig.BIRD_BOUNCE_SPEED : -GameConfig.BIRD_BOUNCE_SPEED;
                    b.velocityX = b.velocityX >= 0 ? GameConfig.BIRD_BOUNCE_SPEED : -GameConfig.BIRD_BOUNCE_SPEED;
                    b.velocityY = b.velocityY >= 0 ? GameConfig.BIRD_BOUNCE_SPEED : -GameConfig.BIRD_BOUNCE_SPEED;

                    // Separate birds to prevent sticking
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
                bird.markDead();
                it.remove();
            }
        }
    }

    private void checkPlayerCollision() {
        for (BirdEntity bird : birds) {
            if (!bird.isImmortal() && !bird.isDead() && bird.getHealth() > 0) {
                if (bird.overlaps(player.x, player.y, player.width, player.height)) {
                    displayGameOver();
                    return;
                }
            }
        }
    }

    private void displayGameOver() {
        state = State.GAME_OVER;
        dragging = false;

        // Original rendered popup at 450x300 (stretched from 256x206)
        // Popup center in original: (camera.centerX, camera.centerY + 30) = (400, 270)
        float popupW = 450;
        float popupH = 300;
        float popupX = GameConfig.CENTER_X - popupW / 2; // 175
        float popupY = GameConfig.CENTER_Y - popupH / 2 + 30; // 120

        Texture playTex = game.assets.gameOverPlayButton;
        Texture fbTex = game.assets.facebookButton;

        // Original button positions (AnchorCenter coords → converted to bottom-left):
        // play center: (gameOver.x/2 + 75, gameOver.y/2 - 65) = (275, 70)
        // facebook center: (play.x * 2 - 25, play.y) = (525, 70)
        float playBtnX = 275 - playTex.getWidth() / 2f; // 175
        float playBtnY = 70 - playTex.getHeight() / 2f;  // 31
        playButtonBounds = new Rectangle(playBtnX, playBtnY, playTex.getWidth(), playTex.getHeight());

        float fbBtnX = 525 - fbTex.getWidth() / 2f; // 425
        facebookButtonBounds = new Rectangle(fbBtnX, playBtnY, fbTex.getWidth(), fbTex.getHeight());
    }

    private boolean handleGameOverTouch(float worldX, float worldY) {
        if (playButtonBounds != null && playButtonBounds.contains(worldX, worldY)) {
            game.setScreen(new LoadingScreen(game, new GameScreen(game)));
            return true;
        }
        return false;
    }

    private void drawHUD(SpriteBatch batch) {
        BitmapFont font = game.assets.hudFont;
        if (font == null) return;

        String timeStr = minutes + "." + seconds;
        font.draw(batch, "Time:", 10, GameConfig.CAMERA_HEIGHT - 10);
        font.draw(batch, timeStr, 150, GameConfig.CAMERA_HEIGHT - 10);
    }

    private void drawGameOver(SpriteBatch batch) {
        // Draw popup stretched to 450x300 (matching original GameOverWindow)
        float popupW = 450;
        float popupH = 300;
        float popupX = GameConfig.CENTER_X - popupW / 2;
        float popupY = GameConfig.CENTER_Y - popupH / 2 + 30;
        batch.draw(game.assets.gameoverPopup, popupX, popupY, popupW, popupH);

        // Draw score text - original: Text at (centerX-20, centerY) with anchorCenter(0,0)
        // anchorCenter(0,0) means bottom-left anchor, so text bottom-left is at (380, 240)
        BitmapFont font = game.assets.gameOverFont;
        if (font != null) {
            String timeStr = minutes + "." + seconds;
            layout.setText(font, timeStr);
            // Position text so it appears centered on the popup
            font.draw(batch,
                timeStr,
                GameConfig.CENTER_X - layout.width / 2,
                GameConfig.CENTER_Y + layout.height + 30);
        }

        // Draw buttons
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
