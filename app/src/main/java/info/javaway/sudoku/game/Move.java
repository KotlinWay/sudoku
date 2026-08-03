package info.javaway.sudoku.game;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Один шаг истории — снимок клеток, которых ход коснулся, каким они были до него.
 *
 * Ход не описывает сам себя («вписал 5», «стёр», «взял подсказку»): он вычисляется сравнением
 * двух досок. Поэтому отмена работает одинаково для любого действия, включая то, которое
 * задело чужие клетки — например вписанная цифра стирает свои пометки у всех соседей.
 * Заводить на каждый вид хода свою запись и свой обратный код не понадобилось.
 *
 * Состояние «после» ход не хранит: его применял только «вернуть», а его в игре больше нет.
 * Понадобится вернуть — сюда придётся дописать вторую половину снимка и вторую половину
 * формата сохранения; одной отмене она не нужна.
 */
public final class Move {

    /** Клетки, которые ход изменил, и что в них стояло до него. */
    private final int[] cells;
    private final int[] values;
    private final int[] notes;

    private Move(int[] cells, int[] values, int[] notes) {
        this.cells = cells;
        this.values = values;
        this.notes = notes;
    }

    /** @return разница двух досок или null, если действие ничего не изменило */
    public static Move between(Board before, Board after) {
        int changed = 0;
        for (int cell = 0; cell < Cells.COUNT; cell++) {
            if (differs(before, after, cell)) changed++;
        }
        if (changed == 0) return null;

        int[] cells = new int[changed];
        int[] values = new int[changed];
        int[] notes = new int[changed];
        int i = 0;
        for (int cell = 0; cell < Cells.COUNT; cell++) {
            if (!differs(before, after, cell)) continue;
            cells[i] = cell;
            values[i] = before.value(cell);
            notes[i] = before.notes(cell);
            i++;
        }
        return new Move(cells, values, notes);
    }

    /** Клетка, вокруг которой ход случился: к ней возвращается выделение при отмене. */
    public int cell() {
        return cells[0];
    }

    public Board undo(Board board) {
        return board.with(cells, values, notes);
    }

    private static boolean differs(Board before, Board after, int cell) {
        return before.value(cell) != after.value(cell)
                || before.notes(cell) != after.notes(cell);
    }

    /*
     * Запись и чтение — здесь, а не в хранилище: иначе три внутренних массива пришлось бы
     * выставить наружу геттерами, и любой желающий смог бы собрать ход, который не является
     * разницей никаких двух досок.
     */

    public void write(DataOutputStream out) throws IOException {
        out.writeInt(cells.length);
        for (int i = 0; i < cells.length; i++) {
            out.writeInt(cells[i]);
            out.writeInt(values[i]);
            out.writeInt(notes[i]);
        }
    }

    public static Move read(DataInputStream in) throws IOException {
        int size = in.readInt();
        // Сохранение испорчено: дальше читать нечего, партия начнётся заново.
        if (size <= 0 || size > Cells.COUNT) throw new IOException();
        int[] cells = new int[size];
        int[] values = new int[size];
        int[] notes = new int[size];
        for (int i = 0; i < size; i++) {
            cells[i] = in.readInt();
            values[i] = in.readInt();
            notes[i] = in.readInt();
            if (cells[i] < 0 || cells[i] >= Cells.COUNT) throw new IOException();
        }
        return new Move(cells, values, notes);
    }
}
