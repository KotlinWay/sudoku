package info.javaway.sudoku.ui.game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

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
}
