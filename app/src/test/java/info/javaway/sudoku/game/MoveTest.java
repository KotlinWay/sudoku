package info.javaway.sudoku.game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.junit.Test;

public class MoveTest {

    private static final int CELL = Cells.at(4, 4);
    private static final int PEER = Cells.at(4, 5);

    @Test public void ходБезИзмененийНеРождается() {
        Board board = Boards.withEmpty(CELL);

        assertNull(Move.between(board, board));
    }

    @Test public void отменаВозвращаетДоскуВПрежнийВид() {
        Board before = Boards.withEmpty(CELL);
        Board after = Rules.place(before, CELL, 7);

        Board undone = Move.between(before, after).undo(after);

        assertEquals(0, undone.value(CELL));
    }

    @Test public void повторВозвращаетХодОбратно() {
        Board before = Boards.withEmpty(CELL);
        Board after = Rules.place(before, CELL, 7);
        Move move = Move.between(before, after);

        Board redone = move.redo(move.undo(after));

        assertEquals(7, redone.value(CELL));
    }

    /**
     * Главное, ради чего ход считается разницей досок: вписанная цифра трогает пометки
     * у двадцати соседей, и отмена обязана вернуть все двадцать.
     */
    @Test public void отменаВозвращаетПометкиСтёртыеУСоседей() {
        Board before = Boards.withEmpty(CELL, PEER)
                .withNotes(PEER, Notes.with(Notes.NONE, 7));
        Board after = Rules.place(before, CELL, 7);

        Board undone = Move.between(before, after).undo(after);

        assertTrue(Notes.has(undone.notes(PEER), 7));
    }

    @Test public void ходПомнитКлеткуВокругКоторойСлучился() {
        Board before = Boards.withEmpty(CELL);
        Board after = Rules.place(before, CELL, 7);

        assertEquals(CELL, Move.between(before, after).cell());
    }

    @Test public void ходПереживаетЗаписьИЧтение() throws IOException {
        Board before = Boards.withEmpty(CELL, PEER)
                .withNotes(PEER, Notes.with(Notes.NONE, 7));
        Board after = Rules.place(before, CELL, 7);
        Move move = Move.between(before, after);

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        move.write(new DataOutputStream(bytes));
        Move read = Move.read(new DataInputStream(new ByteArrayInputStream(bytes.toByteArray())));

        assertNotNull(read);
        Board undone = read.undo(after);
        assertEquals(0, undone.value(CELL));
        assertTrue(Notes.has(undone.notes(PEER), 7));
    }

    @Test(expected = IOException.class)
    public void испорченныйХодНеЧитается() throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        new DataOutputStream(bytes).writeInt(Cells.COUNT + 1);

        Move.read(new DataInputStream(new ByteArrayInputStream(bytes.toByteArray())));
    }
}
