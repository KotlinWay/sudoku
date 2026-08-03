package info.javaway.sudoku.ui.newgame;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import info.javaway.sudoku.game.Difficulty;
import info.javaway.sudoku.ui.mvi.Update;

public class NewGameReducerTest {

    private static final NewGameReducer REDUCER = new NewGameReducer();

    private static NewGameState screen() {
        return NewGameState.of(null, 0);
    }

    private static NewGameState screenOver(Difficulty level, int seconds) {
        return NewGameState.of(level, seconds);
    }

    private static NewGameEffect.Start start(Update<NewGameState, NewGameEffect> update) {
        for (NewGameEffect effect : update.effects) {
            if (effect instanceof NewGameEffect.Start) return (NewGameEffect.Start) effect;
        }
        return null;
    }

    @Test public void безНачатойПартииПредупреждатьНеОЧем() {
        assertFalse(screen().hasCurrentGame());
    }

    @Test public void начатаяПартияВиднаЭкрануЦеликом() {
        NewGameState state = screenOver(Difficulty.EASY, 192);

        assertTrue(state.hasCurrentGame());
        assertEquals(Difficulty.EASY, state.currentLevel);
        assertEquals(192, state.currentSeconds);
    }

    @Test public void выборУровняЗапускаетЕгоЖе() {
        Update<NewGameState, NewGameEffect> update =
                REDUCER.reduce(screen(), new NewGameAction.LevelPicked(Difficulty.EXPERT));

        assertEquals(Difficulty.EXPERT, start(update).level);
    }

    /** Выбор ничего не меняет на экране: он закрывает его, а не перерисовывает. */
    @Test public void выборНеТрогаетСостояниеЭкрана() {
        NewGameState before = screenOver(Difficulty.EASY, 192);

        NewGameState after =
                REDUCER.reduce(before, new NewGameAction.LevelPicked(Difficulty.EXPERT)).state;

        assertEquals(Difficulty.EASY, after.currentLevel);
        assertEquals(192, after.currentSeconds);
    }
}
