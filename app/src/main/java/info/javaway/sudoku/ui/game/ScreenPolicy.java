package info.javaway.sudoku.ui.game;

/** Решает только системное поведение экрана; состояние партии не меняет. */
final class ScreenPolicy {

    static boolean keepOn(boolean enabled, GameState.Phase phase) {
        return enabled && phase == GameState.Phase.PLAYING;
    }

    static boolean shouldChange(boolean current, boolean enabled, GameState.Phase phase) {
        return current != keepOn(enabled, phase);
    }

    private ScreenPolicy() {
    }
}
