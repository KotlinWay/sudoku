package info.javaway.sudoku.game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class BoardTest {

    private static final int CELL = Cells.at(0, 0);
    private static final int NEIGHBOUR = Cells.at(0, 8);

    @Test public void открытыеЦифрыСчитаютсяДанностями() {
        Board board = Boards.withEmpty(CELL);

        assertFalse(board.isGiven(CELL));
        assertTrue(board.isGiven(NEIGHBOUR));
    }

    @Test public void вписаннаяЦифраВидна() {
        Board board = Boards.withEmpty(CELL).withValue(CELL, 7);

        assertEquals(7, board.value(CELL));
        assertFalse(board.isEmpty(CELL));
    }

    @Test public void доскаНеМеняетсяНаМесте() {
        Board before = Boards.withEmpty(CELL);
        Board after = before.withValue(CELL, 7);

        assertEquals(0, before.value(CELL));
        assertNotEquals(before.value(CELL), after.value(CELL));
    }

    @Test public void повторВСтрокеЗамечается() {
        int digit = Boards.answer(NEIGHBOUR);
        Board board = Boards.withEmpty(CELL).withValue(CELL, digit);

        assertTrue(board.isDuplicate(CELL));
        assertTrue(board.isDuplicate(NEIGHBOUR));
    }

    @Test public void вернаяЦифраПовторомНеСчитается() {
        Board board = Boards.withEmpty(CELL).withValue(CELL, Boards.answer(CELL));

        assertFalse(board.isDuplicate(CELL));
    }

    @Test public void пустаяКлеткаПовторомНеСчитается() {
        assertFalse(Boards.withEmpty(CELL).isDuplicate(CELL));
    }

    @Test public void неверностьСверяетсяСРешением() {
        Board board = Boards.withEmpty(CELL).withValue(CELL, Boards.wrongAnswer(CELL));

        assertTrue(board.isWrong(CELL));
        assertFalse(board.isWrong(NEIGHBOUR));
    }

    @Test public void вЕдинственнойПустойКлеткеОстаётсяОдинКандидат() {
        Board board = Boards.withEmpty(CELL);

        int candidates = board.candidates(CELL);

        assertTrue(Notes.has(candidates, Boards.answer(CELL)));
        assertEquals(Notes.with(Notes.NONE, Boards.answer(CELL)), candidates);
    }

    @Test public void уЗанятойКлеткиКандидатовНет() {
        assertEquals(Notes.NONE, Boards.withEmpty(CELL).candidates(NEIGHBOUR));
    }

    @Test public void расставленнаяЦифраСчитаетсяЗавершённой() {
        Board board = Boards.withEmpty(CELL);
        int missing = Boards.answer(CELL);

        assertFalse(board.isComplete(missing));
        assertTrue(board.withValue(CELL, missing).isComplete(missing));
    }

    @Test public void заполненнаяВернымиЦифрамиДоскаРешена() {
        Board board = Boards.withEmpty(CELL);

        assertFalse(board.isSolved());
        assertTrue(board.withValue(CELL, Boards.answer(CELL)).isSolved());
    }

    @Test public void доскаСНеверноЗаполненнойКлеткойНеРешена() {
        Board board = Boards.withEmpty(CELL).withValue(CELL, Boards.wrongAnswer(CELL));

        assertFalse(board.isSolved());
    }

    @Test public void пустаяДоскаНеСчитаетсяРешённой() {
        assertFalse(Board.blank().isSolved());
    }

    @Test public void задачаВосстанавливаетсяИзСохранения() {
        Board board = Boards.withEmpty(CELL).withValue(CELL, 5)
                .withNotes(CELL, Notes.with(Notes.NONE, 3));

        Board restored = Board.restored(board.puzzle(), board.solution(),
                board.values(), board.notesArray());

        assertEquals(5, restored.value(CELL));
        assertFalse(restored.isGiven(CELL));
        assertTrue(restored.isGiven(NEIGHBOUR));
        assertTrue(Notes.has(restored.notes(CELL), 3));
    }
}
