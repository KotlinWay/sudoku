package info.javaway.sudoku.stats;

import android.content.Context;
import android.content.SharedPreferences;

import info.javaway.sudoku.game.Day;

/** Итоги партий на диске. Всё умещается в десяток чисел, поэтому обычные настройки. */
public final class StatsStore {

    private static final String FILE = "stats";

    private static final String PLAYED = "played";
    private static final String WINS = "wins";
    private static final String STREAK = "streak";
    private static final String BEST_STREAK = "best_streak";
    private static final String LAST_WIN_DAY = "last_win_day";
    private static final String HINTS_USED = "hints_used";
    private static final String DAILY_SOLVED_DAY = "daily_solved_day";
    private static final String BEST = "best_";

    private final SharedPreferences preferences;

    public StatsStore(Context context) {
        preferences = context.getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }

    public Stats load() {
        int[] best = new int[Stats.SLOTS];
        for (int slot = 0; slot < best.length; slot++) {
            best[slot] = preferences.getInt(BEST + slot, Stats.NO_TIME);
        }
        return Stats.restored(
                preferences.getInt(PLAYED, 0),
                preferences.getInt(WINS, 0),
                preferences.getInt(STREAK, 0),
                preferences.getInt(BEST_STREAK, 0),
                preferences.getInt(LAST_WIN_DAY, Day.NEVER),
                preferences.getInt(HINTS_USED, 0),
                preferences.getInt(DAILY_SOLVED_DAY, Day.NEVER),
                best);
    }

    public void save(Stats stats) {
        SharedPreferences.Editor editor = preferences.edit()
                .putInt(PLAYED, stats.played)
                .putInt(WINS, stats.wins)
                .putInt(STREAK, stats.streak)
                .putInt(BEST_STREAK, stats.bestStreak)
                .putInt(LAST_WIN_DAY, stats.lastWinDay)
                .putInt(HINTS_USED, stats.hintsUsed)
                .putInt(DAILY_SOLVED_DAY, stats.dailySolvedDay);
        int[] best = stats.bestTimes();
        for (int slot = 0; slot < best.length; slot++) {
            editor.putInt(BEST + slot, best[slot]);
        }
        editor.apply();
    }

    public void clear() {
        preferences.edit().clear().apply();
    }
}
