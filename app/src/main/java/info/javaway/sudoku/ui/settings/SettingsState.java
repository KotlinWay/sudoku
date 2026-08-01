package info.javaway.sudoku.ui.settings;

/** Что показывает экран настроек. Два переключателя — всё остальное на нём это ссылки. */
public final class SettingsState {

    public final boolean candidates;
    public final boolean sound;

    private SettingsState(boolean candidates, boolean sound) {
        this.candidates = candidates;
        this.sound = sound;
    }

    /** Пока настройки не прочитаны с диска, показываем значения по умолчанию. */
    public static SettingsState initial() {
        return new SettingsState(false, true);
    }

    public SettingsState loaded(boolean candidates, boolean sound) {
        return new SettingsState(candidates, sound);
    }

    public SettingsState candidates(boolean value) {
        return new SettingsState(value, sound);
    }

    public SettingsState sound(boolean value) {
        return new SettingsState(candidates, value);
    }
}
