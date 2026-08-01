package info.javaway.sudoku.ui.newgame;

import info.javaway.sudoku.game.Difficulty;

/**
 * Что показывает экран выбора уровня.
 *
 * Всё, кроме отметки о решённой задаче дня, приходит из вызова и за жизнь экрана не меняется;
 * отметка читается с диска, поэтому появляется отдельным действием.
 */
public final class NewGameState {

    /** Номер сегодняшней задачи и её уровень — они выбираются датой, а не человеком. */
    public final int dailyNumber;
    public final Difficulty dailyLevel;
    public final boolean dailySolved;

    /** Начатая партия, которую сотрёт новая. null, если стирать нечего. */
    public final Difficulty currentLevel;
    public final boolean currentDaily;
    public final int currentSeconds;

    private NewGameState(int dailyNumber, Difficulty dailyLevel, boolean dailySolved,
                         Difficulty currentLevel, boolean currentDaily, int currentSeconds) {
        this.dailyNumber = dailyNumber;
        this.dailyLevel = dailyLevel;
        this.dailySolved = dailySolved;
        this.currentLevel = currentLevel;
        this.currentDaily = currentDaily;
        this.currentSeconds = currentSeconds;
    }

    public static NewGameState of(int dailyNumber, Difficulty dailyLevel,
                                  Difficulty currentLevel, boolean currentDaily,
                                  int currentSeconds) {
        return new NewGameState(dailyNumber, dailyLevel, false,
                currentLevel, currentDaily, currentSeconds);
    }

    public boolean hasCurrentGame() {
        return currentLevel != null;
    }

    public NewGameState dailySolved(boolean solved) {
        return new NewGameState(dailyNumber, dailyLevel, solved,
                currentLevel, currentDaily, currentSeconds);
    }
}
