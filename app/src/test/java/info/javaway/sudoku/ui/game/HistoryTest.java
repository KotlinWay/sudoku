package info.javaway.sudoku.ui.game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import info.javaway.sudoku.game.Board;
import info.javaway.sudoku.game.Boards;
import info.javaway.sudoku.game.Cells;
import info.javaway.sudoku.game.Move;
import info.javaway.sudoku.game.Rules;

public class HistoryTest {

    private static final int CELL = Cells.at(4, 4);

    private static Move move(int digit) {
        Board before = Boards.withEmpty(CELL);
        return Move.between(before, Rules.place(before, CELL, digit));
    }

    @Test public void пустаяИсторияНичегоНеУмеет() {
        assertFalse(History.empty().canUndo());
        assertFalse(History.empty().isStarted());
    }

    @Test public void первыйЖеХодДелаетПартиюНачатой() {
        History history = History.empty().pushed(move(1));

        assertTrue(history.canUndo());
        assertTrue(history.isStarted());
    }

    @Test public void снятыйХодИсчезаетБезСледа() {
        History history = History.empty().pushed(move(1));

        History after = history.popped();

        assertFalse(after.canUndo());
        assertEquals(0, after.done().size());
    }

    @Test public void снимаетсяТолькоВерхнийХод() {
        History history = History.empty().pushed(move(1)).pushed(move(2));

        History after = history.popped();

        assertEquals(1, after.done().size());
        assertTrue(after.canUndo());
    }

    @Test public void историяНеМеняетсяНаМесте() {
        History history = History.empty();

        history.pushed(move(1));

        assertFalse(history.canUndo());
    }

    @Test public void сохранённуюИсториюМожноСобратьОбратно() {
        History history = History.empty().pushed(move(1)).pushed(move(2));

        History restored = History.of(history.done());

        assertEquals(2, restored.done().size());
        assertTrue(restored.canUndo());
    }
}
