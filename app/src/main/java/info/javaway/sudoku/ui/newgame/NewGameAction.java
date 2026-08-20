package info.javaway.sudoku.ui.newgame;

import info.javaway.sudoku.game.Difficulty;
import info.javaway.sudoku.game.GameMode;

public abstract class NewGameAction {

    public static final class ModeToggled extends NewGameAction {
        public final GameMode mode;

        public ModeToggled(GameMode mode) {
            this.mode = mode;
        }
    }

    public static final class LevelPicked extends NewGameAction {
        public final Difficulty level;

        public LevelPicked(Difficulty level) {
            this.level = level;
        }
    }
}
