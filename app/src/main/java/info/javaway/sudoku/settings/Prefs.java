package info.javaway.sudoku.settings;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Настройки приложения: две про игру и одна про оформление.
 *
 * Звук включён по умолчанию: отклик на ход — часть игры, а выключить его человек может
 * и кнопкой громкости. Показ возможных цифр выключен: это заметная поблажка, и включать
 * её за игрока нельзя. Тема берётся у системы, пока её не переспросили явно.
 */
public final class Prefs {

    private static final String FILE = "settings";
    private static final String SOUND = "sound";
    private static final String CANDIDATES = "candidates";
    private static final String THEME = "theme";

    private final SharedPreferences preferences;

    public Prefs(Context context) {
        preferences = context.getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }

    public boolean sound() {
        return preferences.getBoolean(SOUND, true);
    }

    public void setSound(boolean value) {
        preferences.edit().putBoolean(SOUND, value).apply();
    }

    public boolean candidates() {
        return preferences.getBoolean(CANDIDATES, false);
    }

    public void setCandidates(boolean value) {
        preferences.edit().putBoolean(CANDIDATES, value).apply();
    }

    public Theme theme() {
        return Theme.byName(preferences.getString(THEME, null), Theme.SYSTEM);
    }

    public void setTheme(Theme value) {
        preferences.edit().putString(THEME, value.name()).apply();
    }
}
