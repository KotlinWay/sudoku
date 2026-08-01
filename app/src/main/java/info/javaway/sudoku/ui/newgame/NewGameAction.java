package info.javaway.sudoku.ui.newgame;

import info.javaway.sudoku.game.Difficulty;

public abstract class NewGameAction {

    /** Отметка «задача дня решена» прочитана с диска. */
    public static final class Loaded extends NewGameAction {
        public final boolean dailySolved;

        public Loaded(boolean dailySolved) {
            this.dailySolved = dailySolved;
        }
    }

    public static final class LevelPicked extends NewGameAction {
        public final Difficulty level;
        public final boolean daily;

        public LevelPicked(Difficulty level, boolean daily) {
            this.level = level;
            this.daily = daily;
        }
    }
}
