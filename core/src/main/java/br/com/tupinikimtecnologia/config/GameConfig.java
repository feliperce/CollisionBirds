package br.com.tupinikimtecnologia.config;

public class GameConfig {

    public static final float CAMERA_WIDTH = 800;
    public static final float CAMERA_HEIGHT = 480;
    public static final float CENTER_X = CAMERA_WIDTH / 2;
    public static final float CENTER_Y = CAMERA_HEIGHT / 2;

    public static final int PLAYER_INITIAL_LIVES = 5;

    // Bird spawn
    public static final float ADD_BIRD_TIMER_DELAY = 5f;
    public static final float BIRD_APPEAR_DELAY = 3f;       // invisible for 3s after spawn
    public static final float BIRD_REMOVABLE_DELAY = 4f;    // removable 4s after bounces=0
    public static final float SPAWN_MARGIN = 30f;

    // Bird-bird collision bounce speed (fixed at 10 * PPM=32)
    public static final float BIRD_COLLISION_BOUNCE_SPEED = 320f;
    // Initial Y velocity for all birds (10 * PPM=32)
    public static final float BIRD_INITIAL_VY = 320f;

    // Items
    public static final float SHIELD_DURATION = 6f;
    public static final float POTION_DURATION = 13f;         // 13 seconds!
    public static final float POST_HIT_INVINCIBILITY = 4f;   // 4 seconds
    public static final float ITEM_DESPAWN_TIME = 6f;         // items disappear after 6s
    public static final int ITEM_SPAWN_DENOMINATOR = 160;

    // Collision hitbox as fraction of sprite frame size (0.6 = 60% centered)
    public static final float HITBOX_SCALE = 0.6f;

    // Animation
    public static final float FRAME_DURATION = 0.2f;

    // Splash
    public static final float SPLASH_DURATION = 2f;
}
