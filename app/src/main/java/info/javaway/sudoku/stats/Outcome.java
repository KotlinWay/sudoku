package info.javaway.sudoku.stats;

/**
 * Чем закончилась победа: обновлённая статистика и то, что нужно сказать игроку прямо сейчас.
 * Экрану победы нужен не весь итог, а две цифры — за сколько прошёл и был ли это рекорд.
 */
public final class Outcome {

    public final Stats stats;

    /** Лучшее время на этом уровне после победы. {@link Stats#NO_TIME}, если его ещё нет. */
    public final int best;

    public final boolean record;

    Outcome(Stats stats, int best, boolean record) {
        this.stats = stats;
        this.best = best;
        this.record = record;
    }
}
