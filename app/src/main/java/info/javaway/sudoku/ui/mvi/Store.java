package info.javaway.sudoku.ui.mvi;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Единственное место, где меняется состояние экрана. Activity только шлёт сюда действия
 * и рисует то, что приходит обратно.
 *
 * Класс намеренно не знает про Android: dispatch зовётся с главного потока, фоновые задачи
 * возвращают результат тоже действием.
 */
public final class Store<S, A, E> {

    public interface View<S> {
        void render(S state);
    }

    public interface Effects<E> {
        void handle(E effect);
    }

    private final Reducer<S, A, E> reducer;
    private final Deque<E> pending = new ArrayDeque<>();

    private S state;
    private View<S> view;
    private Effects<E> effects;

    public Store(S initial, Reducer<S, A, E> reducer) {
        this.state = initial;
        this.reducer = reducer;
    }

    public S state() {
        return state;
    }

    public void dispatch(A action) {
        Update<S, E> update = reducer.reduce(state, action);
        state = update.state;
        pending.addAll(update.effects);
        if (view != null) {
            view.render(state);
        }
        drain();
    }

    /** Подключает экран: сразу рисует текущее состояние и отдаёт накопленные эффекты. */
    public void attach(View<S> view, Effects<E> effects) {
        this.view = view;
        this.effects = effects;
        view.render(state);
        drain();
    }

    public void detach() {
        this.view = null;
        this.effects = null;
    }

    private void drain() {
        if (effects == null) return;
        while (!pending.isEmpty()) {
            effects.handle(pending.poll());
        }
    }
}
