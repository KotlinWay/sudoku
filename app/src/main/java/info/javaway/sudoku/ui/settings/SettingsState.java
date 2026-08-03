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

    /**
     * Состояние, с которым экран открывается. Настройки читаются до первой отрисовки,
     * поэтому промежуточного состояния «ещё не прочитано» у экрана нет — а вместе с ним
     * нет и переключателей, доигрывающих анимацию на глазах у открывшего экран.
     */
    public static SettingsState of(boolean candidates, boolean sound, Theme theme) {
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
