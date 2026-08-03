package info.javaway.sudoku.ui.game;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import info.javaway.sudoku.R;
import info.javaway.sudoku.core.Background;
import info.javaway.sudoku.game.Difficulty;
import info.javaway.sudoku.stats.Stats;
import info.javaway.sudoku.stats.StatsStore;
import info.javaway.sudoku.ui.Labels;

/**
 * Итоги всех партий. Отдельного экрана не заводим: это четыре числа и пять строк, которые
 * смотрят раз в неделю, — окно поверх доски честнее целой Activity в манифесте.
 *
 * Своего состояния у окна нет, поэтому и своего MVI-цикла тоже: оно показывает то, что
 * прочитало с диска в момент открытия, и закрывается.
 */
final class StatsDialog {

    interface OnReset {
        void onReset();
    }

    static void show(Activity activity, StatsStore store, OnReset onReset) {
        Background.work(() -> {
            Stats stats = store.load();
            Background.main(() -> {
                if (activity.isFinishing()) return;
                display(activity, store, stats, onReset);
            });
        });
    }

    private static void display(Activity activity, StatsStore store, Stats stats,
                                OnReset onReset) {
        View view = LayoutInflater.from(activity).inflate(R.layout.dialog_stats, null);

        tile(view, R.id.tile_played, R.string.stats_played, String.valueOf(stats.played));
        tile(view, R.id.tile_wins, R.string.stats_wins, String.valueOf(stats.wins));
        tile(view, R.id.tile_rate, R.string.stats_win_rate,
                activity.getString(R.string.stats_percent, stats.winRate()));
        tile(view, R.id.tile_streak, R.string.stats_streak, String.valueOf(stats.streak));

        ((TextView) view.findViewById(R.id.best_streak)).setText(
                activity.getString(R.string.stats_streak_best, stats.bestStreak));

        best(activity, view, R.id.best_easy, R.string.level_easy, stats.best(Difficulty.EASY));
        best(activity, view, R.id.best_medium, R.string.level_medium,
                stats.best(Difficulty.MEDIUM));
        best(activity, view, R.id.best_hard, R.string.level_hard, stats.best(Difficulty.HARD));
        best(activity, view, R.id.best_expert, R.string.level_expert,
                stats.best(Difficulty.EXPERT));

        new AlertDialog.Builder(activity)
                .setTitle(R.string.stats)
                .setView(view)
                .setPositiveButton(R.string.close, null)
                .setNeutralButton(R.string.stats_reset,
                        (dialog, which) -> confirmReset(activity, store, onReset))
                .show();
    }

    /**
     * Сброс необратим, поэтому спрашиваем и говорим прямо, что именно исчезнет. Начатая
     * партия при этом не трогается — про это в вопросе сказано отдельно, иначе человек
     * решит, что теряет и её.
     */
    private static void confirmReset(Activity activity, StatsStore store, OnReset onReset) {
        new AlertDialog.Builder(activity)
                .setTitle(R.string.stats_reset_title)
                .setMessage(R.string.stats_reset_text)
                .setPositiveButton(R.string.stats_reset, (dialog, which) -> {
                    Background.work(store::clear);
                    onReset.onReset();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private static void tile(View root, int id, int label, String value) {
        View tile = root.findViewById(id);
        ((TextView) tile.findViewById(R.id.tile_value)).setText(value);
        ((TextView) tile.findViewById(R.id.tile_label)).setText(label);
    }

    private static void best(Activity activity, View root, int id, int level, int seconds) {
        View row = root.findViewById(id);
        ((TextView) row.findViewById(R.id.best_level)).setText(level);
        ((TextView) row.findViewById(R.id.best_time)).setText(seconds == Stats.NO_TIME
                ? activity.getString(R.string.stats_no_time)
                : Labels.time(activity, seconds));
    }

    private StatsDialog() {
    }
}
