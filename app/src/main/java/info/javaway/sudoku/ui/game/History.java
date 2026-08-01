package info.javaway.sudoku.ui.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import info.javaway.sudoku.game.Move;

/**
 * Сделанное и отменённое. Иммутабельна, как и всё состояние экрана: каждый ход рождает новую
 * историю, а не правит прежнюю.
 *
 * Новый ход стирает отменённое. Иначе «повторить» вернуло бы ход из ветки, от которой игрок
 * уже отказался, и на доске появилась бы цифра, которую он не ставил.
 */
public final class History {

    private static final History EMPTY =
            new History(Collections.<Move>emptyList(), Collections.<Move>emptyList());

    private final List<Move> done;
    private final List<Move> undone;

    private History(List<Move> done, List<Move> undone) {
        this.done = done;
        this.undone = undone;
    }

    public static History empty() {
        return EMPTY;
    }

    public static History of(List<Move> done, List<Move> undone) {
        return new History(Collections.unmodifiableList(new ArrayList<>(done)),
                Collections.unmodifiableList(new ArrayList<>(undone)));
    }

    public boolean canUndo() {
        return !done.isEmpty();
    }

    public boolean canRedo() {
        return !undone.isEmpty();
    }

    /** Ходов сделано хотя бы один — значит партия начата, и стирать её молча нельзя. */
    public boolean isStarted() {
        return canUndo();
    }

    public Move lastDone() {
        return done.get(done.size() - 1);
    }

    public Move lastUndone() {
        return undone.get(undone.size() - 1);
    }

    public List<Move> done() {
        return done;
    }

    public List<Move> undone() {
        return undone;
    }

    public History pushed(Move move) {
        return new History(appended(done, move), Collections.<Move>emptyList());
    }

    public History undone(Move move) {
        return new History(withoutLast(done), appended(undone, move));
    }

    public History redone(Move move) {
        return new History(appended(done, move), withoutLast(undone));
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
