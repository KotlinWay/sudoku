package info.javaway.sudoku.game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class RngTest {

    @Test public void числаЛежатВПолуинтервалеОтНуляДоЕдиницы() {
        Rng rng = new Rng(1);

        for (int i = 0; i < 1000; i++) {
            double value = rng.next();
            assertTrue(value >= 0);
            assertTrue(value < 1);
        }
    }

    @Test public void границаНикогдаНеДостигается() {
        Rng rng = new Rng(12345);

        for (int i = 0; i < 1000; i++) {
            int value = rng.next(9);
            assertTrue(value >= 0);
            assertTrue(value < 9);
        }
    }

    /** На этом держится воспроизводимость задачи: тот же seed — та же доска. */
    @Test public void одинSeedДаётОдинаковуюПоследовательность() {
        Rng first = new Rng(2026);
        Rng second = new Rng(2026);

        for (int i = 0; i < 100; i++) {
            assertEquals(first.next(), second.next(), 0);
        }
    }

    @Test public void разныеSeedРасходятся() {
        assertNotEquals(new Rng(1).next(), new Rng(2).next(), 0);
    }

    @Test public void тасованиеСохраняетСоставМассива() {
        int[] values = {1, 2, 3, 4, 5, 6, 7, 8, 9};

        new Rng(5).shuffle(values);

        java.util.Arrays.sort(values);
        assertTrue(java.util.Arrays.equals(new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9}, values));
    }

    @Test public void тасованиеДействительноПеремешивает() {
        int[] values = new int[81];
        for (int i = 0; i < values.length; i++) {
            values[i] = i;
        }

        new Rng(9).shuffle(values);

        int inPlace = 0;
        for (int i = 0; i < values.length; i++) {
            if (values[i] == i) inPlace++;
        }
        assertTrue("массив остался прежним", inPlace < values.length / 2);
    }
}
