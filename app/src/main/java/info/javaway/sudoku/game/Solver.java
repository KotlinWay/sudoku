package info.javaway.sudoku.game;

/**
 * Перебор с возвратом — общий и для «дай решённую сетку», и для «а решение точно одно?».
 *
 * Кандидаты клетки хранятся масками занятых цифр по строке, столбцу и квадрату, а следующей
 * берётся клетка с наименьшим числом вариантов. Без этого правила простой перебор слева
 * направо на пустой сетке уходит в миллионы шагов и заметно тормозит генерацию.
 */
final class Solver {

    private static final int ALL = 0x3FE;   // биты 1…9

    /**
     * Заполняет пустую (или частично заполненную) сетку. Порядок цифр случайный, поэтому
     * из одного и того же seed выходит одна и та же сетка, а из разных — разные.
     *
     * @return true, если решение нашлось; сетка при этом заполнена
     */
    static boolean fill(int[] grid, Rng rng) {
        return search(grid, masks(grid), rng, 1) == 1;
    }

    /**
     * Сколько у задачи решений, но не больше limit. Считать все смысла нет: чтобы отклонить
     * задачу, достаточно найти второе.
     */
    static int count(int[] grid, int limit) {
        int[] copy = grid.clone();
        return search(copy, masks(copy), null, limit);
    }

    /**
     * @param limit сколько решений хватит. Дойдя до него, перебор возвращается наверх,
     *              не отменяя расстановку — на этом и держится {@link #fill}.
     */
    private static int search(int[] grid, int[] used, Rng rng, int limit) {
        int cell = scarcest(grid, used);
        if (cell < 0) return 1;                     // пустых клеток нет — сетка решена

        int free = ALL & ~usedFor(used, cell);
        if (free == 0) return 0;                    // тупик: клетку нечем занять

        int[] digits = digits(free, rng);
        int found = 0;
        for (int digit : digits) {
            put(grid, used, cell, digit);
            found += search(grid, used, rng, limit - found);
            if (found >= limit) return found;       // расстановка намеренно остаётся на доске
            take(grid, used, cell, digit);
        }
        return found;
    }

    /**
     * Пустая клетка с наименьшим числом вариантов. Клетка без вариантов возвращается сразу:
     * дальше искать нечего, ветка всё равно оборвётся.
     *
     * @return индекс клетки или -1, если пустых не осталось
     */
    private static int scarcest(int[] grid, int[] used) {
        int best = -1;
        int bestCount = Cells.SIDE + 1;
        for (int cell = 0; cell < Cells.COUNT; cell++) {
            if (grid[cell] != 0) continue;
            int count = Integer.bitCount(ALL & ~usedFor(used, cell));
            if (count < bestCount) {
                best = cell;
                bestCount = count;
                if (count <= 1) return best;
            }
        }
        return best;
    }

    private static int[] digits(int free, Rng rng) {
        int[] digits = new int[Integer.bitCount(free)];
        int size = 0;
        for (int digit = 1; digit <= Cells.SIDE; digit++) {
            if (Notes.has(free, digit)) digits[size++] = digit;
        }
        if (rng != null) rng.shuffle(digits);
        return digits;
    }

    /*
     * Три маски занятости лежат в одном массиве подряд: строки, столбцы, квадраты.
     * Так их не надо таскать по перебору тремя параметрами.
     */
    private static final int ROWS = 0;
    private static final int COLUMNS = Cells.SIDE;
    private static final int BOXES = Cells.SIDE * 2;

    private static int[] masks(int[] grid) {
        int[] used = new int[Cells.SIDE * 3];
        for (int cell = 0; cell < Cells.COUNT; cell++) {
            if (grid[cell] != 0) put(null, used, cell, grid[cell]);
        }
        return used;
    }

    private static int usedFor(int[] used, int cell) {
        return used[ROWS + Cells.row(cell)]
                | used[COLUMNS + Cells.column(cell)]
                | used[BOXES + Cells.box(cell)];
    }

    private static void put(int[] grid, int[] used, int cell, int digit) {
        if (grid != null) grid[cell] = digit;
        used[ROWS + Cells.row(cell)] |= Notes.bit(digit);
        used[COLUMNS + Cells.column(cell)] |= Notes.bit(digit);
        used[BOXES + Cells.box(cell)] |= Notes.bit(digit);
    }

    private static void take(int[] grid, int[] used, int cell, int digit) {
        grid[cell] = 0;
        used[ROWS + Cells.row(cell)] &= ~Notes.bit(digit);
        used[COLUMNS + Cells.column(cell)] &= ~Notes.bit(digit);
        used[BOXES + Cells.box(cell)] &= ~Notes.bit(digit);
    }

    private Solver() {
    }
}
