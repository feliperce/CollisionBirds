package br.com.tupinikimtecnologia.entity;

public enum BirdType {
    VERMELHO(5, 0, 13),
    VERDE(60, 14, 60),
    AMARELO(150, 61, 71),
    VIOLETA(15, 72, 99);

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
