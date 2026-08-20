package info.javaway.sudoku.ui.newgame;

import info.javaway.sudoku.game.Difficulty;
import info.javaway.sudoku.game.GameMode;

/**
 * Что показывает экран выбора уровня. Начатую партию передаёт игра, а выбранный режим меняется
 * только редьюсером.
 */
public final class NewGameState {

    /** Начатая партия, которую сотрёт новая. null, если стирать нечего. */
    public final Difficulty currentLevel;
    public final GameMode currentMode;
    public final int currentSeconds;
    public final GameMode mode;

    private NewGameState(Difficulty currentLevel, GameMode currentMode, int currentSeconds,
            GameMode mode) {
        this.currentLevel = currentLevel;
        this.currentMode = currentMode;
        this.currentSeconds = currentSeconds;
        this.mode = mode;
    }

    public static NewGameState of(Difficulty currentLevel, GameMode currentMode,
            int currentSeconds, GameMode mode) {
        return new NewGameState(currentLevel, currentMode, currentSeconds, mode);
    }

    public NewGameState withMode(GameMode mode) {
        return new NewGameState(currentLevel, currentMode, currentSeconds, mode);
    }

    public boolean hasCurrentGame() {
        return currentLevel != null;
    }
}
