package info.javaway.sudoku.ui.game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ScreenPolicyTest {

    @Test public void enabledPreferenceKeepsScreenOnOnlyDuringPlaying() {
        for (GameState.Phase phase : GameState.Phase.values()) {
            assertEquals(phase.name(), phase == GameState.Phase.PLAYING,
                    ScreenPolicy.keepOn(true, phase));
        }
    }

    @Test public void disabledPreferenceNeverKeepsScreenOn() {
        for (GameState.Phase phase : GameState.Phase.values()) {
            assertFalse(phase.name(), ScreenPolicy.keepOn(false, phase));
        }
    }

    @Test public void changesWindowFlagOnlyWhenItDiffersFromPolicy() {
        assertFalse(ScreenPolicy.shouldChange(false, false, GameState.Phase.PLAYING));
        assertFalse(ScreenPolicy.shouldChange(true, true, GameState.Phase.PLAYING));
        assertTrue(ScreenPolicy.shouldChange(false, true, GameState.Phase.PLAYING));
        assertTrue(ScreenPolicy.shouldChange(true, true, GameState.Phase.PAUSED));
    }
}
