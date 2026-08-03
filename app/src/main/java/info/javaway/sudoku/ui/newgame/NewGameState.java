package info.javaway.sudoku.ui.newgame;

import info.javaway.sudoku.game.Difficulty;

/**
 * Что показывает экран выбора уровня. Всё приходит из вызова и за жизнь экрана не меняется:
 * сами уровни неизменны, а начатую партию передаёт игра.
 */
public final class NewGameState {

    /** Начатая партия, которую сотрёт новая. null, если стирать нечего. */
    public final Difficulty currentLevel;
    public final int currentSeconds;

    private NewGameState(Difficulty currentLevel, int currentSeconds) {
        this.currentLevel = currentLevel;
        this.currentSeconds = currentSeconds;
    }

    public static NewGameState of(Difficulty currentLevel, int currentSeconds) {
        return new NewGameState(currentLevel, currentSeconds);
    }

    public boolean hasCurrentGame() {
        return currentLevel != null;
    }
}
