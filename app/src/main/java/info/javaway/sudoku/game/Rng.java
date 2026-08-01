package info.javaway.sudoku.game;

/**
 * Свой генератор случайных чисел вместо {@link java.util.Random}: головоломку дня нужно
 * получать по номеру дня одинаковой на любом телефоне и в любой версии приложения.
 * Платформенный Random такого не обещает — его алгоритм часть реализации, а не контракта.
 * Здесь взят mulberry32: тридцать строк, воспроизводится дословно.
 */
public final class Rng {

    private int state;

    public Rng(int seed) {
        this.state = seed;
    }

    /** Дробное число в [0, 1). */
    public double next() {
        state += 0x6D2B79F5;
        int t = state;
        t = (t ^ (t >>> 15)) * (t | 1);
        t ^= t + (t ^ (t >>> 7)) * (t | 61);
        return ((t ^ (t >>> 14)) & 0xFFFFFFFFL) / 4294967296.0;
    }

    /** Целое в [0, bound). */
    public int next(int bound) {
        return (int) (next() * bound);
    }

    /** Тасование на месте: единственный способ, которым в проекте перемешивают массивы. */
    public void shuffle(int[] values) {
        for (int i = values.length - 1; i > 0; i--) {
            int j = next(i + 1);
            int swap = values[i];
            values[i] = values[j];
            values[j] = swap;
        }
    }
}
