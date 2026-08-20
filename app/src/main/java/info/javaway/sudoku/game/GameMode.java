package info.javaway.sudoku.game;

public enum GameMode {
    STANDARD,
    RELAXED;

    public static GameMode byName(String name, GameMode fallback) {
        if (name != null) {
            for (GameMode mode : values()) {
                if (mode.name().equals(name)) return mode;
            }
        }
        return fallback;
    }
}
