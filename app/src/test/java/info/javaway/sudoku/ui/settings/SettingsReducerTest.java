package info.javaway.sudoku.ui.settings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import info.javaway.sudoku.settings.Theme;
import info.javaway.sudoku.ui.mvi.Update;

public class SettingsReducerTest {

    private static final SettingsReducer REDUCER = new SettingsReducer();

    private static SettingsState state(boolean keepScreenOn) {
        return SettingsState.of(false, true, keepScreenOn, Theme.DARK);
    }

    @Test public void enablingKeepScreenOnPreservesOtherSettings() {
        Update<SettingsState, SettingsEffect> update = REDUCER.reduce(
                state(false), new SettingsAction.KeepScreenOnToggled(true));

        assertTrue(update.state.keepScreenOn);
        assertFalse(update.state.candidates);
        assertTrue(update.state.sound);
        assertEquals(Theme.DARK, update.state.theme);
        assertEquals(1, update.effects.size());
        assertTrue(update.effects.get(0) instanceof SettingsEffect.SaveKeepScreenOn);
        assertTrue(((SettingsEffect.SaveKeepScreenOn) update.effects.get(0)).value);
    }

    @Test public void disablingKeepScreenOnRequestsFalseSave() {
        Update<SettingsState, SettingsEffect> update = REDUCER.reduce(
                state(true), new SettingsAction.KeepScreenOnToggled(false));

        assertFalse(update.state.keepScreenOn);
        assertEquals(1, update.effects.size());
        assertFalse(((SettingsEffect.SaveKeepScreenOn) update.effects.get(0)).value);
    }

    @Test public void repeatingKeepScreenOnValueDoesNothing() {
        SettingsState before = state(true);

        Update<SettingsState, SettingsEffect> update = REDUCER.reduce(
                before, new SettingsAction.KeepScreenOnToggled(true));

        assertSame(before, update.state);
        assertTrue(update.effects.isEmpty());
    }
}
