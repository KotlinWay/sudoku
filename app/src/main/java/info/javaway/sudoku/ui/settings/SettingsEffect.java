package info.javaway.sudoku.ui.settings;

import info.javaway.sudoku.settings.Theme;

/**
 * Одноразовые команды наружу: запись на диск и открытие ссылок.
 *
 * Чтения тут нет: настройки читаются синхронно до первой отрисовки, потому что файл
 * настроек к этому моменту всё равно уже в памяти — его открыл {@code ThemedActivity},
 * когда выбирал тему в {@code attachBaseContext}.
 */
public abstract class SettingsEffect {

    public static final class SaveCandidates extends SettingsEffect {
        public final boolean value;

        public SaveCandidates(boolean value) {
            this.value = value;
        }
    }

    public static final class SaveSound extends SettingsEffect {
        public final boolean value;

        public SaveSound(boolean value) {
            this.value = value;
        }
    }

    public static final class SaveKeepScreenOn extends SettingsEffect {
        public final boolean value;

        public SaveKeepScreenOn(boolean value) {
            this.value = value;
        }
    }

    /** Записать тему и показать её: экран перестраивается на новых цветах прямо под пальцем. */
    public static final class SaveTheme extends SettingsEffect {
        public final Theme value;

        public SaveTheme(Theme value) {
            this.value = value;
        }
    }

    public static final class OpenLink extends SettingsEffect {
        public final String url;

        public OpenLink(String url) {
            this.url = url;
        }
    }

    public static final class Rate extends SettingsEffect {
    }

    public static final class Raccoon extends SettingsEffect {
    }

    public static final class Write extends SettingsEffect {
    }
}
