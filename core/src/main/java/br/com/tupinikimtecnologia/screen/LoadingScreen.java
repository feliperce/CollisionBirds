package br.com.tupinikimtecnologia.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.utils.viewport.FitViewport;

import br.com.tupinikimtecnologia.Main;
import br.com.tupinikimtecnologia.config.GameConfig;

/**
 * Brief loading screen shown during screen transitions.
 * Matches the original LoadingScene behavior.
 */
public class LoadingScreen extends ScreenAdapter {

    private final Main game;
    private final Screen nextScreen;
    private OrthographicCamera camera;
    private FitViewport viewport;
    private boolean rendered;

    public LoadingScreen(Main game, Screen nextScreen) {
        this.game = game;
        this.nextScreen = nextScreen;
    }

    @Override
    public void show() {
        camera = new OrthographicCamera();
        viewport = new FitViewport(GameConfig.CAMERA_WIDTH, GameConfig.CAMERA_HEIGHT, camera);
        camera.position.set(GameConfig.CENTER_X, GameConfig.CENTER_Y, 0);
        camera.update();
        rendered = false;
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();

        BitmapFont font = game.assets.hudFont;
        if (font != null) {
            GlyphLayout layout = new GlyphLayout(font, "Loading...");
            font.draw(game.batch, "Loading...",
                GameConfig.CENTER_X - layout.width / 2,
                GameConfig.CENTER_Y + layout.height / 2);
        }

        game.batch.end();

        // Show loading for 1 frame, then switch to next screen
        if (rendered) {
            game.setScreen(nextScreen);
        }
        rendered = true;
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }
}
