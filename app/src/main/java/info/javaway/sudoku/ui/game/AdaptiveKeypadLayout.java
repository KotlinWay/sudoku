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

    private static final int SLOT_COUNT = 10;

    private final int gap;
    private final int minimumCellSize;
    private KeypadLayoutPolicy.Layout layout;

    public AdaptiveKeypadLayout(Context context) {
        this(context, null);
    }

    public AdaptiveKeypadLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AdaptiveKeypadLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        gap = getResources().getDimensionPixelSize(R.dimen.space_xs);
        minimumCellSize = getResources().getDimensionPixelSize(R.dimen.touch_target);
    }

    @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (getChildCount() != SLOT_COUNT) {
            throw new IllegalStateException("Adaptive keypad must contain exactly 10 keys");
        }
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int availableWidth = Math.max(0, width - getPaddingLeft() - getPaddingRight());
        int availableHeight = Math.max(0, height - getPaddingTop() - getPaddingBottom());

        int naturalCellHeight = minimumCellSize;
        int naturalWidthSpec = MeasureSpec.makeMeasureSpec(availableWidth, MeasureSpec.AT_MOST);
        int naturalHeightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        for (int index = 0; index < getChildCount(); index++) {
            View child = getChildAt(index);
            if (child.getVisibility() == GONE) continue;
            child.measure(naturalWidthSpec, naturalHeightSpec);
            naturalCellHeight = Math.max(naturalCellHeight, child.getMeasuredHeight());
        }

        layout = KeypadLayoutPolicy.select(
                availableWidth,
                availableHeight,
                minimumCellSize,
                naturalCellHeight,
                gap
        );
        KeypadLayoutPolicy.Geometry geometry = geometry(
                availableWidth,
                availableHeight,
                getPaddingLeft(),
                getPaddingTop()
        );
        for (int index = 0; index < getChildCount(); index++) {
            View child = getChildAt(index);
            if (child.getVisibility() == GONE) continue;
            KeypadLayoutPolicy.Bounds cell = geometry.cell(index);
            child.measure(
                    MeasureSpec.makeMeasureSpec(cell.width(), MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(cell.height(), MeasureSpec.EXACTLY)
            );
        }

        setMeasuredDimension(
                resolveSize(width, widthMeasureSpec),
                resolveSize(height, heightMeasureSpec)
        );
    }

    @Override protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        KeypadLayoutPolicy.Geometry geometry = geometry(
                Math.max(0, right - left - getPaddingLeft() - getPaddingRight()),
                Math.max(0, bottom - top - getPaddingTop() - getPaddingBottom()),
                getPaddingLeft(),
                getPaddingTop()
        );
        for (int index = 0; index < getChildCount(); index++) {
            View child = getChildAt(index);
            // Индекс ребёнка и есть семантический слот: GONE не сдвигает следующие ID.
            if (child.getVisibility() == GONE) continue;
            KeypadLayoutPolicy.Bounds cell = geometry.cell(index);
            child.layout(
                    cell.left,
                    cell.top,
                    cell.right,
                    cell.bottom
            );
        }
    }

    @Override public void onRtlPropertiesChanged(int layoutDirection) {
        super.onRtlPropertiesChanged(layoutDirection);
        requestLayout();
    }

    private KeypadLayoutPolicy.Geometry geometry(
            int availableWidth,
            int availableHeight,
            int contentLeft,
            int contentTop
    ) {
        return KeypadLayoutPolicy.geometry(
                layout,
                contentLeft,
                contentTop,
                availableWidth,
                availableHeight,
                gap,
                getLayoutDirection() == LAYOUT_DIRECTION_RTL
        );
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
