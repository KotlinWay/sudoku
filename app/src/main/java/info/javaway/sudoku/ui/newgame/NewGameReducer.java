package info.javaway.sudoku.ui.newgame;

import info.javaway.sudoku.ui.mvi.Reducer;
import info.javaway.sudoku.ui.mvi.Update;

public final class NewGameReducer implements Reducer<NewGameState, NewGameAction, NewGameEffect> {

    @Override
    public Update<NewGameState, NewGameEffect> reduce(NewGameState state, NewGameAction action) {
        if (action instanceof NewGameAction.Loaded) {
            return Update.state(state.dailySolved(((NewGameAction.Loaded) action).dailySolved));
        }
        if (action instanceof NewGameAction.LevelPicked) {
            NewGameAction.LevelPicked picked = (NewGameAction.LevelPicked) action;
            // Уровень задачи дня выбирает дата, а не нажатие: что бы ни пришло из интерфейса,
            // в игру уходит тот, который сегодня положен.
            return Update.of(state, new NewGameEffect.Start(
                    picked.daily ? state.dailyLevel : picked.level, picked.daily));
        }
        return Update.state(state);
    }
}
