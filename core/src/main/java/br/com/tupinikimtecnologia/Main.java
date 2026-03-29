package br.com.tupinikimtecnologia;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import br.com.tupinikimtecnologia.manager.Assets;
import br.com.tupinikimtecnologia.screen.SplashScreen;

public class Main extends Game {

    public Assets assets;
    public SpriteBatch batch;

    @Override
    public void create() {
        batch = new SpriteBatch();
        assets = new Assets();
        setScreen(new SplashScreen(this));
    }

    @Override
    public void dispose() {
        super.dispose();
        if (batch != null) batch.dispose();
        if (assets != null) assets.dispose();
    }
}
