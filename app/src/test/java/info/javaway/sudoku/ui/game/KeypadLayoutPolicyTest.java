package info.javaway.sudoku.ui.game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class KeypadLayoutPolicyTest {

    private static final int MINIMUM_CELL = 126;
    private static final int GAP = 11;

    @Test public void теснаяВысотаПриДостаточнойШиринеВыбираетДваРяда() {
        assertEquals(
                KeypadLayoutPolicy.Layout.COMPACT,
                KeypadLayoutPolicy.select(1450, 471, MINIMUM_CELL, MINIMUM_CELL, GAP)
        );
    }

    @Test public void ровноДостаточнаяВысотаИШиринаВыбираютПятьРядов() {
        assertEquals(
                KeypadLayoutPolicy.Layout.TALL,
                KeypadLayoutPolicy.select(263, 674, MINIMUM_CELL, MINIMUM_CELL, GAP)
        );
    }

    @Test public void нехваткаОдногоПикселяПоВысотеВыбираетДваРяда() {
        assertEquals(
                KeypadLayoutPolicy.Layout.COMPACT,
                KeypadLayoutPolicy.select(674, 673, MINIMUM_CELL, MINIMUM_CELL, GAP)
        );
    }

    @Test public void узкаяШиринаСохраняетПятьРядовЕслиОниПомещаются() {
        assertEquals(
                KeypadLayoutPolicy.Layout.TALL,
                KeypadLayoutPolicy.select(300, 700, MINIMUM_CELL, MINIMUM_CELL, GAP)
        );
    }

    @Test public void ширинаРовноНаПятьКлавишГарантируетМинимум() {
        KeypadLayoutPolicy.Layout layout = KeypadLayoutPolicy.select(
                674,
                471,
                MINIMUM_CELL,
                MINIMUM_CELL,
                GAP
        );
        KeypadLayoutPolicy.Geometry geometry = KeypadLayoutPolicy.geometry(
                layout, 0, 0, 674, 471, GAP, false
        );

        assertEquals(KeypadLayoutPolicy.Layout.COMPACT, layout);
        for (int slot = 0; slot < 10; slot++) {
            KeypadLayoutPolicy.Bounds cell = geometry.cell(slot);
            assertTrue(cell.width() >= MINIMUM_CELL);
            assertTrue(cell.height() >= MINIMUM_CELL);
        }
    }

    @Test public void еслиНиОднаСхемаНеПомещаетсяВыбираетсяБольшаяМинимальнаяСторона() {
        assertEquals(
                KeypadLayoutPolicy.Layout.COMPACT,
                KeypadLayoutPolicy.select(500, 300, MINIMUM_CELL, MINIMUM_CELL, GAP)
        );
        assertEquals(
                KeypadLayoutPolicy.Layout.TALL,
                KeypadLayoutPolicy.select(300, 500, MINIMUM_CELL, MINIMUM_CELL, GAP)
        );
    }

    @Test public void равныйFallbackДетерминированноПредпочитаетПятьРядов() {
        assertEquals(
                KeypadLayoutPolicy.Layout.TALL,
                KeypadLayoutPolicy.select(300, 300, MINIMUM_CELL, MINIMUM_CELL, GAP)
        );
    }

    @Test public void одинПиксельНижеМинимумаЯвноДеградируетВЛучшуюСхему() {
        KeypadLayoutPolicy.Layout layout = KeypadLayoutPolicy.select(
                673,
                471,
                MINIMUM_CELL,
                MINIMUM_CELL,
                GAP
        );
        KeypadLayoutPolicy.Geometry geometry = KeypadLayoutPolicy.geometry(
                layout, 0, 0, 673, 471, GAP, false
        );

        assertEquals(KeypadLayoutPolicy.Layout.COMPACT, layout);
        assertEquals(125, geometry.cell(4).width());
        assertTrue(geometry.cell(4).height() > 125);
    }

    @Test public void rtlЗеркалитЛогическиеСтолбцыВысокойСхемы() {
        KeypadLayoutPolicy.Geometry geometry = KeypadLayoutPolicy.geometry(
                KeypadLayoutPolicy.Layout.TALL, 10, 20, 205, 504, 4, true
        );

        assertBounds(geometry.cell(0), 115, 20, 215, 118);
        assertBounds(geometry.cell(5), 10, 20, 111, 118);
        assertBounds(geometry.cell(9), 10, 427, 111, 524);
    }

    @Test public void rtlЗеркалитЛогическиеСтолбцыКомпактнойСхемы() {
        KeypadLayoutPolicy.Geometry geometry = KeypadLayoutPolicy.geometry(
                KeypadLayoutPolicy.Layout.COMPACT, 7, 17, 509, 205, 4, true
        );

        assertBounds(geometry.cell(0), 418, 17, 516, 118);
        assertBounds(geometry.cell(4), 7, 17, 106, 118);
        assertBounds(geometry.cell(5), 418, 122, 516, 222);
    }

    @Test public void нечётныеРазмерыСПаддингомЗаполняютГраницыИНеМеняютGap() {
        KeypadLayoutPolicy.Geometry geometry = KeypadLayoutPolicy.geometry(
                KeypadLayoutPolicy.Layout.COMPACT, 13, 17, 509, 205, 4, false
        );

        assertBounds(geometry.cell(0), 13, 17, 112, 118);
        assertBounds(geometry.cell(3), 322, 17, 420, 118);
        assertBounds(geometry.cell(4), 424, 17, 522, 118);
        assertBounds(geometry.cell(9), 424, 122, 522, 222);
        assertEquals(4, geometry.cell(4).left - geometry.cell(3).right);
        assertEquals(4, geometry.cell(9).top - geometry.cell(4).bottom);
    }

    @Test public void goneКлавишаНеСдвигаетСемантическийСлотСледующей() {
        KeypadLayoutPolicy.Geometry geometry = KeypadLayoutPolicy.geometry(
                KeypadLayoutPolicy.Layout.COMPACT, 13, 17, 509, 205, 4, false
        );

        // Слот 5 может быть GONE, но слот 6 остаётся второй клавишей второго ряда.
        assertBounds(geometry.cell(6), 116, 122, 215, 222);
    }

    private static void assertBounds(
            KeypadLayoutPolicy.Bounds actual,
            int left,
            int top,
            int right,
            int bottom
    ) {
        assertEquals(left, actual.left);
        assertEquals(top, actual.top);
        assertEquals(right, actual.right);
        assertEquals(bottom, actual.bottom);
    }
}
