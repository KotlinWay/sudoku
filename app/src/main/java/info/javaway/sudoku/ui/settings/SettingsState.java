package info.javaway.sudoku.ui.settings;

import info.javaway.sudoku.settings.Theme;

/** Что показывает экран настроек. Четыре настройки — всё остальное на нём это ссылки. */
public final class SettingsState {

    public final boolean candidates;
    public final boolean sound;
    public final boolean keepScreenOn;
    public final Theme theme;

    private SettingsState(boolean candidates, boolean sound, boolean keepScreenOn, Theme theme) {
        this.candidates = candidates;
        this.sound = sound;
        this.keepScreenOn = keepScreenOn;
        this.theme = theme;
    }

    /**
     * Состояние, с которым экран открывается. Настройки читаются до первой отрисовки,
     * поэтому промежуточного состояния «ещё не прочитано» у экрана нет — а вместе с ним
     * нет и переключателей, доигрывающих анимацию на глазах у открывшего экран.
     */
    public static SettingsState of(boolean candidates, boolean sound,
                                   boolean keepScreenOn, Theme theme) {
        return new SettingsState(candidates, sound, keepScreenOn, theme);
    }

    public SettingsState candidates(boolean value) {
        return new SettingsState(value, sound, keepScreenOn, theme);
    }

    public SettingsState sound(boolean value) {
        return new SettingsState(candidates, value, keepScreenOn, theme);
    }

    public SettingsState keepScreenOn(boolean value) {
        return new SettingsState(candidates, sound, value, theme);
    }

    public SettingsState theme(Theme value) {
        return new SettingsState(candidates, sound, keepScreenOn, value);
    }
}
