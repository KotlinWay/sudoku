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
        assertFalse(History.empty().canRedo());
        assertFalse(History.empty().isStarted());
    }

    @Test public void первыйЖеХодДелаетПартиюНачатой() {
        History history = History.empty().pushed(move(1));

        assertTrue(history.canUndo());
        assertTrue(history.isStarted());
    }

    @Test public void отменённыйХодУходитВПовтор() {
        History history = History.empty().pushed(move(1));

        History after = history.undone(history.lastDone());

        assertFalse(after.canUndo());
        assertTrue(after.canRedo());
    }

    @Test public void повторённыйХодВозвращаетсяВСделанные() {
        History history = History.empty().pushed(move(1));
        History undone = history.undone(history.lastDone());

        History after = undone.redone(undone.lastUndone());

        assertTrue(after.canUndo());
        assertFalse(after.canRedo());
    }

    /** Иначе «повторить» вернуло бы ход из ветки, от которой игрок уже отказался. */
    @Test public void новыйХодСтираетОтменённое() {
        History history = History.empty().pushed(move(1));
        History undone = history.undone(history.lastDone());

        History after = undone.pushed(move(2));

        assertFalse(after.canRedo());
    }

    @Test public void историяНеМеняетсяНаМесте() {
        History history = History.empty();

        history.pushed(move(1));

        assertFalse(history.canUndo());
    }

    @Test public void сохранённуюИсториюМожноСобратьОбратно() {
        History history = History.empty().pushed(move(1)).pushed(move(2));
        History undone = history.undone(history.lastDone());

        History restored = History.of(undone.done(), undone.undone());

        assertEquals(1, restored.done().size());
        assertEquals(1, restored.undone().size());
        assertTrue(restored.canUndo());
        assertTrue(restored.canRedo());
    }
}
