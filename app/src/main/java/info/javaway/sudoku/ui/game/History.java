package info.javaway.sudoku.ui.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import info.javaway.sudoku.game.Move;

/**
 * Сделанные ходы. Иммутабельна, как и всё состояние экрана: каждый ход рождает новую
 * историю, а не правит прежнюю.
 *
 * Стек один. Отменённый ход никуда не откладывается: «вернуть» из игры убрано, а держать
 * ветку, войти в которую нечем, значит таскать по состоянию и писать на диск список,
 * который никто не прочтёт.
 */
public final class History {

    private static final History EMPTY = new History(Collections.<Move>emptyList());

    private final List<Move> done;

    private History(List<Move> done) {
        this.done = done;
    }

    public static History empty() {
        return EMPTY;
    }

    public static History of(List<Move> done) {
        return new History(Collections.unmodifiableList(new ArrayList<>(done)));
    }

    public boolean canUndo() {
        return !done.isEmpty();
    }

    /** Ходов сделано хотя бы один — значит партия начата, и стирать её молча нельзя. */
    public boolean isStarted() {
        return canUndo();
    }

    public Move lastDone() {
        return done.get(done.size() - 1);
    }

    public List<Move> done() {
        return done;
    }

    public History pushed(Move move) {
        return new History(appended(done, move));
    }

    /** Верхний ход снят. Куда он делся, история не помнит: возвращать его больше некуда. */
    public History popped() {
        return new History(withoutLast(done));
    }

    private static List<Move> appended(List<Move> moves, Move move) {
        List<Move> next = new ArrayList<>(moves.size() + 1);
        next.addAll(moves);
        next.add(move);
        return Collections.unmodifiableList(next);
    }

    private static List<Move> withoutLast(List<Move> moves) {
        return Collections.unmodifiableList(new ArrayList<>(moves.subList(0, moves.size() - 1)));
    }
}
