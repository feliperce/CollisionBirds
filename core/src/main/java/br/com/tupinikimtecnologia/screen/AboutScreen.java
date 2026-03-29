package br.com.tupinikimtecnologia.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.viewport.FitViewport;

import br.com.tupinikimtecnologia.Main;
import br.com.tupinikimtecnologia.config.GameConfig;

public class AboutScreen extends ScreenAdapter {

    private final Main game;
    private OrthographicCamera camera;
    private FitViewport viewport;

    public AboutScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {
        camera = new OrthographicCamera();
        viewport = new FitViewport(GameConfig.CAMERA_WIDTH, GameConfig.CAMERA_HEIGHT, camera);
        camera.position.set(GameConfig.CENTER_X, GameConfig.CENTER_Y, 0);
        camera.update();

        Gdx.input.setCatchKey(Input.Keys.BACK, true);
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                // Any touch goes back to menu
                if (game.assets.menuClickSound != null) game.assets.menuClickSound.play();
                game.setScreen(new MenuScreen(game));
                return true;
            }

            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.BACK || keycode == Input.Keys.ESCAPE) {
                    game.setScreen(new MenuScreen(game));
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
        Texture bg = game.assets.aboutBackground;
        game.batch.draw(bg, 0, 0, GameConfig.CAMERA_WIDTH, GameConfig.CAMERA_HEIGHT);

        // About content centered
        Texture content = game.assets.aboutContent;
        game.batch.draw(content,
            GameConfig.CENTER_X - content.getWidth() / 2f,
            GameConfig.CENTER_Y - content.getHeight() / 2f);

        // Copyright
        Texture cop = game.assets.copyrightMenu;
        game.batch.draw(cop,
            GameConfig.CENTER_X - cop.getWidth() / 2f,
            10);

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
