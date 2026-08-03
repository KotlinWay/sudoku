package info.javaway.sudoku.stats;

import info.javaway.sudoku.game.Day;
import info.javaway.sudoku.game.Difficulty;

/**
 * Итоги всех партий. Иммутабельна и ничего не знает про хранилище: подсчёт серии и рекорда —
 * чистая арифметика, и проверяется она на JVM без телефона.
 */
public final class Stats {

    /** Ячеек под лучшее время: по одной на уровень. */
    static final int SLOTS = Difficulty.values().length;

    /** Времени ещё нет. Ноль секунд партия занять не может, поэтому годится как «нет». */
    public static final int NO_TIME = 0;

    public final int played;
    public final int wins;
    public final int streak;
    public final int bestStreak;
    public final int lastWinDay;
    public final int hintsUsed;

    private final int[] best;

    Stats(int played, int wins, int streak, int bestStreak, int lastWinDay,
          int hintsUsed, int[] best) {
        this.played = played;
        this.wins = wins;
        this.streak = streak;
        this.bestStreak = bestStreak;
        this.lastWinDay = lastWinDay;
        this.hintsUsed = hintsUsed;
        this.best = best;
    }

    public static Stats empty() {
        return new Stats(0, 0, 0, 0, Day.NEVER, 0, new int[SLOTS]);
    }

    public int best(Difficulty level) {
        return best[level.ordinal()];
    }

    /** Доля побед в процентах. Ноль партий — ноль процентов, а не деление на ноль. */
    public int winRate() {
        return played == 0 ? 0 : Math.round(wins * 100f / played);
    }

    /**
     * Проигрыш тоже партия. В прототипе счётчик сыгранных рос только при победе, и доля побед
     * там всегда показывала сто процентов.
     */
    public Stats afterLoss() {
        return new Stats(played + 1, wins, streak, bestStreak, lastWinDay, hintsUsed, best);
    }

    /**
     * Победа. Лучшее время засчитывается только без подсказок: иначе рекорд ставится тремя
     * нажатиями на лампочку и перестаёт что-либо значить.
     */
    public Outcome afterWin(Difficulty level, int seconds, int hints, Day today) {
        int slot = level.ordinal();
        int previous = best[slot];
        boolean record = hints == 0 && (previous == NO_TIME || seconds < previous);

        int[] nextBest = best.clone();
        if (record) nextBest[slot] = seconds;

        int nextStreak = nextStreak(today);
        Stats next = new Stats(played + 1, wins + 1, nextStreak,
                Math.max(bestStreak, nextStreak), today.number, hintsUsed + hints, nextBest);
        return new Outcome(next, nextBest[slot], record);
    }

    /**
     * Серия: вчерашняя победа её продолжает, сегодняшняя — не удлиняет второй раз,
     * любая другая — начинает заново.
     */
    private int nextStreak(Day today) {
        if (lastWinDay == today.number) return Math.max(streak, 1);
        if (today.isYesterday(lastWinDay)) return streak + 1;
        return 1;
    }

    /* Доступ для хранилища: массив наружу отдаётся копией, испортить состояние им нельзя. */

    int[] bestTimes() {
        return best.clone();
    }

    static Stats restored(int played, int wins, int streak, int bestStreak, int lastWinDay,
                          int hintsUsed, int[] best) {
        int[] times = new int[SLOTS];
        System.arraycopy(best, 0, times, 0, Math.min(best.length, SLOTS));
        return new Stats(played, wins, streak, bestStreak, lastWinDay, hintsUsed, times);
    }
}
