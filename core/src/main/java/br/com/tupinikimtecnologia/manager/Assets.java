package br.com.tupinikimtecnologia.manager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
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
    public Texture aboutButton;

    // Game
    public Texture gameBackground;
    public Texture birdVermelhoSheet;
    public Texture birdVerdeSheet;
    public Texture birdAmareloSheet;
    public Texture birdVioletaSheet;
    public Texture playerSheet;

    // Game items & HUD
    public Texture lifeTexture;       // heart icon for 1-UP drops
    public Texture lifeStatTexture;   // heart HUD indicator
    public Texture shieldStatTexture; // shield item/HUD
    public Texture potionStatTexture; // potion item/HUD

    // Game Over
    public Texture gameoverPopup;
    public Texture gameOverPlayButton;

    // About
    public Texture aboutBackground;
    public Texture aboutContent;

    // Fonts
    public BitmapFont hudFont;
    public BitmapFont gameOverFont;

    // Music
    public Music menuMusic;
    public Music gameMusic;
    public Music gameoverMusic;

    // Sound effects
    public Sound menuClickSound;
    public Sound playerDieSound;
    public Sound punchSound;
    public Sound oneUpSound;
    public Sound potionUseSound;
    public Sound shieldUseSound;
    public Sound shieldLostSound;

    public void loadSplash() {
        splashTexture = loadTexture("gfx/splash.png");
    }

    public void loadAll() {
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
        aboutButton = loadTexture("gfx/menu/button_about.png");

        // Game textures
        gameBackground = loadTexture("gfx/game/background-game.png");
        birdVermelhoSheet = loadTexture("gfx/game/birdVermelho.png");
        birdVerdeSheet = loadTexture("gfx/game/birdVerde.png");
        birdAmareloSheet = loadTexture("gfx/game/birdAmarelo.png");
        birdVioletaSheet = loadTexture("gfx/game/birdVioleta.png");
        playerSheet = loadTexture("gfx/game/birdPlayer.png");

        // Game items & HUD
        lifeTexture = loadTexture("gfx/game/life.png");
        lifeStatTexture = loadTexture("gfx/game/life-stat.png");
        shieldStatTexture = loadTexture("gfx/game/shield-stat.png");
        potionStatTexture = loadTexture("gfx/game/potioninv-stat.png");

        // Game over
        gameoverPopup = loadTexture("gfx/menu/popup_gameover.png");
        gameOverPlayButton = loadTexture("gfx/menu/button_play.png");

        // About
        aboutBackground = loadTexture("gfx/sobre/background-about.png");
        aboutContent = loadTexture("gfx/sobre/about.png");

        // Fonts
        loadFonts();

        // Audio
        loadAudio();
    }

    private Texture loadTexture(String path) {
        Texture tex = new Texture(Gdx.files.internal(path));
        tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        return tex;
    }

    private void loadFonts() {
        try {
            FreeTypeFontGenerator gen = new FreeTypeFontGenerator(Gdx.files.internal("font/Square.ttf"));
            FreeTypeFontParameter param = new FreeTypeFontParameter();
            param.size = 28;
            param.color = Color.WHITE;
            param.borderWidth = 2;
            param.borderColor = Color.BLACK;
            hudFont = gen.generateFont(param);
            gen.dispose();

            gen = new FreeTypeFontGenerator(Gdx.files.internal("font/Square.ttf"));
            param = new FreeTypeFontParameter();
            param.size = 40;
            param.color = Color.BLACK;
            param.borderWidth = 1;
            param.borderColor = Color.BLACK;
            gameOverFont = gen.generateFont(param);
            gen.dispose();
        } catch (Exception e) {
            hudFont = new BitmapFont();
            gameOverFont = new BitmapFont();
        }
    }

    private void loadAudio() {
        try {
            menuMusic = Gdx.audio.newMusic(Gdx.files.internal("mfx/menu-music.ogg"));
            menuMusic.setLooping(true);
            gameMusic = Gdx.audio.newMusic(Gdx.files.internal("mfx/game-music.ogg"));
            gameMusic.setLooping(true);
            gameoverMusic = Gdx.audio.newMusic(Gdx.files.internal("mfx/gameover-music.ogg"));
            gameoverMusic.setLooping(true);

            menuClickSound = Gdx.audio.newSound(Gdx.files.internal("sfx/menu-click.ogg"));
            playerDieSound = Gdx.audio.newSound(Gdx.files.internal("sfx/player-die.ogg"));
            punchSound = Gdx.audio.newSound(Gdx.files.internal("sfx/punch.ogg"));
            oneUpSound = Gdx.audio.newSound(Gdx.files.internal("sfx/1up.ogg"));
            potionUseSound = Gdx.audio.newSound(Gdx.files.internal("sfx/potioninv-use.ogg"));
            shieldUseSound = Gdx.audio.newSound(Gdx.files.internal("sfx/shield-use.ogg"));
            shieldLostSound = Gdx.audio.newSound(Gdx.files.internal("sfx/shield-lost.ogg"));
        } catch (Exception e) {
            // Audio might not be available on all platforms
        }
    }

    public void stopAllMusic() {
        if (menuMusic != null && menuMusic.isPlaying()) menuMusic.stop();
        if (gameMusic != null && gameMusic.isPlaying()) gameMusic.stop();
        if (gameoverMusic != null && gameoverMusic.isPlaying()) gameoverMusic.stop();
    }

    public void dispose() {
        safeDispose(splashTexture);
        safeDispose(menuBackground);
        safeDispose(logo);
        safeDispose(copyrightMenu);
        safeDispose(playButton);
        safeDispose(rankButton);
        safeDispose(rateButton);
        safeDispose(aboutButton);
        safeDispose(gameBackground);
        safeDispose(birdVermelhoSheet);
        safeDispose(birdVerdeSheet);
        safeDispose(birdAmareloSheet);
        safeDispose(birdVioletaSheet);
        safeDispose(playerSheet);
        safeDispose(lifeTexture);
        safeDispose(lifeStatTexture);
        safeDispose(shieldStatTexture);
        safeDispose(potionStatTexture);
        safeDispose(gameoverPopup);
        safeDispose(gameOverPlayButton);
        safeDispose(aboutBackground);
        safeDispose(aboutContent);
        safeDispose(hudFont);
        safeDispose(gameOverFont);
        safeDispose(menuMusic);
        safeDispose(gameMusic);
        safeDispose(gameoverMusic);
        safeDispose(menuClickSound);
        safeDispose(playerDieSound);
        safeDispose(punchSound);
        safeDispose(oneUpSound);
        safeDispose(potionUseSound);
        safeDispose(shieldUseSound);
        safeDispose(shieldLostSound);
    }

    private void safeDispose(com.badlogic.gdx.utils.Disposable d) {
        if (d != null) d.dispose();
    }
}
