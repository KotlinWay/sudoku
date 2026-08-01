package info.javaway.sudoku.ui.mvi;

/**
 * Чистая функция перехода. Без ввода-вывода, без Context, без обращений к диску и часам:
 * всё это делают эффекты. Благодаря этому переходы экрана проверяются тестами на JVM.
 */
public interface Reducer<S, A, E> {
    Update<S, E> reduce(S state, A action);
}
