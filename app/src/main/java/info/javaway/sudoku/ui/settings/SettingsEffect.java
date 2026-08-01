package info.javaway.sudoku.ui.settings;

/** Одноразовые команды наружу: запись на диск и открытие ссылок. */
public abstract class SettingsEffect {

    public static final class Load extends SettingsEffect {
    }

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

    public static final class OpenLink extends SettingsEffect {
        public final String url;

        public OpenLink(String url) {
            this.url = url;
        }
    }

    public static final class Rate extends SettingsEffect {
    }

    public static final class Write extends SettingsEffect {
    }
}
