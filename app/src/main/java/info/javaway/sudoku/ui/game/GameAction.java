package info.javaway.sudoku.ui.game;

import info.javaway.sudoku.game.Board;
import info.javaway.sudoku.game.Difficulty;

/** Всё, что может случиться на экране игры: жест человека, ответ фоновой работы или тик часов. */
public abstract class GameAction {

    /**
     * Начать партию выбранного уровня. Приходит с экрана новой игры или с панели итогов —
     * оба места сами показывают, что при этом теряется, поэтому вопроса вдогонку нет.
     */
    public static final class NewGame extends GameAction {
        public final Difficulty level;

        public NewGame(Difficulty level) {
            this.level = level;
        }
    }

    public static final class Generated extends GameAction {
        public final Board board;

        public Generated(Board board) {
            this.board = board;
        }
    }

    public static final class CellTapped extends GameAction {
        public final int cell;

        public CellTapped(int cell) {
            this.cell = cell;
        }
    }

    public static final class DigitTapped extends GameAction {
        public final int digit;

        public DigitTapped(int digit) {
            this.digit = digit;
        }
    }

    public static final class EraseTapped extends GameAction {
    }

    public static final class PencilToggled extends GameAction {
    }

    public static final class UndoTapped extends GameAction {
    }

    public static final class HintTapped extends GameAction {
    }

    public static final class PauseToggled extends GameAction {
    }

    public static final class Ticked extends GameAction {
    }

    /** Итог победы записан в статистику и вернулся, чтобы панель показала рекорд. */
    public static final class WinRecorded extends GameAction {
        public final int best;
        public final boolean record;

        public WinRecorded(int best, boolean record) {
            this.best = best;
            this.record = record;
        }
    }

    /** Настройку кандидатов меняют на другом экране; сюда она приходит при возврате. */
    public static final class CandidatesChanged extends GameAction {
        public final boolean on;

        public CandidatesChanged(boolean on) {
            this.on = on;
        }
    }

    /** «Посмотреть доску» на панели итогов. */
    public static final class PanelDismissed extends GameAction {
    }
}
