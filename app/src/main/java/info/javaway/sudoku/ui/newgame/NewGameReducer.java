package info.javaway.sudoku.ui.newgame;

import info.javaway.sudoku.ui.mvi.Reducer;
import info.javaway.sudoku.ui.mvi.Update;

public final class NewGameReducer implements Reducer<NewGameState, NewGameAction, NewGameEffect> {

    @Override
    public Update<NewGameState, NewGameEffect> reduce(NewGameState state, NewGameAction action) {
        if (action instanceof NewGameAction.ModeToggled) {
            NewGameAction.ModeToggled toggled = (NewGameAction.ModeToggled) action;
            return Update.of(state.withMode(toggled.mode), new NewGameEffect.RememberMode(toggled.mode));
        }
        if (action instanceof NewGameAction.LevelPicked) {
            return Update.of(state,
                    new NewGameEffect.Start(((NewGameAction.LevelPicked) action).level, state.mode));
        }
        return Update.state(state);
    }
}
