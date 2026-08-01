package info.javaway.sudoku.game;

/**
 * Выдача новой задачи: сначала полное решение, потом из него вынимаются цифры.
 *
 * Клетка вынимается только если задача осталась с единственным решением. Прототип на HTML
 * убирал клетки как попало, и на низких уровнях это давало задачи с несколькими решениями:
 * игрок вписывал верную по правилам цифру, а игра засчитывала ошибку и снимала жизнь,
 * потому что сверяется она с одним заранее выбранным решением. Проверка стоит доли секунды
 * один раз за партию и снимает целый класс «игра врёт».
 */
public final class Generator {

    public static Board board(Difficulty level, Rng rng) {
        int[] solution = solved(rng);
        return Board.of(carve(solution, level.clues, rng), solution);
    }

    static int[] solved(Rng rng) {
        int[] grid = new int[Cells.COUNT];
        Solver.fill(grid, rng);
        return grid;
    }

    /**
     * Клетки перебираются в случайном порядке ровно один раз: вынули — проверили — оставили
     * вынутой или вернули. Возвращаться к уже отвергнутой клетке незачем, дальше задача
     * становится только беднее, и второй раз она пройдёт проверку тем более не сможет.
     */
    static int[] carve(int[] solution, int clues, Rng rng) {
        int[] puzzle = solution.clone();
        int[] order = new int[Cells.COUNT];
        for (int cell = 0; cell < Cells.COUNT; cell++) {
            order[cell] = cell;
        }
        rng.shuffle(order);

        int left = Cells.COUNT;
        for (int cell : order) {
            if (left <= clues) break;
            int digit = puzzle[cell];
            puzzle[cell] = 0;
            if (Solver.count(puzzle, 2) == 1) {
                left--;
            } else {
                puzzle[cell] = digit;
            }
        }
        return puzzle;
    }

    private Generator() {
    }
}
