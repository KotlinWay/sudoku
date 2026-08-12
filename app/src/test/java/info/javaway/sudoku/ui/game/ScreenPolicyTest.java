package info.javaway.sudoku.ui.game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ScreenPolicyTest {

    @Test public void enabledPreferenceKeepsScreenOnOnlyDuringPlaying() {
        for (GameState.Phase phase : GameState.Phase.values()) {
            assertEquals(phase.name(), phase == GameState.Phase.PLAYING,
                    ScreenPolicy.keepOn(true, phase, false));
        }
    }

    @Test public void disabledPreferenceNeverKeepsScreenOn() {
        for (GameState.Phase phase : GameState.Phase.values()) {
            assertFalse(phase.name(), ScreenPolicy.keepOn(false, phase, false));
        }
    }

    @Test public void visibleModalReleasesScreenThroughoutPlaying() {
        assertFalse(ScreenPolicy.keepOn(
                true, GameState.Phase.PLAYING, true));
        assertTrue(ScreenPolicy.shouldChange(
                true, true, GameState.Phase.PLAYING, true));
        assertFalse(ScreenPolicy.shouldChange(
                false, true, GameState.Phase.PLAYING, true));
    }

    @Test public void changesWindowFlagOnlyWhenItDiffersFromPolicy() {
        assertFalse(ScreenPolicy.shouldChange(
                false, false, GameState.Phase.PLAYING, false));
        assertFalse(ScreenPolicy.shouldChange(
                true, true, GameState.Phase.PLAYING, false));
        assertTrue(ScreenPolicy.shouldChange(
                false, true, GameState.Phase.PLAYING, false));
        assertTrue(ScreenPolicy.shouldChange(
                true, true, GameState.Phase.PAUSED, false));
    }
}
