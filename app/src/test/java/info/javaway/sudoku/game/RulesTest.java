package info.javaway.sudoku.game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class RulesTest {

    private static final int CELL = Cells.at(4, 4);
    private static final int PEER = Cells.at(4, 5);
    private static final int STRANGER = Cells.at(0, 0);

    @Test public void цифраВстаётВПустуюКлетку() {
        Board board = Rules.place(Boards.withEmpty(CELL), CELL, 7);

        assertEquals(7, board.value(CELL));
    }

    @Test public void вДанностьПисатьНельзя() {
        Board before = Boards.withEmpty(CELL);

        Board after = Rules.place(before, PEER, 7);

        assertEquals(before.value(PEER), after.value(PEER));
    }

    @Test public void вписаннаяЦифраСтираетСвоиПометкиВЭтойЖеКлетке() {
        Board board = Boards.withEmpty(CELL).withNotes(CELL, Notes.with(Notes.NONE, 3));

        Board after = Rules.place(board, CELL, 7);

        assertTrue(Notes.isEmpty(after.notes(CELL)));
    }

    @Test public void вписаннаяЦифраСтираетТакуюЖеПометкуУСоседей() {
        Board board = Boards.withEmpty(CELL, PEER)
                .withNotes(PEER, Notes.with(Notes.with(Notes.NONE, 7), 8));

        Board after = Rules.place(board, CELL, 7);

        assertFalse(Notes.has(after.notes(PEER), 7));
        assertTrue(Notes.has(after.notes(PEER), 8));
    }

    @Test public void чужиеКлеткиОтВписаннойЦифрыНеСтрадают() {
        Board board = Boards.withEmpty(CELL, STRANGER)
                .withNotes(STRANGER, Notes.with(Notes.NONE, 7));

        Board after = Rules.place(board, CELL, 7);

        assertTrue(Notes.has(after.notes(STRANGER), 7));
    }

    @Test public void стираниеУбираетИЦифруИПометки() {
        Board board = Boards.withEmpty(CELL)
                .withValue(CELL, 7)
                .withNotes(CELL, Notes.with(Notes.NONE, 3));

        Board after = Rules.erase(board, CELL);

        assertEquals(0, after.value(CELL));
        assertTrue(Notes.isEmpty(after.notes(CELL)));
    }

    @Test public void стираниеДанностиНичегоНеДелает() {
        Board before = Boards.withEmpty(CELL);

        assertEquals(before.value(PEER), Rules.erase(before, PEER).value(PEER));
    }

    @Test public void карандашПереключаетПометку() {
        Board once = Rules.note(Boards.withEmpty(CELL), CELL, 4);
        Board twice = Rules.note(once, CELL, 4);

        assertTrue(Notes.has(once.notes(CELL), 4));
        assertTrue(Notes.isEmpty(twice.notes(CELL)));
    }

    @Test public void карандашНеПишетПоЗанятойКлетке() {
        Board board = Rules.place(Boards.withEmpty(CELL), CELL, 7);

        assertTrue(Notes.isEmpty(Rules.note(board, CELL, 4).notes(CELL)));
    }

    @Test public void подсказкаОткрываетВернуюЦифру() {
        Board after = Rules.reveal(Boards.withEmpty(CELL), CELL);

        assertEquals(Boards.answer(CELL), after.value(CELL));
    }

    @Test public void подсказкаИдётВВыбраннуюПустуюКлетку() {
        Board board = Boards.withEmpty(CELL, PEER);

        assertEquals(PEER, Rules.hintCell(board, PEER, 0));
    }

    @Test public void безВыбораПодсказкаИщетЛюбуюПустуюКлетку() {
        Board board = Boards.withEmpty(CELL, PEER);

        int cell = Rules.hintCell(board, -1, 0);

        assertTrue(cell == CELL || cell == PEER);
    }

    @Test public void заполненнаяВыбраннаяКлеткаПодсказкуНеЗабирает() {
        Board board = Rules.place(Boards.withEmpty(CELL, PEER), CELL, 7);

        assertEquals(PEER, Rules.hintCell(board, CELL, 0));
    }

    @Test public void наПолнойДоскеПодсказыватьНечего() {
        Board board = Rules.reveal(Boards.withEmpty(CELL), CELL);

        assertEquals(-1, Rules.hintCell(board, -1, 0));
    }

    @Test public void выборКлеткиДляПодсказкиНеЗависитОтЗнакаСчётчика() {
        Board board = Boards.withEmpty(CELL, PEER, STRANGER);

        assertTrue(Rules.hintCell(board, -1, -7) >= 0);
    }

}
