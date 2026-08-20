package info.javaway.sudoku.ui.game;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import info.javaway.sudoku.game.Board;
import info.javaway.sudoku.game.Boards;
import info.javaway.sudoku.game.Cells;
import info.javaway.sudoku.game.Difficulty;
import info.javaway.sudoku.game.GameMode;
import info.javaway.sudoku.game.Move;
import info.javaway.sudoku.game.Rules;

public class GameSaveTest {

    private static final int CELL = Cells.at(4, 4);

    @Rule public final TemporaryFolder folder = new TemporaryFolder();

    @Test public void спокойныйРежимПереживаетСохранение() {
        assertRoundTrip(GameMode.RELAXED);
    }

    @Test public void обычныйРежимПереживаетСохранение() {
        assertRoundTrip(GameMode.STANDARD);
    }

    @Test public void сохранениеВерсииТриОткрываетсяВОбычномРежиме() throws IOException {
        writeFixture(3, null);

        GameState after = new GameSave(folder.getRoot()).load(false);

        assertNotNull(after);
        assertEquals(GameMode.STANDARD, after.mode);
        assertEquals(Difficulty.HARD, after.level);
        assertEquals(2, after.mistakes);
        assertEquals(1, after.hintsLeft);
        assertEquals(2, after.hintsUsed);
        assertEquals(37, after.seconds);
        assertEquals(0, after.history.done().size());
    }

    @Test public void неизвестныйРежимВерсииЧетыреЗаменяетсяОбычным() throws IOException {
        writeFixture(4, "UNKNOWN");

        GameState after = new GameSave(folder.getRoot()).load(false);

        assertNotNull(after);
        assertEquals(GameMode.STANDARD, after.mode);
    }

    private void assertRoundTrip(GameMode mode) {
        Board beforeMove = Boards.withEmpty(CELL);
        Board afterMove = Rules.place(beforeMove, CELL, Boards.answer(CELL));
        Move move = Move.between(beforeMove, afterMove);
        GameState before = GameState.generating(Difficulty.HARD, mode, false)
                .started(beforeMove)
                .selecting(CELL)
                .moved(afterMove, move, CELL)
                .mistaken()
                .hinted()
                .ticked();
        GameSave save = new GameSave(folder.getRoot());

        save.save(before);
        GameState after = save.load(false);

        assertNotNull(after);
        assertEquals(Difficulty.HARD, after.level);
        assertEquals(mode, after.mode);
        assertArrayEquals(before.board.puzzle(), after.board.puzzle());
        assertArrayEquals(before.board.solution(), after.board.solution());
        assertArrayEquals(before.board.values(), after.board.values());
        assertArrayEquals(before.board.notesArray(), after.board.notesArray());
        assertEquals(CELL, after.selected);
        assertEquals(1, after.mistakes);
        assertEquals(2, after.hintsLeft);
        assertEquals(1, after.hintsUsed);
        assertEquals(1, after.seconds);
        assertEquals(1, after.history.done().size());
        assertArrayEquals(beforeMove.values(), after.history.lastDone().undo(after.board).values());
        assertArrayEquals(beforeMove.notesArray(), after.history.lastDone().undo(after.board).notesArray());
    }

    private void writeFixture(int version, String mode) throws IOException {
        Board board = Boards.withEmpty(CELL);
        File file = new File(folder.getRoot(), "game.bin");
        try (DataOutputStream out = new DataOutputStream(new FileOutputStream(file))) {
            out.writeInt(version);
            out.writeUTF(Difficulty.HARD.name());
            if (version == 4) out.writeUTF(mode);
            writeCells(out, board.puzzle());
            writeCells(out, board.solution());
            writeCells(out, board.values());
            writeCells(out, board.notesArray());
            out.writeInt(CELL);
            out.writeBoolean(true);
            out.writeInt(2);
            out.writeInt(1);
            out.writeInt(2);
            out.writeInt(37);
            out.writeBoolean(false);
            out.writeInt(0);
        }
    }

    private static void writeCells(DataOutputStream out, int[] cells) throws IOException {
        for (int value : cells) out.writeInt(value);
    }
}
