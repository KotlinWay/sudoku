package info.javaway.sudoku.game;

/**
 * Арифметика доски: клетка всюду в приложении — это один индекс 0…80, а не пара «строка,
 * столбец». Пара нужна ровно в двух местах — при отрисовке и при озвучивании клетки вслух,
 * и оба берут её отсюда.
 */
public final class Cells {

    public static final int SIDE = 9;
    public static final int BOX = 3;
    public static final int COUNT = SIDE * SIDE;

    /** Соседи клетки: её строка, столбец и квадрат без неё самой. Всегда 20 клеток. */
    public static final int PEERS = 20;

    private static final int[][] PEERS_OF = new int[COUNT][];

    static {
        for (int cell = 0; cell < COUNT; cell++) {
            PEERS_OF[cell] = computePeers(cell);
        }
    }

    public static int row(int cell) {
        return cell / SIDE;
    }

    public static int column(int cell) {
        return cell % SIDE;
    }

    public static int at(int row, int column) {
        return row * SIDE + column;
    }

    /** Номер квадрата 3×3, в котором лежит клетка: 0…8, слева направо и сверху вниз. */
    public static int box(int cell) {
        return row(cell) / BOX * BOX + column(cell) / BOX;
    }

    /**
     * Соседи посчитаны один раз при загрузке класса: их спрашивают на каждой отрисовке
     * доски и на каждой проверке хода, а ответ от хода к ходу не меняется.
     */
    public static int[] peers(int cell) {
        return PEERS_OF[cell];
    }

    public static boolean sees(int cell, int other) {
        return cell != other
                && (row(cell) == row(other) || column(cell) == column(other)
                || box(cell) == box(other));
    }

    private static int[] computePeers(int cell) {
        int[] peers = new int[PEERS];
        int size = 0;
        for (int other = 0; other < COUNT; other++) {
            if (sees(cell, other)) peers[size++] = other;
        }
        return peers;
    }

    private Cells() {
    }
}
