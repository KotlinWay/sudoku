package info.javaway.sudoku.ui.newgame;

import info.javaway.sudoku.game.Difficulty;
import info.javaway.sudoku.game.GameMode;

public abstract class NewGameEffect {

    public static final class RememberMode extends NewGameEffect {
        public final GameMode mode;

        public RememberMode(GameMode mode) {
            this.mode = mode;
        }
    }

    /** Уровень выбран: экран закрывается и отдаёт выбор игре. */
    public static final class Start extends NewGameEffect {
        public final Difficulty level;
        public final GameMode mode;

        public Start(Difficulty level, GameMode mode) {
            this.level = level;
            this.mode = mode;
        }
    }
}
