package br.com.tupinikimtecnologia.entity;

public enum BirdType {
    // Published APK values: bounces, speed (Box2D m/s), spawn range
    VERMELHO(6, 30f, 0, 13),     // Red: fast, few bounces, drops 1-UP
    VERDE(35, 10f, 14, 60),      // Green: slow, many bounces, most common
    AMARELO(130, 10f, 61, 71),   // Yellow: slow, very many bounces, rare
    VIOLETA(15, 15f, 72, 99);    // Purple: medium speed/bounces

    public final int bounces;
    public final float speed;     // Box2D m/s (multiply by PPM=32 for px/s)
    public final int minProb;
    public final int maxProb;

    private static final float PPM = 32f;

    BirdType(int bounces, float speed, int minProb, int maxProb) {
        this.bounces = bounces;
        this.speed = speed;
        this.minProb = minProb;
        this.maxProb = maxProb;
    }

    public float getPixelSpeed() {
        return speed * PPM;
    }

    public static BirdType fromRandom(int value) {
        for (BirdType type : values()) {
            if (value >= type.minProb && value <= type.maxProb) {
                return type;
            }
        }
        return VERDE;
    }
}
