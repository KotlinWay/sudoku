package info.javaway.sudoku.ui.newgame;

import info.javaway.sudoku.game.Difficulty;

public abstract class NewGameAction {

    public static final class LevelPicked extends NewGameAction {
        public final Difficulty level;

        public LevelPicked(Difficulty level) {
            this.level = level;
        }
    }
}
