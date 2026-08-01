package info.javaway.sudoku.game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class CellsTest {

    @Test public void клеткаРаскладываетсяВСтрокуИСтолбец() {
        assertEquals(0, Cells.row(0));
        assertEquals(0, Cells.column(0));
        assertEquals(8, Cells.row(80));
        assertEquals(8, Cells.column(80));
        assertEquals(4, Cells.row(40));
        assertEquals(4, Cells.column(40));
    }

    @Test public void координатыСобираютсяОбратноВИндекс() {
        for (int cell = 0; cell < Cells.COUNT; cell++) {
            assertEquals(cell, Cells.at(Cells.row(cell), Cells.column(cell)));
        }
    }

    @Test public void квадратСчитаетсяПоТриНаТри() {
        assertEquals(0, Cells.box(Cells.at(0, 0)));
        assertEquals(0, Cells.box(Cells.at(2, 2)));
        assertEquals(1, Cells.box(Cells.at(0, 3)));
        assertEquals(4, Cells.box(Cells.at(4, 4)));
        assertEquals(8, Cells.box(Cells.at(8, 8)));
    }

    @Test public void уКаждойКлеткиРовноДвадцатьСоседей() {
        for (int cell = 0; cell < Cells.COUNT; cell++) {
            assertEquals(Cells.PEERS, Cells.peers(cell).length);
        }
    }

    @Test public void себяКлеткаСоседомНеСчитает() {
        for (int peer : Cells.peers(40)) {
            assertFalse(peer == 40);
        }
        assertFalse(Cells.sees(40, 40));
    }

    @Test public void соседствоВзаимно() {
        for (int cell = 0; cell < Cells.COUNT; cell++) {
            for (int peer : Cells.peers(cell)) {
                assertTrue(Cells.sees(peer, cell));
            }
        }
    }

    @Test public void соседствоЭтоСтрокаСтолбецИлиКвадрат() {
        assertTrue(Cells.sees(Cells.at(0, 0), Cells.at(0, 8)));    // строка
        assertTrue(Cells.sees(Cells.at(0, 0), Cells.at(8, 0)));    // столбец
        assertTrue(Cells.sees(Cells.at(0, 0), Cells.at(2, 2)));    // квадрат
        assertFalse(Cells.sees(Cells.at(0, 0), Cells.at(4, 4)));
    }
}
