package info.javaway.sudoku.ui.newgame;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import info.javaway.sudoku.game.Difficulty;
import info.javaway.sudoku.game.GameMode;
import info.javaway.sudoku.ui.mvi.Update;

public class NewGameReducerTest {

    private static final NewGameReducer REDUCER = new NewGameReducer();

    private static NewGameState screen() {
        return NewGameState.of(null, null, 0, GameMode.STANDARD);
    }

    private static NewGameState screenOver(Difficulty level, int seconds) {
        return NewGameState.of(level, GameMode.STANDARD, seconds, GameMode.STANDARD);
    }

    private static NewGameEffect.Start start(Update<NewGameState, NewGameEffect> update) {
        return effect(update, NewGameEffect.Start.class);
    }

    private static <T> T effect(Update<NewGameState, NewGameEffect> update, Class<T> type) {
        for (NewGameEffect effect : update.effects) {
            if (type.isInstance(effect)) return type.cast(effect);
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

    @Test public void переключательМеняетРежимИПроситЕгоЗапомнить() {
        Update<NewGameState, NewGameEffect> update = REDUCER.reduce(screen(),
                new NewGameAction.ModeToggled(GameMode.RELAXED));

        assertEquals(GameMode.RELAXED, update.state.mode);
        assertEquals(GameMode.RELAXED,
                effect(update, NewGameEffect.RememberMode.class).mode);
    }

    @Test public void уровеньЗапускаетсяСВыбраннымРежимом() {
        NewGameState relaxed = REDUCER.reduce(screen(),
                new NewGameAction.ModeToggled(GameMode.RELAXED)).state;

        NewGameEffect.Start start = start(REDUCER.reduce(relaxed,
                new NewGameAction.LevelPicked(Difficulty.EXPERT)));
        assertEquals(Difficulty.EXPERT, start.level);
        assertEquals(GameMode.RELAXED, start.mode);
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
