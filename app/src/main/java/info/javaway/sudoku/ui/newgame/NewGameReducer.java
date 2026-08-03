package info.javaway.sudoku.ui.newgame;

import info.javaway.sudoku.ui.mvi.Reducer;
import info.javaway.sudoku.ui.mvi.Update;

public final class NewGameReducer implements Reducer<NewGameState, NewGameAction, NewGameEffect> {

    @Override
    public Update<NewGameState, NewGameEffect> reduce(NewGameState state, NewGameAction action) {
        if (action instanceof NewGameAction.LevelPicked) {
            return Update.of(state,
                    new NewGameEffect.Start(((NewGameAction.LevelPicked) action).level));
        }
        return Update.state(state);
    }
}
