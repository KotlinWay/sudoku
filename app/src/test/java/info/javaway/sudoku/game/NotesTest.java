package info.javaway.sudoku.game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class NotesTest {

    @Test public void пустыеПометкиНичегоНеСодержат() {
        for (int digit = 1; digit <= Cells.SIDE; digit++) {
            assertFalse(Notes.has(Notes.NONE, digit));
        }
        assertTrue(Notes.isEmpty(Notes.NONE));
    }

    @Test public void добавленнаяЦифраНаходится() {
        int notes = Notes.with(Notes.NONE, 5);

        assertTrue(Notes.has(notes, 5));
        assertFalse(Notes.has(notes, 4));
        assertFalse(Notes.isEmpty(notes));
    }

    @Test public void цифрыНеМешаютДругДругу() {
        int notes = Notes.with(Notes.with(Notes.with(Notes.NONE, 1), 5), 9);

        assertTrue(Notes.has(notes, 1));
        assertTrue(Notes.has(notes, 5));
        assertTrue(Notes.has(notes, 9));
        assertFalse(Notes.has(notes, 2));
    }

    @Test public void снятиеУбираетТолькоСвоюЦифру() {
        int notes = Notes.without(Notes.with(Notes.with(Notes.NONE, 3), 7), 3);

        assertFalse(Notes.has(notes, 3));
        assertTrue(Notes.has(notes, 7));
    }

    @Test public void снятиеОтсутствующейЦифрыНичегоНеМеняет() {
        int notes = Notes.with(Notes.NONE, 7);

        assertEquals(notes, Notes.without(notes, 3));
    }

    @Test public void переключениеРаботаетВОбеСтороны() {
        int once = Notes.toggled(Notes.NONE, 4);
        int twice = Notes.toggled(once, 4);

        assertTrue(Notes.has(once, 4));
        assertEquals(Notes.NONE, twice);
    }
}
