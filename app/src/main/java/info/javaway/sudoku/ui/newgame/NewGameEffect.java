package info.javaway.sudoku.ui.newgame;

import info.javaway.sudoku.game.Difficulty;

public abstract class NewGameEffect {

    /** Уровень выбран: экран закрывается и отдаёт выбор игре. */
    public static final class Start extends NewGameEffect {
        public final Difficulty level;

        public Start(Difficulty level) {
            this.level = level;
        }
    }
}
