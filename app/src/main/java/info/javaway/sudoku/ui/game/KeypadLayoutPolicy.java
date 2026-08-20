package info.javaway.sudoku.ui.game;

final class KeypadLayoutPolicy {

    private static final int TALL_ROWS = 5;

    private KeypadLayoutPolicy() {}

    static boolean usesTallLayout(int availableHeight, int minCellHeight, int gap) {
        long requiredHeight = (long) TALL_ROWS * minCellHeight
                + (long) (TALL_ROWS - 1) * gap;
        return availableHeight >= requiredHeight;
    }
}
