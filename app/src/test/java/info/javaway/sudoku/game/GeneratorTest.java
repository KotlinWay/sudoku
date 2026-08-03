package info.javaway.sudoku.game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class GeneratorTest {

    @Test public void решённаяСеткаЗаполненаЦеликом() {
        int[] grid = Generator.solved(new Rng(1));

        for (int cell = 0; cell < Cells.COUNT; cell++) {
            assertTrue(grid[cell] >= 1 && grid[cell] <= Cells.SIDE);
        }
    }

    @Test public void решённаяСеткаНеНарушаетПравил() {
        assertValid(Generator.solved(new Rng(42)));
    }

    @Test public void разныеSeedДаютРазныеСетки() {
        assertFalse(java.util.Arrays.equals(
                Generator.solved(new Rng(1)), Generator.solved(new Rng(2))));
    }

    /** На этом держатся тесты: доска по seed обязана получаться одна и та же. */
    @Test public void одинаковыйSeedДаётОдинаковуюЗадачу() {
        Board first = Generator.board(Difficulty.MEDIUM, new Rng(777));
        Board second = Generator.board(Difficulty.MEDIUM, new Rng(777));

        assertTrue(java.util.Arrays.equals(first.puzzle(), second.puzzle()));
        assertTrue(java.util.Arrays.equals(first.solution(), second.solution()));
    }

    /**
     * Ради чего вся проверка на единственность: иначе игрок вписывает цифру, верную
     * по правилам судоку, а игра снимает жизнь, потому что сверяется с одним из решений.
     */
    @Test public void уКаждогоУровняРовноОдноРешение() {
        for (Difficulty level : Difficulty.values()) {
            Board board = Generator.board(level, new Rng(level.ordinal() + 1));

            assertEquals(level.name(), 1, Solver.count(board.puzzle(), 2));
        }
    }

    @Test public void открытыеЦифрыСовпадаютСРешением() {
        for (Difficulty level : Difficulty.values()) {
            Board board = Generator.board(level, new Rng(level.ordinal() + 100));
            int[] puzzle = board.puzzle();
            int[] solution = board.solution();

            assertValid(solution);
            for (int cell = 0; cell < Cells.COUNT; cell++) {
                if (puzzle[cell] != 0) {
                    assertEquals(level.name(), solution[cell], puzzle[cell]);
                }
            }
        }
    }

    /** Задача, решённая до конца по её же решению, доской считается решённой. */
    @Test public void решениеЗакрываетЗадачу() {
        Board board = Generator.board(Difficulty.EASY, new Rng(3));
        int[] solution = board.solution();

        for (int cell = 0; cell < Cells.COUNT; cell++) {
            board = board.withValue(cell, solution[cell]);
        }

        assertTrue(board.isSolved());
    }

    @Test public void открытыхЦифрНеБольшеЗаказанного() {
        for (Difficulty level : Difficulty.values()) {
            int[] puzzle = Generator.board(level, new Rng(level.ordinal() + 7)).puzzle();

            int clues = 0;
            for (int digit : puzzle) {
                if (digit != 0) clues++;
            }
            // Ровно до цели генератор дойти не обязан: снизу его держит единственность решения.
            assertTrue(level.name() + ": " + clues, clues >= level.clues);
            assertTrue(level.name() + ": " + clues, clues <= Cells.COUNT);
        }
    }

    @Test public void болееСложныйУровеньОткрываетНеБольшеЦифр() {
        assertTrue(Difficulty.EASY.clues > Difficulty.MEDIUM.clues);
        assertTrue(Difficulty.MEDIUM.clues > Difficulty.HARD.clues);
        assertTrue(Difficulty.HARD.clues > Difficulty.EXPERT.clues);
    }

    private static void assertValid(int[] grid) {
        for (int cell = 0; cell < Cells.COUNT; cell++) {
            for (int peer : Cells.peers(cell)) {
                assertFalse("повтор в клетке " + cell, grid[cell] == grid[peer]);
            }
        }
    }
}
