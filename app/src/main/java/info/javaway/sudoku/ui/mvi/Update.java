package info.javaway.sudoku.ui.mvi;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Результат редьюсера: новое состояние и список одноразовых эффектов.
 * Эффекты не живут в состоянии намеренно — иначе после пересоздания экрана они сработают
 * повторно, и человек второй раз услышит звук победы за одну победу.
 */
public final class Update<S, E> {

    public final S state;
    public final List<E> effects;

    private Update(S state, List<E> effects) {
        this.state = state;
        this.effects = effects;
    }

    public static <S, E> Update<S, E> state(S state) {
        return new Update<>(state, Collections.<E>emptyList());
    }

    @SafeVarargs
    public static <S, E> Update<S, E> of(S state, E... effects) {
        return new Update<>(state, Collections.unmodifiableList(Arrays.asList(effects)));
    }
}
