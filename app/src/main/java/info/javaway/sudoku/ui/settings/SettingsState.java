package info.javaway.sudoku.ui.settings;

import info.javaway.sudoku.settings.Theme;

/** Что показывает экран настроек. Три настройки — всё остальное на нём это ссылки. */
public final class SettingsState {

    public final boolean candidates;
    public final boolean sound;
    public final Theme theme;

    private SettingsState(boolean candidates, boolean sound, Theme theme) {
        this.candidates = candidates;
        this.sound = sound;
        this.theme = theme;
    }

    /** Пока настройки не прочитаны с диска, показываем значения по умолчанию. */
    public static SettingsState initial() {
        return new SettingsState(false, true, Theme.SYSTEM);
    }

    public SettingsState loaded(boolean candidates, boolean sound, Theme theme) {
        return new SettingsState(candidates, sound, theme);
    }

    public SettingsState candidates(boolean value) {
        return new SettingsState(value, sound, theme);
    }

    public SettingsState sound(boolean value) {
        return new SettingsState(candidates, value, theme);
    }

    public SettingsState theme(Theme value) {
        return new SettingsState(candidates, sound, value);
    }
}
