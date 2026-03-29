package br.com.tupinikimtecnologia.manager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;

public class Assets {

    // Splash
    public Texture splashTexture;

    // Menu
    public Texture menuBackground;
    public Texture logo;
    public Texture copyrightMenu;
    public Texture playButton;
    public Texture rankButton;
    public Texture rateButton;

    // Game
    public Texture gameBackground;
    public Texture birdVermelhoSheet;
    public Texture birdVerdeSheet;
    public Texture birdAmareloSheet;
    public Texture birdVioletaSheet;
    public Texture playerSheet;

    // Game Over
    public Texture gameoverPopup;
    public Texture facebookButton;
    public Texture gameOverPlayButton;

    // Fonts
    public BitmapFont hudFont;
    public BitmapFont gameOverFont;

    public void loadSplash() {
        splashTexture = loadTexture("gfx/splash.png");
    }

    public void loadAll() {
        // Dispose splash if still loaded
        if (splashTexture != null) {
            splashTexture.dispose();
            splashTexture = null;
        }

        // Menu textures
        menuBackground = loadTexture("gfx/menu/background.png");
        logo = loadTexture("gfx/menu/logo_cbirds.png");
        copyrightMenu = loadTexture("gfx/menu/copyright_tupinikim.png");
        playButton = loadTexture("gfx/menu/button_play.png");
        rankButton = loadTexture("gfx/menu/button_rank.png");
        rateButton = loadTexture("gfx/menu/button_rate.png");

        // Game textures
        gameBackground = loadTexture("gfx/game/background-game.png");
        birdVermelhoSheet = loadTexture("gfx/game/birdVermelho.png");
        birdVerdeSheet = loadTexture("gfx/game/birdVerde.png");
        birdAmareloSheet = loadTexture("gfx/game/birdAmarelo.png");
        birdVioletaSheet = loadTexture("gfx/game/birdVioleta.png");
        playerSheet = loadTexture("gfx/game/birdPlayer.png");

        // Game over textures
        gameoverPopup = loadTexture("gfx/menu/popup_gameover.png");
        facebookButton = loadTexture("gfx/menu/button_facebook.png");
        gameOverPlayButton = loadTexture("gfx/menu/button_play.png");

        // Fonts
        loadFonts();
    }

    private Texture loadTexture(String path) {
        Texture tex = new Texture(Gdx.files.internal(path));
        tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        return tex;
    }

    private void loadFonts() {
        try {
            FreeTypeFontGenerator gen = new FreeTypeFontGenerator(Gdx.files.internal("font/8-BIT WONDER.TTF"));
            FreeTypeFontParameter param = new FreeTypeFontParameter();
            param.size = 30;
            param.color = Color.WHITE;
            param.borderWidth = 2;
            param.borderColor = Color.BLACK;
            hudFont = gen.generateFont(param);
            gen.dispose();

            gen = new FreeTypeFontGenerator(Gdx.files.internal("font/Square.ttf"));
            param = new FreeTypeFontParameter();
            param.size = 40;
            param.color = Color.BLACK;
            param.borderWidth = 2;
            param.borderColor = Color.BLACK;
            gameOverFont = gen.generateFont(param);
            gen.dispose();
        } catch (Exception e) {
            hudFont = new BitmapFont();
            gameOverFont = new BitmapFont();
        }
    }

    public void dispose() {
        safeDispose(splashTexture);
        safeDispose(menuBackground);
        safeDispose(logo);
        safeDispose(copyrightMenu);
        safeDispose(playButton);
        safeDispose(rankButton);
        safeDispose(rateButton);
        safeDispose(gameBackground);
        safeDispose(birdVermelhoSheet);
        safeDispose(birdVerdeSheet);
        safeDispose(birdAmareloSheet);
        safeDispose(birdVioletaSheet);
        safeDispose(playerSheet);
        safeDispose(gameoverPopup);
        safeDispose(facebookButton);
        safeDispose(gameOverPlayButton);
        safeDispose(hudFont);
        safeDispose(gameOverFont);
    }

    private void safeDispose(com.badlogic.gdx.utils.Disposable d) {
        if (d != null) d.dispose();
    }
}
