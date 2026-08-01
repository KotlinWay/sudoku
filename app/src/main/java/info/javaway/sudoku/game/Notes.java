package info.javaway.sudoku.game;

/**
 * Пометки одной клетки — карандашные цифры, которые игрок расставляет, пока не уверен.
 * Хранятся битовой маской: девять флагов в одном int. Так заметки всей доски — это обычный
 * массив из 81 числа, который сравнивается, копируется и сохраняется без всяких коллекций.
 */
public final class Notes {

    public static final int NONE = 0;

    /** Пометка цифры n живёт в бите n: бит 0 не используется, зато n нигде не надо смещать. */
    public static int bit(int digit) {
        return 1 << digit;
    }

    public static boolean has(int notes, int digit) {
        return (notes & bit(digit)) != 0;
    }

    public static int with(int notes, int digit) {
        return notes | bit(digit);
    }

    public static int without(int notes, int digit) {
        return notes & ~bit(digit);
    }

    public static int toggled(int notes, int digit) {
        return notes ^ bit(digit);
    }

    public static boolean isEmpty(int notes) {
        return notes == NONE;
    }

    private Notes() {
    }
}
