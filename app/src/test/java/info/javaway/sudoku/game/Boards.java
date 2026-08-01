package info.javaway.sudoku.game;

/**
 * Доски для тестов. Решение берётся построением, а не генератором: тест про правила игры
 * не должен падать оттого, что генератор стал выдавать другую задачу.
 */
public final class Boards {

    /**
     * Классическое построение решённой сетки сдвигами строк. Каждая следующая строка
     * сдвинута на три, каждый следующий пояс — ещё на один; правила судоку это соблюдает.
     */
    public static int[] solution() {
        int[] grid = new int[Cells.COUNT];
        for (int row = 0; row < Cells.SIDE; row++) {
            for (int column = 0; column < Cells.SIDE; column++) {
                grid[Cells.at(row, column)] = (row * 3 + row / 3 + column) % Cells.SIDE + 1;
            }
        }
        return grid;
    }

    /** Доска, где пусты только перечисленные клетки. Всё остальное — данности. */
    public static Board withEmpty(int... cells) {
        int[] solution = solution();
        int[] puzzle = solution.clone();
        for (int cell : cells) {
            puzzle[cell] = 0;
        }
        return Board.of(puzzle, solution);
    }

    /** Верный ответ для клетки. */
    public static int answer(int cell) {
        return solution()[cell];
    }

    /** Любая цифра, кроме верной: чем именно ошибиться, тесту всё равно. */
    public static int wrongAnswer(int cell) {
        int right = answer(cell);
        return right == 1 ? 2 : 1;
    }

    private Boards() {
    }
}
