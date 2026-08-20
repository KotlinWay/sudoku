package info.javaway.sudoku.ui.game;

final class KeypadLayoutPolicy {

    private static final int SLOT_COUNT = 10;

    enum Layout {
        TALL(2, 5),
        COMPACT(5, 2);

        final int columns;
        final int rows;

        Layout(int columns, int rows) {
            this.columns = columns;
            this.rows = rows;
        }
    }

    private KeypadLayoutPolicy() {}

    static Layout select(
            int availableWidth,
            int availableHeight,
            int minimumCellWidth,
            int minimumCellHeight,
            int gap
    ) {
        if (fits(Layout.TALL, availableWidth, availableHeight,
                minimumCellWidth, minimumCellHeight, gap)) {
            return Layout.TALL;
        }
        if (fits(Layout.COMPACT, availableWidth, availableHeight,
                minimumCellWidth, minimumCellHeight, gap)) {
            return Layout.COMPACT;
        }

        int tallMinimumSide = minimumActualSide(
                Layout.TALL,
                availableWidth,
                availableHeight,
                gap
        );
        int compactMinimumSide = minimumActualSide(
                Layout.COMPACT,
                availableWidth,
                availableHeight,
                gap
        );
        return compactMinimumSide > tallMinimumSide ? Layout.COMPACT : Layout.TALL;
    }

    static Geometry geometry(
            Layout layout,
            int contentLeft,
            int contentTop,
            int contentWidth,
            int contentHeight,
            int gap,
            boolean rightToLeft
    ) {
        return new Geometry(
                layout,
                contentLeft,
                contentTop,
                Math.max(0, contentWidth),
                Math.max(0, contentHeight),
                Math.max(0, gap),
                rightToLeft
        );
    }

    private static boolean fits(
            Layout layout,
            int availableWidth,
            int availableHeight,
            int minimumCellWidth,
            int minimumCellHeight,
            int gap
    ) {
        return required(layout.columns, minimumCellWidth, gap) <= availableWidth
                && required(layout.rows, minimumCellHeight, gap) <= availableHeight;
    }

    private static long required(int count, int minimumCellSize, int gap) {
        return (long) count * Math.max(0, minimumCellSize)
                + (long) (count - 1) * Math.max(0, gap);
    }

    private static int minimumActualSide(
            Layout layout,
            int availableWidth,
            int availableHeight,
            int gap
    ) {
        Axis columns = new Axis(availableWidth, layout.columns, gap);
        Axis rows = new Axis(availableHeight, layout.rows, gap);
        return Math.min(columns.minimumCellSize(), rows.minimumCellSize());
    }

    static final class Geometry {
        private final Layout layout;
        private final int contentLeft;
        private final int contentTop;
        private final Axis columns;
        private final Axis rows;
        private final boolean rightToLeft;

        private Geometry(
                Layout layout,
                int contentLeft,
                int contentTop,
                int contentWidth,
                int contentHeight,
                int gap,
                boolean rightToLeft
        ) {
            this.layout = layout;
            this.contentLeft = contentLeft;
            this.contentTop = contentTop;
            columns = new Axis(contentWidth, layout.columns, gap);
            rows = new Axis(contentHeight, layout.rows, gap);
            this.rightToLeft = rightToLeft;
        }

        Bounds cell(int semanticSlot) {
            if (semanticSlot < 0 || semanticSlot >= SLOT_COUNT) {
                throw new IllegalArgumentException("Keypad slot must be from 0 to 9");
            }
            int logicalColumn = layout == Layout.TALL
                    ? semanticSlot / layout.rows
                    : semanticSlot % layout.columns;
            int row = layout == Layout.TALL
                    ? semanticSlot % layout.rows
                    : semanticSlot / layout.columns;
            int physicalColumn = rightToLeft
                    ? layout.columns - 1 - logicalColumn
                    : logicalColumn;
            int left = contentLeft + columns.start(physicalColumn);
            int top = contentTop + rows.start(row);
            return new Bounds(
                    left,
                    top,
                    left + columns.size(physicalColumn),
                    top + rows.size(row)
            );
        }
    }

    static final class Bounds {
        final int left;
        final int top;
        final int right;
        final int bottom;

        private Bounds(int left, int top, int right, int bottom) {
            this.left = left;
            this.top = top;
            this.right = right;
            this.bottom = bottom;
        }

        int width() {
            return right - left;
        }

        int height() {
            return bottom - top;
        }
    }

    private static final class Axis {
        private final int gap;
        private final int baseCellSize;
        private final int remainder;

        private Axis(int availableSize, int count, int requestedGap) {
            int safeSize = Math.max(0, availableSize);
            gap = count > 1
                    ? Math.min(Math.max(0, requestedGap), safeSize / (count - 1))
                    : 0;
            int spaceForCells = safeSize - (count - 1) * gap;
            baseCellSize = spaceForCells / count;
            remainder = spaceForCells % count;
        }

        private int start(int index) {
            return index * (baseCellSize + gap) + Math.min(index, remainder);
        }

        private int size(int index) {
            return baseCellSize + (index < remainder ? 1 : 0);
        }

        private int minimumCellSize() {
            return baseCellSize;
        }
    }
}
