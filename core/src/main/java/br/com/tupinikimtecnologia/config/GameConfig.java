package br.com.tupinikimtecnologia.config;

public class GameConfig {

    // Camera / world
    public static final float CAMERA_WIDTH = 800;
    public static final float CAMERA_HEIGHT = 480;
    public static final float CENTER_X = CAMERA_WIDTH / 2;
    public static final float CENTER_Y = CAMERA_HEIGHT / 2;

    // Player
    public static final int PLAYER_INITIAL_LIVES = 5;

    // Bird spawn & timers
    public static final float ADD_BIRD_TIMER_DELAY = 5f;
    public static final float BIRD_IMMORTAL_TIMER_DELAY = 3f;
    public static final float BIRD_DEATH_TIMER_DELAY = 4f;
    public static final float SPAWN_MARGIN = 30f;

    // Velocity (pixels per second) - original Box2D values * PPM(32)
    public static final float BIRD_INITIAL_VX = -160f;
    public static final float BIRD_INITIAL_VY = 320f;
    public static final float BIRD_BOUNCE_SPEED = 320f;

    // Items
    public static final float SHIELD_DURATION = 6f;
    public static final float POTION_DURATION = 6f;
    public static final int ITEM_SPAWN_DENOMINATOR = 160; // 1/160 chance per frame
    public static final float ITEM_LIFETIME = 8f; // seconds before item disappears

    // Animation
    public static final float FRAME_DURATION = 0.2f;

    // Splash
    public static final float SPLASH_DURATION = 2f;
}
