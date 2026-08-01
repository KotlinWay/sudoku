package info.javaway.sudoku.settings;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Две настройки игры.
 *
 * Звук включён по умолчанию: отклик на ход — часть игры, а выключить его человек может
 * и кнопкой громкости. Показ возможных цифр выключен: это заметная поблажка, и включать
 * её за игрока нельзя.
 */
public final class Prefs {

    private static final String FILE = "settings";
    private static final String SOUND = "sound";
    private static final String CANDIDATES = "candidates";

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
}
