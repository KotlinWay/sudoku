package info.javaway.sudoku.ui.settings;

public abstract class SettingsAction {

    public static final class Loaded extends SettingsAction {
        public final boolean candidates;
        public final boolean sound;

        public Loaded(boolean candidates, boolean sound) {
            this.candidates = candidates;
            this.sound = sound;
        }
    }

    public static final class CandidatesToggled extends SettingsAction {
        public final boolean value;

        public CandidatesToggled(boolean value) {
            this.value = value;
        }
    }

    public static final class SoundToggled extends SettingsAction {
        public final boolean value;

        public SoundToggled(boolean value) {
            this.value = value;
        }
    }

    public static final class LinkClicked extends SettingsAction {
        public final String url;

        public LinkClicked(String url) {
            this.url = url;
        }
    }

    public static final class RateClicked extends SettingsAction {
    }

    public static final class WriteClicked extends SettingsAction {
    }
}
