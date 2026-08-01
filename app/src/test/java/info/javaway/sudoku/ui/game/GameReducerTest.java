package info.javaway.sudoku.ui.game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import info.javaway.sudoku.game.Board;
import info.javaway.sudoku.game.Boards;
import info.javaway.sudoku.game.Cells;
import info.javaway.sudoku.game.Difficulty;
import info.javaway.sudoku.game.Notes;
import info.javaway.sudoku.ui.mvi.Update;

/**
 * Игра целиком. Ни одного обращения к телефону: редьюсер не знает ни про часы, ни про диск,
 * ни про случайность, поэтому всё поведение партии проверяется здесь.
 */
public class GameReducerTest {

    private static final GameReducer REDUCER = new GameReducer();

    private static final int CELL = Cells.at(4, 4);
    private static final int OTHER = Cells.at(0, 0);
    private static final int PEER = Cells.at(4, 5);

    /** Партия с двумя пустыми клетками: до победы всегда ровно два верных хода. */
    private static GameState playing() {
        return playing(Difficulty.EASY, CELL, OTHER);
    }

    private static GameState playing(Difficulty level, int... empty) {
        return GameState.generating(level, false, false)
                .started(Boards.withEmpty(empty))
                .selecting(empty[0]);
    }

    private static GameState reduce(GameState state, GameAction action) {
        return REDUCER.reduce(state, action).state;
    }

    private static <T> T effect(Update<GameState, GameEffect> update, Class<T> type) {
        for (GameEffect effect : update.effects) {
            if (type.isInstance(effect)) return type.cast(effect);
        }
        return null;
    }

    /* ── Ход ──────────────────────────────────────────────────────────────── */

    @Test public void вернаяЦифраВстаётИНеСтоитЖизни() {
        GameState state = reduce(playing(), new GameAction.DigitTapped(Boards.answer(CELL)));

        assertEquals(Boards.answer(CELL), state.board.value(CELL));
        assertEquals(0, state.mistakes);
        assertEquals(GameState.Phase.PLAYING, state.phase);
    }

    @Test public void неверноеЦифраОстаётсяНаДоскеИСтоитЖизни() {
        GameState state = reduce(playing(), new GameAction.DigitTapped(Boards.wrongAnswer(CELL)));

        assertEquals(Boards.wrongAnswer(CELL), state.board.value(CELL));
        assertEquals(1, state.mistakes);
    }

    @Test public void вДанностьПисатьНельзя() {
        GameState state = playing().selecting(PEER);

        GameState after = reduce(state, new GameAction.DigitTapped(1));

        assertEquals(state.board.value(PEER), after.board.value(PEER));
        assertFalse(after.history.canUndo());
    }

    @Test public void безВыбраннойКлеткиЦифраНикудаНеИдёт() {
        GameState state = playing().selecting(-1);

        assertFalse(reduce(state, new GameAction.DigitTapped(1)).history.canUndo());
    }

    @Test public void карандашПишетПометкуАНеОтвет() {
        GameState state = reduce(playing(), new GameAction.PencilToggled());

        GameState after = reduce(state, new GameAction.DigitTapped(5));

        assertEquals(0, after.board.value(CELL));
        assertTrue(Notes.has(after.board.notes(CELL), 5));
        assertEquals(0, after.mistakes);
    }

    @Test public void ошибочнаяПометкаЖизниНеСтоит() {
        GameState state = reduce(playing(), new GameAction.PencilToggled());

        assertEquals(0, reduce(state, new GameAction.DigitTapped(
                Boards.wrongAnswer(CELL))).mistakes);
    }

    @Test public void стираниеОсвобождаетКлетку() {
        GameState state = reduce(playing(), new GameAction.DigitTapped(1));

        assertEquals(0, reduce(state, new GameAction.EraseTapped()).board.value(CELL));
    }

    @Test public void стираниеПустойКлеткиНеСчитаетсяХодом() {
        assertFalse(reduce(playing(), new GameAction.EraseTapped()).history.canUndo());
    }

    /* ── Жизни ────────────────────────────────────────────────────────────── */

    @Test public void последняяОшибкаЗаканчиваетПартию() {
        GameState state = playing(Difficulty.EXPERT, CELL, OTHER);

        GameState after = reduce(state, new GameAction.DigitTapped(Boards.wrongAnswer(CELL)));

        assertEquals(GameState.Phase.LOST, after.phase);
        assertTrue(after.panel);
    }

    @Test public void допустимаяОшибкаПартиюНеЗаканчивает() {
        GameState state = playing(Difficulty.EASY, CELL, OTHER);

        GameState after = reduce(state, new GameAction.DigitTapped(Boards.wrongAnswer(CELL)));

        assertEquals(GameState.Phase.PLAYING, after.phase);
        assertEquals(3, after.mistakeLimit());
    }

    @Test public void проигрышОтмечаетсяВСтатистике() {
        GameState state = playing(Difficulty.EXPERT, CELL, OTHER);

        Update<GameState, GameEffect> update =
                REDUCER.reduce(state, new GameAction.DigitTapped(Boards.wrongAnswer(CELL)));

        assertNotNull(effect(update, GameEffect.RecordLoss.class));
    }

    @Test public void послеПроигрышаХодыНеПринимаются() {
        GameState lost = reduce(playing(Difficulty.EXPERT, CELL, OTHER),
                new GameAction.DigitTapped(Boards.wrongAnswer(CELL)));

        GameState after = reduce(lost, new GameAction.DigitTapped(Boards.answer(OTHER)));

        assertEquals(0, after.board.value(OTHER));
    }

    @Test public void панельПоражненияПредлагаетТотЖеУровень() {
        GameState lost = reduce(playing(Difficulty.EXPERT, CELL, OTHER),
                new GameAction.DigitTapped(Boards.wrongAnswer(CELL)));

        assertEquals(Difficulty.EXPERT, lost.level);
        assertTrue(lost.panel);
    }

    /* ── Победа ───────────────────────────────────────────────────────────── */

    @Test public void последняяВернаяЦифраЗаканчиваетПартиюПобедой() {
        GameState state = reduce(playing(Difficulty.EASY, CELL),
                new GameAction.DigitTapped(Boards.answer(CELL)));

        assertEquals(GameState.Phase.WON, state.phase);
        assertTrue(state.panel);
    }

    @Test public void победаПроситЗаписатьИтогИПраздник() {
        Update<GameState, GameEffect> update = REDUCER.reduce(playing(Difficulty.EASY, CELL),
                new GameAction.DigitTapped(Boards.answer(CELL)));

        GameEffect.RecordWin win = effect(update, GameEffect.RecordWin.class);
        assertNotNull(win);
        assertNotNull(effect(update, GameEffect.Celebrate.class));
        assertEquals(Difficulty.EASY, win.level);
        assertEquals(0, win.hints);
    }

    /** Последняя цифра встаёт так же, как все прежние: свой отклик у неё не пропадает. */
    @Test public void победаНеСъедаетОткликСамогоХода() {
        Update<GameState, GameEffect> update = REDUCER.reduce(playing(Difficulty.EASY, CELL),
                new GameAction.DigitTapped(Boards.answer(CELL)));

        GameEffect.Animate animate = effect(update, GameEffect.Animate.class);
        assertNotNull(animate);
        assertEquals(GameEffect.Animation.POP, animate.animation);
    }

    @Test public void записанныйИтогПопадаетНаПанель() {
        GameState won = reduce(playing(Difficulty.EASY, CELL),
                new GameAction.DigitTapped(Boards.answer(CELL)));

        GameState after = reduce(won, new GameAction.WinRecorded(120, true));

        assertEquals(120, after.best);
        assertTrue(after.record);
    }

    @Test public void панельПобедыУбираетсяНоПартияОстаётсяЗаконченной() {
        GameState won = reduce(playing(Difficulty.EASY, CELL),
                new GameAction.DigitTapped(Boards.answer(CELL)));

        GameState after = reduce(won, new GameAction.PanelDismissed());

        assertFalse(after.panel);
        assertEquals(GameState.Phase.WON, after.phase);
    }

    /* ── Отмена и повтор ──────────────────────────────────────────────────── */

    @Test public void отменаУбираетПоследнийХод() {
        GameState state = reduce(playing(), new GameAction.DigitTapped(3));

        GameState after = reduce(state, new GameAction.UndoTapped());

        assertEquals(0, after.board.value(CELL));
        assertFalse(after.history.canUndo());
        assertTrue(after.history.canRedo());
    }

    @Test public void повторВозвращаетОтменённое() {
        GameState state = reduce(reduce(playing(),
                new GameAction.DigitTapped(3)), new GameAction.UndoTapped());

        GameState after = reduce(state, new GameAction.RedoTapped());

        assertEquals(3, after.board.value(CELL));
    }

    @Test public void отменаБезИсторииНичегоНеЛомает() {
        GameState state = playing();

        assertEquals(state, reduce(state, new GameAction.UndoTapped()));
    }

    @Test public void новыйХодСтираетОтменённое() {
        GameState state = reduce(reduce(playing(),
                new GameAction.DigitTapped(3)), new GameAction.UndoTapped());

        GameState after = reduce(state, new GameAction.DigitTapped(4));

        assertFalse(after.history.canRedo());
    }

    @Test public void отменаВозвращаетВыделениеНаКлеткуХода() {
        GameState state = reduce(playing(), new GameAction.DigitTapped(3))
                .selecting(OTHER);

        assertEquals(CELL, reduce(state, new GameAction.UndoTapped()).selected);
    }

    /* ── Подсказка ────────────────────────────────────────────────────────── */

    @Test public void подсказкаОткрываетВернуюЦифруИТратитЛимит() {
        GameState state = reduce(playing(), new GameAction.HintTapped());

        assertEquals(Boards.answer(CELL), state.board.value(CELL));
        assertEquals(GameState.MAX_HINTS - 1, state.hintsLeft);
        assertEquals(1, state.hintsUsed);
    }

    @Test public void подсказкаНеСчитаетсяОшибкой() {
        assertEquals(0, reduce(playing(), new GameAction.HintTapped()).mistakes);
    }

    @Test public void кончившиесяПодсказкиОбъясняютсяИНичегоНеОткрывают() {
        GameState state = playing().hinted().hinted().hinted();

        Update<GameState, GameEffect> update =
                REDUCER.reduce(state, new GameAction.HintTapped());

        GameEffect.Say say = effect(update, GameEffect.Say.class);
        assertNotNull(say);
        assertEquals(GameEffect.Message.NO_HINTS, say.message);
        assertEquals(0, update.state.hintsLeft);
        assertEquals(0, update.state.board.value(CELL));
        assertEquals(state.hintsUsed, update.state.hintsUsed);
    }

    /** Потраченное потрачено: иначе три подсказки превращаются в бесконечные. */
    @Test public void отменаПодсказкиНеВозвращаетЕё() {
        GameState state = reduce(playing(), new GameAction.HintTapped());

        GameState after = reduce(state, new GameAction.UndoTapped());

        assertEquals(0, after.board.value(CELL));
        assertEquals(GameState.MAX_HINTS - 1, after.hintsLeft);
    }

    @Test public void подсказкаМожетЗакончитьПартиюПобедой() {
        GameState state = reduce(playing(Difficulty.EASY, CELL), new GameAction.HintTapped());

        assertEquals(GameState.Phase.WON, state.phase);
    }

    @Test public void наПолнойДоскеПодсказкаОбъясняетЧтоНечегоОткрывать() {
        GameState full = reduce(playing(Difficulty.EASY, CELL, OTHER),
                new GameAction.DigitTapped(1)).selecting(OTHER);
        full = reduce(full, new GameAction.DigitTapped(1));

        Update<GameState, GameEffect> update = REDUCER.reduce(full, new GameAction.HintTapped());

        GameEffect.Say say = effect(update, GameEffect.Say.class);
        assertNotNull(say);
        assertEquals(GameEffect.Message.BOARD_FULL, say.message);
    }

    /* ── Пауза и часы ─────────────────────────────────────────────────────── */

    @Test public void паузаОстанавливаетИгруИПоказываетПанель() {
        GameState state = reduce(playing(), new GameAction.PauseToggled());

        assertEquals(GameState.Phase.PAUSED, state.phase);
        assertTrue(state.panel);
        assertFalse(state.accepts());
    }

    @Test public void сНаузыВозвращаютсяТемЖеНажатием() {
        GameState paused = reduce(playing(), new GameAction.PauseToggled());

        GameState after = reduce(paused, new GameAction.PauseToggled());

        assertEquals(GameState.Phase.PLAYING, after.phase);
        assertFalse(after.panel);
    }

    @Test public void наПаузеЦифрыНеПринимаются() {
        GameState paused = reduce(playing(), new GameAction.PauseToggled());

        assertEquals(0, reduce(paused, new GameAction.DigitTapped(1)).board.value(CELL));
    }

    @Test public void законченнуюПартиюПаузаНеТрогает() {
        GameState won = reduce(playing(Difficulty.EASY, CELL),
                new GameAction.DigitTapped(Boards.answer(CELL)));

        assertEquals(GameState.Phase.WON, reduce(won, new GameAction.PauseToggled()).phase);
    }

    @Test public void часыИдутТолькоВоВремяИгры() {
        GameState playing = reduce(playing(), new GameAction.Ticked());
        GameState paused = reduce(reduce(playing(), new GameAction.PauseToggled()),
                new GameAction.Ticked());

        assertEquals(1, playing.seconds);
        assertEquals(0, paused.seconds);
    }

    @Test public void часыНеИдутПокаСоставляетсяЗадача() {
        GameState generating = GameState.generating(Difficulty.EASY, false, false);

        assertEquals(0, reduce(generating, new GameAction.Ticked()).seconds);
    }

    /* ── Новая партия ─────────────────────────────────────────────────────── */

    @Test public void новаяПартияПроситСоставитьЗадачуВыбранногоУровня() {
        Update<GameState, GameEffect> update =
                REDUCER.reduce(playing(), new GameAction.NewGame(Difficulty.HARD, false));

        GameEffect.Generate generate = effect(update, GameEffect.Generate.class);
        assertNotNull(generate);
        assertEquals(Difficulty.HARD, generate.level);
        assertFalse(generate.daily);
        assertEquals(Difficulty.HARD, update.state.level);
        assertEquals(GameState.Phase.GENERATING, update.state.phase);
    }

    /** Часы, жизни, подсказки и история — всё с нуля: это другая партия, а не продолжение. */
    @Test public void новаяПартияСбрасываетВсёОтПрежней() {
        GameState started = reduce(reduce(playing(), new GameAction.DigitTapped(
                Boards.wrongAnswer(CELL))), new GameAction.Ticked());

        Update<GameState, GameEffect> update = REDUCER.reduce(started,
                new GameAction.NewGame(Difficulty.HARD, false));

        assertEquals(0, update.state.seconds);
        assertEquals(0, update.state.mistakes);
        assertEquals(GameState.MAX_HINTS, update.state.hintsLeft);
        assertEquals(0, update.state.hintsUsed);
        assertFalse(update.state.history.canUndo());
        assertFalse(update.state.panel);
    }

    @Test public void законченнуюПартиюМожноПереигратьПрямоСПанели() {
        GameState won = reduce(playing(Difficulty.EASY, CELL),
                new GameAction.DigitTapped(Boards.answer(CELL)));

        Update<GameState, GameEffect> update =
                REDUCER.reduce(won, new GameAction.NewGame(Difficulty.EASY, false));

        assertNotNull(effect(update, GameEffect.Generate.class));
        assertEquals(GameState.Phase.GENERATING, update.state.phase);
    }

    @Test public void задачаДняОстаётсяЗадачейДня() {
        Update<GameState, GameEffect> update =
                REDUCER.reduce(playing(), new GameAction.NewGame(Difficulty.MEDIUM, true));

        assertTrue(update.state.daily);
        assertTrue(effect(update, GameEffect.Generate.class).daily);
    }

    @Test public void готоваяДоскаЗаменяетЗаглушку() {
        GameState generating = GameState.generating(Difficulty.HARD, false, false);
        Board board = Boards.withEmpty(CELL);

        GameState after = reduce(generating, new GameAction.Generated(board));

        assertEquals(GameState.Phase.PLAYING, after.phase);
        assertEquals(board, after.board);
    }

    /* ── Кандидаты и карандаш ─────────────────────────────────────────────── */

    @Test public void включённыеКандидатыВыключаютКарандаш() {
        GameState state = reduce(playing(), new GameAction.PencilToggled());

        GameState after = reduce(state, new GameAction.CandidatesChanged(true));

        assertTrue(after.candidates);
        assertFalse(after.pencil);
    }

    @Test public void сКандидатамиКарандашНеВключается() {
        GameState state = reduce(playing(), new GameAction.CandidatesChanged(true));

        assertFalse(reduce(state, new GameAction.PencilToggled()).pencil);
    }

    @Test public void настройкаКандидатовПереживаетНовуюПартию() {
        GameState state = reduce(playing(), new GameAction.CandidatesChanged(true));

        GameState after = reduce(state, new GameAction.NewGame(Difficulty.HARD, false));

        assertTrue(after.candidates);
    }

    /* ── Выбор клетки ─────────────────────────────────────────────────────── */

    @Test public void выборКлеткиЗапоминается() {
        assertEquals(OTHER, reduce(playing(), new GameAction.CellTapped(OTHER)).selected);
    }

    @Test public void повторныйВыборТойЖеКлеткиНичегоНеШлёт() {
        Update<GameState, GameEffect> update =
                REDUCER.reduce(playing(), new GameAction.CellTapped(CELL));

        assertTrue(update.effects.isEmpty());
    }

    @Test public void покаЗадачаСоставляетсяКлеткиНеВыбираются() {
        GameState generating = GameState.generating(Difficulty.EASY, false, false);

        assertEquals(-1, reduce(generating, new GameAction.CellTapped(CELL)).selected);
    }
}
