package br.com.tupinikimtecnologia.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;

import br.com.tupinikimtecnologia.Main;
import br.com.tupinikimtecnologia.config.GameConfig;

public class MenuScreen extends ScreenAdapter {

    private final Main game;
    private OrthographicCamera camera;
    private FitViewport viewport;

    private Rectangle playBounds;
    private Rectangle rankBounds;
    private Rectangle rateBounds;
    private Rectangle aboutBounds;

    private final Vector3 touchPoint = new Vector3();

    public MenuScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {
        camera = new OrthographicCamera();
        viewport = new FitViewport(GameConfig.CAMERA_WIDTH, GameConfig.CAMERA_HEIGHT, camera);
        camera.position.set(GameConfig.CENTER_X, GameConfig.CENTER_Y, 0);
        camera.update();

        Texture playTex = game.assets.playButton;
        Texture rankTex = game.assets.rankButton;
        Texture rateTex = game.assets.rateButton;
        Texture aboutTex = game.assets.aboutButton;

        // Button layout: Play and Rank side by side at bottom, Rate and About above
        playBounds = new Rectangle(
            290 - playTex.getWidth() / 2f,
            70 - playTex.getHeight() / 2f,
            playTex.getWidth(), playTex.getHeight());

        rankBounds = new Rectangle(
            510 - rankTex.getWidth() / 2f,
            70 - rankTex.getHeight() / 2f,
            rankTex.getWidth(), rankTex.getHeight());

        rateBounds = new Rectangle(
            290 - rateTex.getWidth() / 2f,
            162 - rateTex.getHeight() / 2f,
            rateTex.getWidth(), rateTex.getHeight());

        aboutBounds = new Rectangle(
            510 - aboutTex.getWidth() / 2f,
            162 - aboutTex.getHeight() / 2f,
            aboutTex.getWidth(), aboutTex.getHeight());

        // Start menu music
        game.assets.stopAllMusic();
        if (game.assets.menuMusic != null) {
            game.assets.menuMusic.play();
        }

        Gdx.input.setCatchKey(Input.Keys.BACK, true);
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                viewport.unproject(touchPoint.set(screenX, screenY, 0));

                if (playBounds.contains(touchPoint.x, touchPoint.y)) {
                    if (game.assets.menuClickSound != null) game.assets.menuClickSound.play();
                    game.assets.stopAllMusic();
                    game.setScreen(new LoadingScreen(game, new GameScreen(game)));
                    return true;
                }
                // About screen
                if (aboutBounds.contains(touchPoint.x, touchPoint.y)) {
                    if (game.assets.menuClickSound != null) game.assets.menuClickSound.play();
                    game.setScreen(new AboutScreen(game));
                    return true;
                }
                // Rank and Rate - not implemented (would need Google Play)
                return false;
            }

            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.BACK || keycode == Input.Keys.ESCAPE) {
                    Gdx.app.exit();
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();

        // Background
        game.batch.draw(game.assets.menuBackground, 0, 0, GameConfig.CAMERA_WIDTH, GameConfig.CAMERA_HEIGHT);

        // Logo centered near top
        Texture logoTex = game.assets.logo;
        game.batch.draw(logoTex,
            410 - logoTex.getWidth() / 2f,
            340 - logoTex.getHeight() / 2f);

        // Copyright at bottom center
        Texture copTex = game.assets.copyrightMenu;
        game.batch.draw(copTex,
            GameConfig.CENTER_X - copTex.getWidth() / 2f,
            15 - copTex.getHeight() / 2f);

        // Buttons
        game.batch.draw(game.assets.playButton, playBounds.x, playBounds.y);
        game.batch.draw(game.assets.rankButton, rankBounds.x, rankBounds.y);
        game.batch.draw(game.assets.rateButton, rateBounds.x, rateBounds.y);
        game.batch.draw(game.assets.aboutButton, aboutBounds.x, aboutBounds.y);

        game.batch.end();
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
