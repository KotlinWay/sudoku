package info.javaway.sudoku.ui.game;

import java.util.ArrayList;
import java.util.List;

import info.javaway.sudoku.game.Board;
import info.javaway.sudoku.game.Difficulty;
import info.javaway.sudoku.game.Move;
import info.javaway.sudoku.game.Rules;
import info.javaway.sudoku.ui.mvi.Reducer;
import info.javaway.sudoku.ui.mvi.Update;

/**
 * Правила экрана игры. Ни часов, ни диска, ни случайности: время приходит тиками, задачу
 * считает фоновая работа, а нужный ей seed выбирает Activity. Поэтому вся игра целиком
 * проверяется тестами на JVM.
 */
public final class GameReducer implements Reducer<GameState, GameAction, GameEffect> {

    @Override
    public Update<GameState, GameEffect> reduce(GameState state, GameAction action) {
        if (action instanceof GameAction.NewGame) {
            GameAction.NewGame game = (GameAction.NewGame) action;
            return generate(state, game.level, game.daily);
        }
        if (action instanceof GameAction.Generated) {
            return Update.state(state.started(((GameAction.Generated) action).board));
        }
        if (action instanceof GameAction.CellTapped) {
            return select(state, ((GameAction.CellTapped) action).cell);
        }
        if (action instanceof GameAction.DigitTapped) {
            return digit(state, ((GameAction.DigitTapped) action).digit);
        }
        if (action instanceof GameAction.EraseTapped) {
            return erase(state);
        }
        if (action instanceof GameAction.PencilToggled) {
            return pencil(state);
        }
        if (action instanceof GameAction.UndoTapped) {
            return undo(state);
        }
        if (action instanceof GameAction.RedoTapped) {
            return redo(state);
        }
        if (action instanceof GameAction.HintTapped) {
            return hint(state);
        }
        if (action instanceof GameAction.PauseToggled) {
            return pause(state);
        }
        if (action instanceof GameAction.Ticked) {
            return Update.state(state.accepts() ? state.ticked() : state);
        }
        if (action instanceof GameAction.WinRecorded) {
            GameAction.WinRecorded recorded = (GameAction.WinRecorded) action;
            return Update.state(state.recorded(recorded.best, recorded.record));
        }
        if (action instanceof GameAction.CandidatesChanged) {
            boolean on = ((GameAction.CandidatesChanged) action).on;
            return Update.state(state.candidates == on ? state : state.candidates(on));
        }
        if (action instanceof GameAction.PanelDismissed) {
            return Update.state(state.panel ? state.withoutPanel() : state);
        }
        return Update.state(state);
    }

    /**
     * Новая партия стирает начатую. Вопроса здесь нет намеренно: экран новой игры показывает
     * потерю до выбора уровня, а панель итогов появляется, когда терять уже нечего. Каталожное
     * правило «деструктивное только с предпросмотром» выполнено предпросмотром, а не
     * подтверждением вдогонку.
     */
    private Update<GameState, GameEffect> generate(GameState state, Difficulty level,
                                                   boolean daily) {
        return Update.of(GameState.generating(level, daily, state.candidates),
                new GameEffect.Generate(level, daily));
    }

    private Update<GameState, GameEffect> select(GameState state, int cell) {
        if (!state.accepts() || state.selected == cell) return Update.state(state);
        return Update.of(state.selecting(cell),
                new GameEffect.Respond(GameEffect.Feedback.SELECT));
    }

    /**
     * Цифра с клавиатуры. В режиме карандаша она ложится пометкой, иначе — ответом, и тогда
     * сверяется с решением: ошибка стоит жизни, а последняя жизнь — партии.
     *
     * Неверная цифра остаётся на доске, а не отскакивает: игрок должен видеть, что именно
     * он вписал, и стереть это сам.
     */
    private Update<GameState, GameEffect> digit(GameState state, int digit) {
        if (!editable(state)) return Update.state(state);
        int cell = state.selected;

        if (state.pencil) {
            return changed(state, Rules.note(state.board, cell, digit), cell,
                    GameEffect.Feedback.NOTE);
        }

        Board next = Rules.place(state.board, cell, digit);
        Move move = Move.between(state.board, next);
        if (move == null) return Update.state(state);

        GameState moved = state.moved(next, move, cell);
        if (digit == state.board.answer(cell)) {
            return finishIfSolved(moved,
                    new GameEffect.Animate(GameEffect.Animation.POP, cell),
                    new GameEffect.Respond(GameEffect.Feedback.PLACE));
        }

        GameState mistaken = moved.mistaken();
        List<GameEffect> effects = effects(
                new GameEffect.Animate(GameEffect.Animation.SHAKE, cell),
                new GameEffect.Respond(GameEffect.Feedback.WRONG));
        if (mistaken.mistakes >= mistaken.mistakeLimit()) {
            effects.add(new GameEffect.RecordLoss());
            return update(mistaken.finished(GameState.Phase.LOST), effects);
        }
        return update(mistaken, effects);
    }

    private Update<GameState, GameEffect> erase(GameState state) {
        if (!editable(state)) return Update.state(state);
        return changed(state, Rules.erase(state.board, state.selected), state.selected,
                GameEffect.Feedback.ERASE);
    }

    private Update<GameState, GameEffect> pencil(GameState state) {
        if (!state.accepts() || state.candidates) return Update.state(state);
        return Update.of(state.pencil(!state.pencil),
                new GameEffect.Respond(GameEffect.Feedback.SELECT));
    }

    private Update<GameState, GameEffect> undo(GameState state) {
        if (!state.accepts() || !state.history.canUndo()) return Update.state(state);
        Move move = state.history.lastDone();
        return Update.of(state.undone(move.undo(state.board), move),
                new GameEffect.Respond(GameEffect.Feedback.ERASE));
    }

    private Update<GameState, GameEffect> redo(GameState state) {
        if (!state.accepts() || !state.history.canRedo()) return Update.state(state);
        Move move = state.history.lastUndone();
        return Update.of(state.redone(move.redo(state.board), move),
                new GameEffect.Respond(GameEffect.Feedback.ERASE));
    }

    /**
     * Подсказка открывает верную цифру. Если клетка выбрана — именно её: игрок показал, где
     * застрял. Иначе берётся пустая клетка по счётчику секунд и потраченных подсказок:
     * выбор произвольный, но воспроизводимый, а значит проверяемый тестом.
     *
     * Отмена хода вернёт цифру обратно, но не саму подсказку: потраченное — потрачено,
     * иначе три подсказки превращаются в бесконечные.
     */
    private Update<GameState, GameEffect> hint(GameState state) {
        if (!state.accepts()) return Update.state(state);
        if (state.hintsLeft <= 0) {
            return Update.of(state, new GameEffect.Say(GameEffect.Message.NO_HINTS));
        }
        int cell = Rules.hintCell(state.board, state.selected, state.seconds + state.hintsUsed);
        if (cell < 0) {
            return Update.of(state, new GameEffect.Say(GameEffect.Message.BOARD_FULL));
        }

        Board next = Rules.reveal(state.board, cell);
        Move move = Move.between(state.board, next);
        if (move == null) return Update.state(state);

        return finishIfSolved(state.moved(next, move, cell).hinted(),
                new GameEffect.Animate(GameEffect.Animation.REVEAL, cell),
                new GameEffect.Respond(GameEffect.Feedback.HINT));
    }

    private Update<GameState, GameEffect> pause(GameState state) {
        if (state.phase == GameState.Phase.PLAYING) return Update.state(state.paused(true));
        if (state.phase == GameState.Phase.PAUSED) return Update.state(state.paused(false));
        return Update.state(state);
    }

    /**
     * Доска решена — партия закончена, и к откликам хода добавляются праздничные.
     * Отклик самого хода при этом не пропадает: последняя цифра встаёт так же, как все прежние.
     */
    private Update<GameState, GameEffect> finishIfSolved(GameState state, GameEffect... moveEffects) {
        List<GameEffect> effects = effects(moveEffects);
        if (!state.board.isSolved()) return update(state, effects);
        effects.add(new GameEffect.Respond(GameEffect.Feedback.WIN));
        effects.add(new GameEffect.Celebrate());
        effects.add(new GameEffect.RecordWin(state.level, state.daily, state.seconds,
                state.hintsUsed));
        return update(state.finished(GameState.Phase.WON), effects);
    }

    /** Ход, который мог ничего не изменить: та же пометка второй раз, стирание пустой клетки. */
    private Update<GameState, GameEffect> changed(GameState state, Board next, int cell,
                                                  GameEffect.Feedback feedback) {
        Move move = Move.between(state.board, next);
        if (move == null) return Update.state(state);
        return Update.of(state.moved(next, move, cell), new GameEffect.Respond(feedback));
    }

    /** Писать можно только в выбранную клетку, и только если её не открыл генератор. */
    private boolean editable(GameState state) {
        return state.accepts() && state.selected >= 0 && !state.board.isGiven(state.selected);
    }

    private static List<GameEffect> effects(GameEffect... effects) {
        List<GameEffect> list = new ArrayList<>(effects.length + 3);
        for (GameEffect effect : effects) {
            list.add(effect);
        }
        return list;
    }

    private static Update<GameState, GameEffect> update(GameState state, List<GameEffect> effects) {
        return Update.of(state, effects.toArray(new GameEffect[0]));
    }
}
