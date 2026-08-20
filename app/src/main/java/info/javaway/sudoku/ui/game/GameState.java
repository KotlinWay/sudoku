package info.javaway.sudoku.ui.game;

import info.javaway.sudoku.game.Board;
import info.javaway.sudoku.game.Difficulty;
import info.javaway.sudoku.game.GameMode;
import info.javaway.sudoku.game.Move;
import info.javaway.sudoku.stats.Stats;

/**
 * Всё, что видно на экране игры. Иммутабельно: переход — это новое состояние, а не правка
 * старого, поэтому доска никогда не показывается наполовину обновлённой.
 *
 * Полей много, и перечислять их в каждом переходе значило бы двенадцать вызовов конструктора
 * на четырнадцать одинаковых аргументов — в таком месте перепутанные местами два `boolean`
 * компилируются молча. Поэтому копия делается через {@link Draft}: меняется то, что назвали,
 * остальное переносится само.
 */
public final class GameState {

    public enum Phase {
        /** Генератор считает задачу. Доска на экране пустая, ввод не принимается. */
        GENERATING,
        PLAYING,
        PAUSED,
        WON,
        LOST
    }

    public static final int MAX_HINTS = 3;

    public final Phase phase;
    public final Difficulty level;
    public final GameMode mode;
    public final Board board;
    public final History history;

    /** Выбранная клетка или -1. */
    public final int selected;

    /** Режим карандаша: цифра с клавиатуры ложится пометкой, а не ответом. */
    public final boolean pencil;

    /** Показывать ли возможные цифры в пустых клетках. Переключается в настройках. */
    public final boolean candidates;

    public final int mistakes;
    public final int hintsLeft;
    public final int hintsUsed;
    public final int seconds;

    /** Панель поверх доски: пауза, победа, поражение. */
    public final boolean panel;

    /** Лучшее время на этом уровне и признак рекорда — только для панели победы. */
    public final int best;
    public final boolean record;

    private GameState(Draft draft) {
        this.phase = draft.phase;
        this.level = draft.level;
        this.mode = draft.mode;
        this.board = draft.board;
        this.history = draft.history;
        this.selected = draft.selected;
        this.pencil = draft.pencil;
        this.candidates = draft.candidates;
        this.mistakes = draft.mistakes;
        this.hintsLeft = draft.hintsLeft;
        this.hintsUsed = draft.hintsUsed;
        this.seconds = draft.seconds;
        this.panel = draft.panel;
        this.best = draft.best;
        this.record = draft.record;
    }

    /** Состояние на время работы генератора: уровень уже выбран, доски ещё нет. */
    public static GameState generating(Difficulty level, GameMode mode, boolean candidates) {
        Draft draft = new Draft();
        draft.phase = Phase.GENERATING;
        draft.level = level;
        draft.mode = mode;
        draft.board = Board.blank();
        draft.history = History.empty();
        draft.selected = -1;
        draft.pencil = false;
        draft.candidates = candidates;
        draft.mistakes = 0;
        draft.hintsLeft = MAX_HINTS;
        draft.hintsUsed = 0;
        draft.seconds = 0;
        draft.panel = false;
        draft.best = Stats.NO_TIME;
        draft.record = false;
        return new GameState(draft);
    }

    public int mistakeLimit() {
        return level.mistakeLimit;
    }

    public boolean isOver() {
        return phase == Phase.WON || phase == Phase.LOST;
    }

    /** Ввод принимается только в игре: на паузе, на итогах и во время генерации — нет. */
    public boolean accepts() {
        return phase == Phase.PLAYING;
    }

    /* ── Переходы ─────────────────────────────────────────────────────────── */

    /** Генератор отдал задачу. Часы идут с этой секунды, а не с нажатия на уровень. */
    public GameState started(Board board) {
        Draft draft = edit();
        draft.phase = Phase.PLAYING;
        draft.board = board;
        return new GameState(draft);
    }

    public GameState selecting(int cell) {
        Draft draft = edit();
        draft.selected = cell;
        return new GameState(draft);
    }

    /** Ход изменил доску. Выделение переезжает на клетку хода: следующая цифра пойдёт туда же. */
    public GameState moved(Board board, Move move, int cell) {
        Draft draft = edit();
        draft.board = board;
        draft.history = history.pushed(move);
        draft.selected = cell;
        return new GameState(draft);
    }

    /** Ход отменён. Выделение встаёт на клетку хода: человек смотрит именно туда. */
    public GameState undone(Board board, Move move) {
        Draft draft = edit();
        draft.board = board;
        draft.history = history.popped();
        draft.selected = move.cell();
        return new GameState(draft);
    }

    public GameState mistaken() {
        Draft draft = edit();
        draft.mistakes = mistakes + 1;
        return new GameState(draft);
    }

    public GameState hinted() {
        Draft draft = edit();
        draft.hintsLeft = hintsLeft - 1;
        draft.hintsUsed = hintsUsed + 1;
        return new GameState(draft);
    }

    public GameState pencil(boolean on) {
        Draft draft = edit();
        draft.pencil = on;
        return new GameState(draft);
    }

    /**
     * Автоподсказка кандидатов и карандаш — взаимоисключающие: когда цифры расставляет
     * программа, писать поверх них свои нечего, и карандаш выключается. Кнопки карандаша
     * сейчас нет ни в баре, ни в полосе, но правило живёт здесь, а не в ней.
     */
    public GameState candidates(boolean on) {
        Draft draft = edit();
        draft.candidates = on;
        draft.pencil = pencil && !on;
        return new GameState(draft);
    }

    public GameState ticked() {
        Draft draft = edit();
        draft.seconds = seconds + 1;
        return new GameState(draft);
    }

    public GameState paused(boolean paused) {
        Draft draft = edit();
        draft.phase = paused ? Phase.PAUSED : Phase.PLAYING;
        draft.panel = paused;
        return new GameState(draft);
    }

    public GameState finished(Phase phase) {
        Draft draft = edit();
        draft.phase = phase;
        draft.panel = true;
        return new GameState(draft);
    }

    /** Итог записан: теперь панели победы есть что показать про рекорд. */
    public GameState recorded(int best, boolean record) {
        Draft draft = edit();
        draft.best = best;
        draft.record = record;
        return new GameState(draft);
    }

    /** «Посмотреть доску»: партия кончена, но панель убрана. */
    public GameState withoutPanel() {
        Draft draft = edit();
        draft.panel = false;
        return new GameState(draft);
    }

    private Draft edit() {
        Draft draft = new Draft();
        draft.phase = phase;
        draft.level = level;
        draft.mode = mode;
        draft.board = board;
        draft.history = history;
        draft.selected = selected;
        draft.pencil = pencil;
        draft.candidates = candidates;
        draft.mistakes = mistakes;
        draft.hintsLeft = hintsLeft;
        draft.hintsUsed = hintsUsed;
        draft.seconds = seconds;
        draft.panel = panel;
        draft.best = best;
        draft.record = record;
        return draft;
    }

    /** Изменяемая копия состояния, живущая ровно один переход. Наружу не выходит. */
    static final class Draft {
        Phase phase;
        Difficulty level;
        GameMode mode;
        Board board;
        History history;
        int selected;
        boolean pencil;
        boolean candidates;
        int mistakes;
        int hintsLeft;
        int hintsUsed;
        int seconds;
        boolean panel;
        int best;
        boolean record;

        GameState build() {
            return new GameState(this);
        }
    }

    /** Сборка состояния из сохранённой партии — единственный вход мимо {@link #generating}. */
    static Draft draft() {
        return new Draft();
    }
}
