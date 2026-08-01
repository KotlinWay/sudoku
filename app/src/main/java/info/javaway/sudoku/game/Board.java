package info.javaway.sudoku.game;

/**
 * Доска целиком: решение, открытые с начала цифры, что вписал игрок и его карандашные пометки.
 * Иммутабельна — любое изменение возвращает новую доску. Отсюда бесплатно берутся отмена хода
 * (старая доска никуда не делась) и уверенность, что экран не покажет полусделанный ход.
 *
 * Решение и маска данностей общие для всех копий: за партию они не меняются, копировать их
 * на каждый ход незачем. Наружу оба массива не выдаются, испортить их некому.
 */
public final class Board {

    private final int[] solution;
    private final boolean[] given;
    private final int[] values;
    private final int[] notes;

    private Board(int[] solution, boolean[] given, int[] values, int[] notes) {
        this.solution = solution;
        this.given = given;
        this.values = values;
        this.notes = notes;
    }

    /**
     * @param puzzle   задача: цифра или 0 в каждой из 81 клетки
     * @param solution полное решение этой задачи
     */
    public static Board of(int[] puzzle, int[] solution) {
        boolean[] given = new boolean[Cells.COUNT];
        for (int cell = 0; cell < Cells.COUNT; cell++) {
            given[cell] = puzzle[cell] != 0;
        }
        return new Board(solution.clone(), given, puzzle.clone(), new int[Cells.COUNT]);
    }

    /** Восстановление сохранённой партии: значения и пометки уже свои, задача — только маска. */
    public static Board restored(int[] puzzle, int[] solution, int[] values, int[] notes) {
        boolean[] given = new boolean[Cells.COUNT];
        for (int cell = 0; cell < Cells.COUNT; cell++) {
            given[cell] = puzzle[cell] != 0;
        }
        return new Board(solution.clone(), given, values.clone(), notes.clone());
    }

    public int value(int cell) {
        return values[cell];
    }

    public int answer(int cell) {
        return solution[cell];
    }

    public boolean isGiven(int cell) {
        return given[cell];
    }

    public int notes(int cell) {
        return notes[cell];
    }

    public boolean isEmpty(int cell) {
        return values[cell] == 0;
    }

    /** Копия задачи — нужна только сохранению: по ней восстанавливается маска данностей. */
    public int[] puzzle() {
        int[] puzzle = new int[Cells.COUNT];
        for (int cell = 0; cell < Cells.COUNT; cell++) {
            puzzle[cell] = given[cell] ? values[cell] : 0;
        }
        return puzzle;
    }

    public int[] solution() {
        return solution.clone();
    }

    public int[] values() {
        return values.clone();
    }

    public int[] notesArray() {
        return notes.clone();
    }

    public Board withValue(int cell, int digit) {
        int[] next = values.clone();
        next[cell] = digit;
        return new Board(solution, given, next, notes);
    }

    public Board withNotes(int cell, int mask) {
        int[] next = notes.clone();
        next[cell] = mask;
        return new Board(solution, given, values, next);
    }

    /** Замена сразу нескольких клеток — так отмена и повтор хода возвращают доску одним шагом. */
    public Board with(int[] cells, int[] cellValues, int[] cellNotes) {
        int[] nextValues = values.clone();
        int[] nextNotes = notes.clone();
        for (int i = 0; i < cells.length; i++) {
            nextValues[cells[i]] = cellValues[i];
            nextNotes[cells[i]] = cellNotes[i];
        }
        return new Board(solution, given, nextValues, nextNotes);
    }

    /**
     * Цифра в клетке повторяется среди её соседей. Это видно игроку сразу и не требует
     * заглядывать в решение: конфликт — свойство самой доски, а не знания правильного ответа.
     */
    public boolean isDuplicate(int cell) {
        int digit = values[cell];
        if (digit == 0) return false;
        for (int peer : Cells.peers(cell)) {
            if (values[peer] == digit) return true;
        }
        return false;
    }

    /** Цифра не совпадает с решением. Так игра узнаёт, что ход стоил жизни. */
    public boolean isWrong(int cell) {
        return values[cell] != 0 && values[cell] != solution[cell];
    }

    /** Цифры, которые можно вписать в пустую клетку, не создав повтора. Маска как у пометок. */
    public int candidates(int cell) {
        if (values[cell] != 0) return Notes.NONE;
        int taken = Notes.NONE;
        for (int peer : Cells.peers(cell)) {
            taken = Notes.with(taken, values[peer]);
        }
        int free = Notes.NONE;
        for (int digit = 1; digit <= Cells.SIDE; digit++) {
            if (!Notes.has(taken, digit)) free = Notes.with(free, digit);
        }
        return free;
    }

    public int placed(int digit) {
        int count = 0;
        for (int value : values) {
            if (value == digit) count++;
        }
        return count;
    }

    /** Цифра расставлена везде — на клавиатуре её больше предлагать незачем. */
    public boolean isComplete(int digit) {
        return placed(digit) >= Cells.SIDE;
    }

    /**
     * Пустая клетка проверяется отдельно от несовпадения нарочно: пустая доска — это ещё
     * и доска, которую только что попросили сгенерировать, и решённой она быть не должна.
     */
    public boolean isSolved() {
        for (int cell = 0; cell < Cells.COUNT; cell++) {
            if (values[cell] == 0 || values[cell] != solution[cell]) return false;
        }
        return true;
    }

    /** Доска-заглушка на время, пока генератор считает задачу: рисуется пустой сеткой. */
    public static Board blank() {
        return new Board(new int[Cells.COUNT], new boolean[Cells.COUNT],
                new int[Cells.COUNT], new int[Cells.COUNT]);
    }
}
