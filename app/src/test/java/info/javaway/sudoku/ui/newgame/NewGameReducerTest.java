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
        return NewGameState.of(666, Difficulty.HARD, null, false, 0);
    }

    private static NewGameState screenOver(Difficulty level, int seconds) {
        return NewGameState.of(666, Difficulty.HARD, level, false, seconds);
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
                REDUCER.reduce(screen(), new NewGameAction.LevelPicked(Difficulty.EXPERT, false));

        NewGameEffect.Start start = start(update);
        assertEquals(Difficulty.EXPERT, start.level);
        assertFalse(start.daily);
    }

    /** Уровень задачи дня назначает дата: что бы ни пришло из интерфейса, идёт сегодняшний. */
    @Test public void задачаДняИдётСоСвоимУровнем() {
        Update<NewGameState, NewGameEffect> update =
                REDUCER.reduce(screen(), new NewGameAction.LevelPicked(null, true));

        NewGameEffect.Start start = start(update);
        assertEquals(Difficulty.HARD, start.level);
        assertTrue(start.daily);
    }

    @Test public void отметкаОРешённойЗадачеДняПриходитОтдельно() {
        NewGameState state = screen();

        assertFalse(state.dailySolved);
        assertTrue(REDUCER.reduce(state, new NewGameAction.Loaded(true)).state.dailySolved);
    }

    @Test public void отметкаНеТрогаетОстальноеСостояние() {
        NewGameState after = REDUCER.reduce(screenOver(Difficulty.EASY, 192),
                new NewGameAction.Loaded(true)).state;

        assertEquals(666, after.dailyNumber);
        assertEquals(Difficulty.HARD, after.dailyLevel);
        assertEquals(Difficulty.EASY, after.currentLevel);
        assertEquals(192, after.currentSeconds);
    }
}
