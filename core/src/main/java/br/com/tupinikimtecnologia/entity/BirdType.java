package br.com.tupinikimtecnologia.entity;

public enum BirdType {
    // Health values from published APK version
    VERMELHO(6, 0, 13),     // Red: low health, drops 1-UP on death
    VERDE(35, 14, 60),      // Green: medium health, most common
    AMARELO(130, 61, 71),   // Yellow: very high health, rare
    VIOLETA(15, 72, 99);    // Purple: low-medium health

    public final int health;
    public final int minProb;
    public final int maxProb;

    BirdType(int health, int minProb, int maxProb) {
        this.health = health;
        this.minProb = minProb;
        this.maxProb = maxProb;
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
