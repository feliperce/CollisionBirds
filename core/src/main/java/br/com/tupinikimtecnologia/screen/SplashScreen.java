package br.com.tupinikimtecnologia.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.viewport.FitViewport;

import br.com.tupinikimtecnologia.Main;
import br.com.tupinikimtecnologia.config.GameConfig;

public class SplashScreen extends ScreenAdapter {

    private final Main game;
    private OrthographicCamera camera;
    private FitViewport viewport;
    private float timer;

    public SplashScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {
        camera = new OrthographicCamera();
        viewport = new FitViewport(GameConfig.CAMERA_WIDTH, GameConfig.CAMERA_HEIGHT, camera);
        camera.position.set(GameConfig.CENTER_X, GameConfig.CENTER_Y, 0);
        camera.update();
        timer = 0;

        // Catch back key during splash (ignore it, matching original)
        Gdx.input.setCatchKey(Input.Keys.BACK, true);

        game.assets.loadSplash();
    }

    @Override
    public void render(float delta) {
        timer += delta;

        Gdx.gl.glClearColor(0f, 85f / 255f, 26f / 255f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();

        float splashW = game.assets.splashTexture.getWidth() * 1.5f;
        float splashH = game.assets.splashTexture.getHeight() * 1.5f;
        float splashX = GameConfig.CENTER_X - splashW / 2;
        float splashY = GameConfig.CENTER_Y - splashH / 2;
        game.batch.draw(game.assets.splashTexture, splashX, splashY, splashW, splashH);

        game.batch.end();

        if (timer >= GameConfig.SPLASH_DURATION) {
            game.assets.loadAll();
            game.setScreen(new MenuScreen(game));
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }
}
