package info.javaway.sudoku.ui;

import android.content.Context;

import info.javaway.sudoku.R;
import info.javaway.sudoku.game.Difficulty;

/**
 * Подписи, которые нужны больше чем одному экрану: название уровня, его отличия и время.
 * Заведено, чтобы «Задача дня» и «03:12» не разошлись между экраном игры, экраном новой
 * игры и статистикой — а разошлись бы они при первой же правке одной из трёх копий.
 */
public final class Labels {

    public static String level(Context context, Difficulty level, boolean daily) {
        if (daily) return context.getString(R.string.level_daily);
        switch (level) {
            case EASY: return context.getString(R.string.level_easy);
            case MEDIUM: return context.getString(R.string.level_medium);
            case HARD: return context.getString(R.string.level_hard);
            default: return context.getString(R.string.level_expert);
        }
    }

    /** Чем уровень отличается от соседних: сколько цифр открыто и сколько ошибок прощается. */
    public static String note(Context context, Difficulty level) {
        return context.getString(R.string.level_note,
                context.getResources().getQuantityString(
                        R.plurals.clues, level.clues, level.clues),
                context.getResources().getQuantityString(
                        R.plurals.lives, level.mistakeLimit, level.mistakeLimit));
    }

    public static String time(Context context, int seconds) {
        return context.getString(R.string.time, seconds / 60, seconds % 60);
    }

    private Labels() {
    }
}
