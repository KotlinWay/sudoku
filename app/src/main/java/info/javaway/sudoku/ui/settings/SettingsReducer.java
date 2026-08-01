package info.javaway.sudoku.ui.settings;

import info.javaway.sudoku.ui.mvi.Reducer;
import info.javaway.sudoku.ui.mvi.Update;

public final class SettingsReducer
        implements Reducer<SettingsState, SettingsAction, SettingsEffect> {

    @Override
    public Update<SettingsState, SettingsEffect> reduce(SettingsState state,
                                                        SettingsAction action) {
        if (action instanceof SettingsAction.Loaded) {
            SettingsAction.Loaded loaded = (SettingsAction.Loaded) action;
            return Update.state(state.loaded(loaded.candidates, loaded.sound));
        }
        if (action instanceof SettingsAction.CandidatesToggled) {
            boolean value = ((SettingsAction.CandidatesToggled) action).value;
            return Update.of(state.candidates(value), new SettingsEffect.SaveCandidates(value));
        }
        if (action instanceof SettingsAction.SoundToggled) {
            boolean value = ((SettingsAction.SoundToggled) action).value;
            return Update.of(state.sound(value), new SettingsEffect.SaveSound(value));
        }
        if (action instanceof SettingsAction.LinkClicked) {
            return Update.of(state,
                    new SettingsEffect.OpenLink(((SettingsAction.LinkClicked) action).url));
        }
        if (action instanceof SettingsAction.RateClicked) {
            return Update.of(state, new SettingsEffect.Rate());
        }
        if (action instanceof SettingsAction.WriteClicked) {
            return Update.of(state, new SettingsEffect.Write());
        }
        return Update.state(state);
    }
}
