package info.javaway.sudoku.ui.game;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import info.javaway.sudoku.R;

/**
 * Раскладывает клавиши двумя столбцами, пока для пяти рядов хватает высоты. При крупном
 * системном шрифте перестраивает те же клавиши в два ряда, не отнимая место у доски.
 */
public final class AdaptiveKeypadLayout extends ViewGroup {

    private static final int TALL_COLUMNS = 2;
    private static final int TALL_ROWS = 5;
    private static final int COMPACT_COLUMNS = 5;
    private static final int COMPACT_ROWS = 2;

    private final int gap;
    private final int minimumCellHeight;
    private boolean tallLayout;

    public AdaptiveKeypadLayout(Context context) {
        this(context, null);
    }

    public AdaptiveKeypadLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AdaptiveKeypadLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        gap = getResources().getDimensionPixelSize(R.dimen.space_xs);
        minimumCellHeight = getResources().getDimensionPixelSize(R.dimen.touch_target);
    }

    @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int availableWidth = Math.max(0, width - getPaddingLeft() - getPaddingRight());
        int availableHeight = Math.max(0, height - getPaddingTop() - getPaddingBottom());

        int naturalCellHeight = minimumCellHeight;
        int naturalWidthSpec = MeasureSpec.makeMeasureSpec(availableWidth, MeasureSpec.AT_MOST);
        int naturalHeightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        for (int index = 0; index < getChildCount(); index++) {
            View child = getChildAt(index);
            if (child.getVisibility() == GONE) continue;
            child.measure(naturalWidthSpec, naturalHeightSpec);
            naturalCellHeight = Math.max(naturalCellHeight, child.getMeasuredHeight());
        }

        tallLayout = KeypadLayoutPolicy.usesTallLayout(
                availableHeight,
                naturalCellHeight,
                gap
        );
        int columns = tallLayout ? TALL_COLUMNS : COMPACT_COLUMNS;
        int rows = tallLayout ? TALL_ROWS : COMPACT_ROWS;
        int cellWidth = cellSize(availableWidth, columns);
        int cellHeight = cellSize(availableHeight, rows);
        int cellWidthSpec = MeasureSpec.makeMeasureSpec(cellWidth, MeasureSpec.EXACTLY);
        int cellHeightSpec = MeasureSpec.makeMeasureSpec(cellHeight, MeasureSpec.EXACTLY);
        for (int index = 0; index < getChildCount(); index++) {
            View child = getChildAt(index);
            if (child.getVisibility() != GONE) child.measure(cellWidthSpec, cellHeightSpec);
        }

        setMeasuredDimension(
                resolveSize(width, widthMeasureSpec),
                resolveSize(height, heightMeasureSpec)
        );
    }

    @Override protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int columns = tallLayout ? TALL_COLUMNS : COMPACT_COLUMNS;
        for (int index = 0; index < getChildCount(); index++) {
            View child = getChildAt(index);
            if (child.getVisibility() == GONE) continue;
            int column = tallLayout ? index / TALL_ROWS : index % COMPACT_COLUMNS;
            int row = tallLayout ? index % TALL_ROWS : index / COMPACT_COLUMNS;
            int childLeft = getPaddingLeft() + column * (child.getMeasuredWidth() + gap);
            int childTop = getPaddingTop() + row * (child.getMeasuredHeight() + gap);
            child.layout(
                    childLeft,
                    childTop,
                    childLeft + child.getMeasuredWidth(),
                    childTop + child.getMeasuredHeight()
            );
        }
    }

    private int cellSize(int available, int count) {
        return Math.max(0, (available - (count - 1) * gap) / count);
    }

    @Override protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    }

    @Override public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @Override protected LayoutParams generateLayoutParams(LayoutParams source) {
        return new LayoutParams(source);
    }

    @Override protected boolean checkLayoutParams(LayoutParams params) {
        return params != null;
    }
}
