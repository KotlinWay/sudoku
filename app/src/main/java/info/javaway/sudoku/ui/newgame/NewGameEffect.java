package info.javaway.sudoku.ui.newgame;

import info.javaway.sudoku.game.Difficulty;

public abstract class NewGameEffect {

    public static final class Load extends NewGameEffect {
    }

    /** Уровень выбран: экран закрывается и отдаёт выбор игре. */
    public static final class Start extends NewGameEffect {
        public final Difficulty level;
        public final boolean daily;

        public Start(Difficulty level, boolean daily) {
            this.level = level;
            this.daily = daily;
        }
    }
}
