package info.javaway.sudoku.game;

/**
 * Что происходит с доской от действий игрока. Отдельно от {@link Board}, потому что доска —
 * это данные и правила их целостности, а здесь правила игры: они меняются от замысла,
 * а не от структуры данных.
 */
public final class Rules {

    /**
     * Вписать цифру. Заодно снимается ставшая ложью пометка о ней у соседей: цифра уже стоит
     * в этой строке, столбце и квадрате, карандашный след о ней в них — мусор, который игрок
     * иначе стирает руками по двадцать клеток за ход. Отмена вернёт пометки на место сама.
     */
    public static Board place(Board board, int cell, int digit) {
        if (board.isGiven(cell)) return board;
        Board next = board.withValue(cell, digit).withNotes(cell, Notes.NONE);
        for (int peer : Cells.peers(cell)) {
            int notes = next.notes(peer);
            if (Notes.has(notes, digit)) {
                next = next.withNotes(peer, Notes.without(notes, digit));
            }
        }
        return next;
    }

    /** Стереть и цифру, и пометки: кнопка одна, и она означает «в этой клетке ничего нет». */
    public static Board erase(Board board, int cell) {
        if (board.isGiven(cell)) return board;
        return board.withValue(cell, 0).withNotes(cell, Notes.NONE);
    }

    /** Карандаш пишет только по пустой клетке: помечать то, где уже стоит цифра, нечего. */
    public static Board note(Board board, int cell, int digit) {
        if (board.isGiven(cell) || !board.isEmpty(cell)) return board;
        return board.withNotes(cell, Notes.toggled(board.notes(cell), digit));
    }

    /** Подсказка вписывает верную цифру и дальше ведёт себя как обычный ход. */
    public static Board reveal(Board board, int cell) {
        return place(board, cell, board.answer(cell));
    }

    /**
     * Куда потратить подсказку. Выбранная пустая клетка честнее случайной: игрок показал,
     * где именно застрял. Если выбора нет — берём пустую клетку по счётчику, а не по часам,
     * чтобы поведение оставалось воспроизводимым в тестах.
     *
     * @return индекс клетки или -1, если пустых не осталось
     */
    public static int hintCell(Board board, int selected, int pick) {
        if (selected >= 0 && board.isEmpty(selected)) return selected;
        int empty = 0;
        for (int cell = 0; cell < Cells.COUNT; cell++) {
            if (board.isEmpty(cell)) empty++;
        }
        if (empty == 0) return -1;
        int target = Math.floorMod(pick, empty);
        for (int cell = 0; cell < Cells.COUNT; cell++) {
            if (board.isEmpty(cell) && target-- == 0) return cell;
        }
        return -1;
    }

    private Rules() {
    }
}
