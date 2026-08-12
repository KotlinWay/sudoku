package info.javaway.sudoku.ui.game;

/** Решает только системное поведение экрана; состояние партии не меняет. */
final class ScreenPolicy {

    static boolean keepOn(boolean enabled, GameState.Phase phase, boolean modalVisible) {
        return enabled && phase == GameState.Phase.PLAYING && !modalVisible;
    }

    static boolean shouldChange(boolean current, boolean enabled, GameState.Phase phase,
                                boolean modalVisible) {
        return current != keepOn(enabled, phase, modalVisible);
    }

    private ScreenPolicy() {
    }
}
