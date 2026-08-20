package info.javaway.sudoku.ui.game;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityNodeProvider;

import info.javaway.sudoku.R;
import info.javaway.sudoku.game.Board;
import info.javaway.sudoku.game.Cells;
import info.javaway.sudoku.game.Notes;

/**
 * Доска. Рисуется целиком по состоянию: 81 клетка отдельными View означала бы 81 набор
 * отступов, фонов и слушателей и всё равно не дала бы ни толстых линий между квадратами,
 * ни пометок сеткой 3×3 внутри клетки.
 *
 * Своя отрисовка стоит одного: холст для TalkBack пуст, поэтому доска отдаёт виртуальное
 * дерево из 81 узла — без него игра для незрячего просто не существует.
 */
public class BoardView extends View {

    public interface OnCellTap {
        void onCellTap(int cell);
    }

    private static final float DIGIT_HEIGHT = 0.62f;
    private static final float NOTE_HEIGHT = 0.26f;
    private static final float POP_FROM = 0.55f;
    private static final float SHAKE_SHIFT = 0.09f;
    private static final int SHAKE_SWINGS = 3;

    private final Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint line = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint digit = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint note = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF cellRect = new RectF();
    private final Rect bounds = new Rect();
    private final int[] onScreen = new int[2];

    private final int colorCell;
    private final int colorPeer;
    private final int colorSame;
    private final int colorSelected;
    private final int colorWrong;
    private final int colorLine;
    private final int colorFrame;
    private final int colorAccent;
    private final int colorGiven;
    private final int colorPlayer;
    private final int colorNote;
    private final int colorDigitWrong;

    private final float thinWidth;
    private final float frameWidth;
    private final float ringWidth;

    private GameState state;
    private OnCellTap listener;

    private GameEffect.Animation animation;
    private int animatedCell = -1;
    private long animationStart;

    /** Клетка под пальцем при исследовании касанием. -1, когда палец вне доски. */
    private int hovered = AccessibilityNodeProvider.HOST_VIEW_ID;
    private int focused = AccessibilityNodeProvider.HOST_VIEW_ID;

    private final Provider provider = new Provider();

    public BoardView(Context context, AttributeSet attrs) {
        super(context, attrs);

        colorCell = color(R.color.cell);
        colorPeer = color(R.color.cell_peer);
        colorSame = color(R.color.cell_same);
        colorSelected = color(R.color.cell_selected);
        colorWrong = color(R.color.cell_wrong);
        colorLine = color(R.color.board_line);
        colorFrame = color(R.color.board_frame);
        colorAccent = color(R.color.accent);
        colorGiven = color(R.color.digit_given);
        colorPlayer = color(R.color.digit_player);
        colorNote = color(R.color.digit_note);
        colorDigitWrong = color(R.color.digit_wrong);

        thinWidth = size(R.dimen.board_line);
        frameWidth = size(R.dimen.board_frame);
        ringWidth = size(R.dimen.board_ring);

        line.setStyle(Paint.Style.STROKE);
        digit.setTextAlign(Paint.Align.CENTER);
        note.setTextAlign(Paint.Align.CENTER);
        note.setTypeface(Typeface.DEFAULT);

        setContentDescription(context.getString(R.string.board_desc));
        setFocusable(true);
    }

    public void setOnCellTap(OnCellTap listener) {
        this.listener = listener;
    }

    /** Единственный вход: доска не знает других способов узнать, что показывать. */
    public void show(GameState state) {
        this.state = state;
        invalidate();
    }

    /**
     * Разовая анимация. Состоянием она не является: после поворота экрана цифра не должна
     * заново подпрыгивать, а ошибка — заново вздрагивать.
     */
    public void play(GameEffect.Animation animation, int cell) {
        this.animation = animation;
        this.animatedCell = cell;
        this.animationStart = SystemClock.uptimeMillis();
        invalidate();
    }

    /** Сколько длится анимация. Число здесь, а не в эффекте: это свойство показа, а не игры. */
    private int duration() {
        switch (animation) {
            case POP: return 180;
            case SHAKE: return 300;
            default: return 420;
        }
    }

    /**
     * Доска квадратная: берём меньшую из предложенных сторон. Высота без ограничения
     * (в прокручиваемой разметке) означает «сколько дадите по ширине».
     */
    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        int width = MeasureSpec.getSize(widthSpec);
        int height = MeasureSpec.getMode(heightSpec) == MeasureSpec.UNSPECIFIED
                ? width : MeasureSpec.getSize(heightSpec);
        int side = Math.min(width, height);
        setMeasuredDimension(side, side);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (state == null) return;
        float step = getWidth() / (float) Cells.SIDE;
        float shift = shake(step);

        drawCells(canvas, step, shift);
        drawLines(canvas, step);
        drawSelection(canvas, step);

        if (animation != null && progress() >= 1f) {
            animation = null;
            animatedCell = -1;
        }
        if (animation != null) postInvalidateOnAnimation();
    }

    /**
     * На паузе доска пустеет. Иначе пауза становится способом разглядывать задачу с
     * остановленными часами — то есть кнопкой «выключить таймер», а не «отойти».
     */
    private void drawCells(Canvas canvas, float step, float shift) {
        Board board = state.board;
        boolean hidden = state.phase == GameState.Phase.PAUSED;
        int selectedDigit = !hidden && state.selected >= 0 ? board.value(state.selected) : 0;

        for (int cell = 0; cell < Cells.COUNT; cell++) {
            float left = Cells.column(cell) * step;
            float top = Cells.row(cell) * step;
            cellRect.set(left, top, left + step, top + step);

            fill.setColor(hidden ? colorCell : background(cell, selectedDigit));
            canvas.drawRect(cellRect, fill);
            if (hidden) continue;

            int value = board.value(cell);
            if (value != 0) {
                drawDigit(canvas, cell, value, cell == animatedCell ? shift : 0);
            } else {
                drawNotes(canvas, cell, step);
            }
        }
    }

    /**
     * Подсветка тремя ступенями одного цвета: выбранная клетка, клетки с той же цифрой,
     * соседи по строке, столбцу и квадрату. Ошибка перебивает соседство, но не выбор:
     * выбранная клетка должна оставаться видимой всегда, а про ошибку в ней скажет цвет цифры.
     */
    private int background(int cell, int selectedDigit) {
        if (cell == state.selected) return colorSelected;
        if (isWrong(cell)) return colorWrong;
        if (selectedDigit != 0 && state.board.value(cell) == selectedDigit) return colorSame;
        if (state.selected >= 0 && Cells.sees(state.selected, cell)) return colorPeer;
        return colorCell;
    }

    /**
     * Клетка отмечена ошибкой, если цифра в ней повторяется у соседей. Про решение доска
     * молчит намеренно: цифру, верную по правилам, но не ту, игра уже наказала жизнью
     * в момент ввода — красить её и дальше значило бы решать задачу за игрока.
     */
    private boolean isWrong(int cell) {
        return state.board.isDuplicate(cell);
    }

    /**
     * Дрожание при ошибке сдвигает только цифру, а не всю клетку: клетка, съехавшая
     * с места, обнажила бы полосу пустого холста под сеткой.
     */
    private void drawDigit(Canvas canvas, int cell, int value, float shift) {
        boolean given = state.board.isGiven(cell);
        digit.setColor(isWrong(cell) ? colorDigitWrong : given ? colorGiven : colorPlayer);
        digit.setTypeface(given ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
        digit.setTextSize(cellRect.height() * DIGIT_HEIGHT * scale(cell));

        // Базовая линия ставится по реальной высоте цифр, а не по середине клетки:
        // у шрифта над цифрами есть запас под диакритику, и без поправки столбец
        // цифр стоит заметно выше центра.
        Paint.FontMetrics metrics = digit.getFontMetrics();
        float baseline = cellRect.centerY() - (metrics.ascent + metrics.descent) / 2f;
        canvas.drawText(String.valueOf(value), cellRect.centerX() + shift, baseline, digit);
    }

    /**
     * Пометки сеткой 3×3 внутри клетки: каждая цифра всегда на своём месте, поэтому
     * их читают не перечитывая — глаз запоминает расположение, а не порядок.
     */
    private void drawNotes(Canvas canvas, int cell, float step) {
        int marks = state.candidates ? state.board.candidates(cell) : state.board.notes(cell);
        if (Notes.isEmpty(marks)) return;

        note.setColor(colorNote);
        note.setTextSize(step * NOTE_HEIGHT);
        Paint.FontMetrics metrics = note.getFontMetrics();
        float third = step / Cells.BOX;

        for (int value = 1; value <= Cells.SIDE; value++) {
            if (!Notes.has(marks, value)) continue;
            int index = value - 1;
            float x = cellRect.left + (index % Cells.BOX + 0.5f) * third;
            float y = cellRect.top + (index / Cells.BOX + 0.5f) * third
                    - (metrics.ascent + metrics.descent) / 2f;
            canvas.drawText(String.valueOf(value), x, y, note);
        }
    }

    /** Тонкие линии между клетками и толстые между квадратами и по краю доски. */
    private void drawLines(Canvas canvas, float step) {
        for (int index = 0; index <= Cells.SIDE; index++) {
            boolean frame = index % Cells.BOX == 0;
            line.setColor(frame ? colorFrame : colorLine);
            line.setStrokeWidth(frame ? frameWidth : thinWidth);

            // Крайние линии сдвинуты внутрь на половину толщины: иначе половина рамки
            // уходит за границу View и доска выглядит обрезанной с двух сторон.
            float at = position(index, step, frame);
            canvas.drawLine(at, 0, at, getHeight(), line);
            canvas.drawLine(0, at, getWidth(), at, line);
        }
    }

    private float position(int index, float step, boolean frame) {
        float at = index * step;
        if (!frame) return at;
        float half = frameWidth / 2f;
        if (index == 0) return half;
        if (index == Cells.SIDE) return at - half;
        return at;
    }

    /** Выбор показан не только заливкой, но и кольцом: так его видно и мимо цвета. */
    private void drawSelection(Canvas canvas, float step) {
        if (state.selected < 0 || state.phase == GameState.Phase.PAUSED) return;
        line.setColor(colorAccent);
        line.setStrokeWidth(ringWidth);
        float inset = ringWidth / 2f;
        float left = Cells.column(state.selected) * step + inset;
        float top = Cells.row(state.selected) * step + inset;
        cellRect.set(left, top, left + step - ringWidth, top + step - ringWidth);
        canvas.drawRect(cellRect, line);
    }

    /* ── Анимации ─────────────────────────────────────────────────────────── */

    private float progress() {
        if (animation == null) return 1f;
        float passed = SystemClock.uptimeMillis() - animationStart;
        return Math.min(1f, passed / duration());
    }

    private float scale(int cell) {
        if (cell != animatedCell || animation == null
                || animation == GameEffect.Animation.SHAKE) {
            return 1f;
        }
        return POP_FROM + (1f - POP_FROM) * progress();
    }

    /** Затухающие качания вокруг центра: к концу анимации цифра всегда возвращается на место. */
    private float shake(float step) {
        if (animation != GameEffect.Animation.SHAKE) return 0f;
        double swing = Math.sin(progress() * SHAKE_SWINGS * 2 * Math.PI);
        return (float) (swing * step * SHAKE_SHIFT * (1f - progress()));
    }

    /* ── Ввод ─────────────────────────────────────────────────────────────── */

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() != MotionEvent.ACTION_UP) {
            return event.getAction() == MotionEvent.ACTION_DOWN;
        }
        int cell = cellAt(event.getX(), event.getY());
        if (cell >= 0) tap(cell);
        return true;
    }

    private void tap(int cell) {
        if (listener != null) listener.onCellTap(cell);
    }

    /** @return индекс клетки под точкой или -1, если точка вне доски */
    private int cellAt(float x, float y) {
        if (x < 0 || y < 0 || x >= getWidth() || y >= getHeight()) return -1;
        float step = getWidth() / (float) Cells.SIDE;
        int column = Math.min(Cells.SIDE - 1, (int) (x / step));
        int row = Math.min(Cells.SIDE - 1, (int) (y / step));
        return Cells.at(row, column);
    }

    /* ── Доступность ──────────────────────────────────────────────────────── */

    @Override
    public AccessibilityNodeProvider getAccessibilityNodeProvider() {
        return provider;
    }

    /**
     * Исследование касанием. Пока палец скользит по доске, TalkBack должен объявлять клетки —
     * а для этого ему нужны события входа и выхода по каждой из 81 клетки, которых у холста
     * без виртуального дерева нет.
     */
    @Override
    public boolean dispatchHoverEvent(MotionEvent event) {
        AccessibilityManager manager = (AccessibilityManager)
                getContext().getSystemService(Context.ACCESSIBILITY_SERVICE);
        if (!manager.isEnabled() || !manager.isTouchExplorationEnabled()) {
            hovered = AccessibilityNodeProvider.HOST_VIEW_ID;
            return super.dispatchHoverEvent(event);
        }
        int cell = event.getAction() == MotionEvent.ACTION_HOVER_EXIT
                ? AccessibilityNodeProvider.HOST_VIEW_ID
                : cellAt(event.getX(), event.getY());
        if (cell != hovered) {
            send(hovered, AccessibilityEvent.TYPE_VIEW_HOVER_EXIT);
            hovered = cell;
            send(hovered, AccessibilityEvent.TYPE_VIEW_HOVER_ENTER);
        }
        return cell >= 0 || super.dispatchHoverEvent(event);
    }

    private void send(int cell, int type) {
        if (cell < 0 || getParent() == null) return;
        AccessibilityEvent event = AccessibilityEvent.obtain(type);
        event.setPackageName(getContext().getPackageName());
        event.setClassName(BoardView.class.getName());
        event.setSource(this, cell);
        getParent().requestSendAccessibilityEvent(this, event);
    }

    /**
     * Что TalkBack скажет про клетку. Координаты называются от единицы: «строка 1» понятнее,
     * чем «строка 0», а внутри приложения клетка всё равно живёт одним индексом.
     */
    private CharSequence describe(int cell) {
        // На паузе доска скрыта и от глаз, и от голоса: иначе пауза обошла бы себя же.
        if (state == null || state.phase == GameState.Phase.PAUSED) return "";
        int row = Cells.row(cell) + 1;
        int column = Cells.column(cell) + 1;
        int value = state.board.value(cell);

        if (value != 0) {
            if (isWrong(cell)) return getContext().getString(R.string.cell_wrong, row, column, value);
            return getContext().getString(
                    state.board.isGiven(cell) ? R.string.cell_given : R.string.cell_value,
                    row, column, value);
        }

        int marks = state.candidates ? state.board.candidates(cell) : state.board.notes(cell);
        if (Notes.isEmpty(marks)) {
            return getContext().getString(R.string.cell_empty, row, column);
        }
        return getContext().getString(R.string.cell_notes, row, column, list(marks));
    }

    private String list(int marks) {
        StringBuilder text = new StringBuilder();
        String separator = getContext().getString(R.string.notes_separator);
        for (int value = 1; value <= Cells.SIDE; value++) {
            if (!Notes.has(marks, value)) continue;
            if (text.length() > 0) text.append(separator);
            text.append(value);
        }
        return text.toString();
    }

    /** Виртуальное дерево доски: узел-хозяин и 81 клетка-ребёнок. */
    private final class Provider extends AccessibilityNodeProvider {

        @Override
        public AccessibilityNodeInfo createAccessibilityNodeInfo(int cell) {
            if (cell == HOST_VIEW_ID) return host();
            if (cell < 0 || cell >= Cells.COUNT) return null;

            AccessibilityNodeInfo node = AccessibilityNodeInfo.obtain(BoardView.this, cell);
            node.setPackageName(getContext().getPackageName());
            node.setClassName(BoardView.class.getName());
            node.setParent(BoardView.this);
            node.setContentDescription(describe(cell));
            node.setBoundsInParent(rectOf(cell));
            node.setBoundsInScreen(screenRectOf(cell));
            node.setVisibleToUser(true);
            node.setEnabled(state != null && state.accepts());
            node.setClickable(true);
            node.setSelected(state != null && state.selected == cell);
            node.addAction(AccessibilityNodeInfo.ACTION_CLICK);
            node.addAction(focused == cell
                    ? AccessibilityNodeInfo.ACTION_CLEAR_ACCESSIBILITY_FOCUS
                    : AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS);
            node.setAccessibilityFocused(focused == cell);
            return node;
        }

        private AccessibilityNodeInfo host() {
            AccessibilityNodeInfo node = AccessibilityNodeInfo.obtain(BoardView.this);
            onInitializeAccessibilityNodeInfo(node);
            for (int cell = 0; cell < Cells.COUNT; cell++) {
                node.addChild(BoardView.this, cell);
            }
            return node;
        }

        @Override
        public boolean performAction(int cell, int action, Bundle arguments) {
            if (cell == HOST_VIEW_ID) return performAccessibilityAction(action, arguments);
            if (cell < 0 || cell >= Cells.COUNT) return false;

            if (action == AccessibilityNodeInfo.ACTION_CLICK) {
                tap(cell);
                send(cell, AccessibilityEvent.TYPE_VIEW_CLICKED);
                return true;
            }
            if (action == AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS) {
                focused = cell;
                send(cell, AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED);
                return true;
            }
            if (action == AccessibilityNodeInfo.ACTION_CLEAR_ACCESSIBILITY_FOCUS
                    && focused == cell) {
                focused = HOST_VIEW_ID;
                send(cell, AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUS_CLEARED);
                return true;
            }
            return false;
        }
    }

    private Rect rectOf(int cell) {
        int step = getWidth() / Cells.SIDE;
        int left = Cells.column(cell) * step;
        int top = Cells.row(cell) * step;
        return new Rect(left, top, left + step, top + step);
    }

    private Rect screenRectOf(int cell) {
        getLocationOnScreen(onScreen);
        bounds.set(rectOf(cell));
        bounds.offset(onScreen[0], onScreen[1]);
        return new Rect(bounds);
    }

    private int color(int id) {
        return getResources().getColor(id, getContext().getTheme());
    }

    private float size(int id) {
        return getResources().getDimension(id);
    }
}
