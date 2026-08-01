package info.javaway.sudoku.game;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Один шаг истории — разница между доской до и доской после.
 *
 * Ход не описывает сам себя («вписал 5», «стёр», «взял подсказку»): он вычисляется сравнением
 * двух досок. Поэтому отмена работает одинаково для любого действия, включая то, которое
 * задело чужие клетки — например вписанная цифра стирает свои пометки у всех соседей.
 * Заводить на каждый вид хода свою запись и свой обратный код не понадобилось.
 */
public final class Move {

    private final int[] cells;
    private final int[] valuesBefore;
    private final int[] valuesAfter;
    private final int[] notesBefore;
    private final int[] notesAfter;

    private Move(int[] cells, int[] valuesBefore, int[] valuesAfter,
                 int[] notesBefore, int[] notesAfter) {
        this.cells = cells;
        this.valuesBefore = valuesBefore;
        this.valuesAfter = valuesAfter;
        this.notesBefore = notesBefore;
        this.notesAfter = notesAfter;
    }

    /** @return разница двух досок или null, если действие ничего не изменило */
    public static Move between(Board before, Board after) {
        int changed = 0;
        for (int cell = 0; cell < Cells.COUNT; cell++) {
            if (differs(before, after, cell)) changed++;
        }
        if (changed == 0) return null;

        int[] cells = new int[changed];
        int[] valuesBefore = new int[changed];
        int[] valuesAfter = new int[changed];
        int[] notesBefore = new int[changed];
        int[] notesAfter = new int[changed];
        int i = 0;
        for (int cell = 0; cell < Cells.COUNT; cell++) {
            if (!differs(before, after, cell)) continue;
            cells[i] = cell;
            valuesBefore[i] = before.value(cell);
            valuesAfter[i] = after.value(cell);
            notesBefore[i] = before.notes(cell);
            notesAfter[i] = after.notes(cell);
            i++;
        }
        return new Move(cells, valuesBefore, valuesAfter, notesBefore, notesAfter);
    }

    /** Клетка, вокруг которой ход случился: к ней возвращается выделение при отмене и повторе. */
    public int cell() {
        return cells[0];
    }

    public Board undo(Board board) {
        return board.with(cells, valuesBefore, notesBefore);
    }

    public Board redo(Board board) {
        return board.with(cells, valuesAfter, notesAfter);
    }

    private static boolean differs(Board before, Board after, int cell) {
        return before.value(cell) != after.value(cell)
                || before.notes(cell) != after.notes(cell);
    }

    /*
     * Запись и чтение — здесь, а не в хранилище: иначе пять внутренних массивов пришлось бы
     * выставить наружу геттерами, и любой желающий смог бы собрать ход, который не является
     * разницей никаких двух досок.
     */

    public void write(DataOutputStream out) throws IOException {
        out.writeInt(cells.length);
        for (int i = 0; i < cells.length; i++) {
            out.writeInt(cells[i]);
            out.writeInt(valuesBefore[i]);
            out.writeInt(valuesAfter[i]);
            out.writeInt(notesBefore[i]);
            out.writeInt(notesAfter[i]);
        }
    }

    public static Move read(DataInputStream in) throws IOException {
        int size = in.readInt();
        // Сохранение испорчено: дальше читать нечего, партия начнётся заново.
        if (size <= 0 || size > Cells.COUNT) throw new IOException();
        int[] cells = new int[size];
        int[] valuesBefore = new int[size];
        int[] valuesAfter = new int[size];
        int[] notesBefore = new int[size];
        int[] notesAfter = new int[size];
        for (int i = 0; i < size; i++) {
            cells[i] = in.readInt();
            valuesBefore[i] = in.readInt();
            valuesAfter[i] = in.readInt();
            notesBefore[i] = in.readInt();
            notesAfter[i] = in.readInt();
            if (cells[i] < 0 || cells[i] >= Cells.COUNT) throw new IOException();
        }
        return new Move(cells, valuesBefore, valuesAfter, notesBefore, notesAfter);
    }
}
