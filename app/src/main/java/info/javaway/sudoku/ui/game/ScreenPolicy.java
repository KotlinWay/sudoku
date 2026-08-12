package info.javaway.sudoku.ui.game;

/** Решает только системное поведение экрана; состояние партии не меняет. */
final class ScreenPolicy {

    static boolean keepOn(boolean enabled, GameState.Phase phase) {
        return enabled && phase == GameState.Phase.PLAYING;
    }

    private ScreenPolicy() {
    }
}
