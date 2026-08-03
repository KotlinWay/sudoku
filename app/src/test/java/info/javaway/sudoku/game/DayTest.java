package info.javaway.sudoku.game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.TimeZone;

import org.junit.Test;

public class DayTest {

    @Test public void суткиМеняютНомерДня() {
        assertEquals(day("UTC", 2026, Calendar.AUGUST, 1, 12).number + 1,
                day("UTC", 2026, Calendar.AUGUST, 2, 12).number);
    }

    @Test public void вТечениеСутокНомерНеМеняется() {
        assertEquals(day("UTC", 2026, Calendar.AUGUST, 1, 0).number,
                day("UTC", 2026, Calendar.AUGUST, 1, 23).number);
    }

    /**
     * Прототип брал номер дня по UTC: вечером на востоке это выдавало завтрашнюю задачу.
     * Здесь день считается по местному времени, поэтому одиннадцать вечера первого августа
     * во Владивостоке — это первое августа, а не второе.
     */
    @Test public void деньСчитаетсяПоМестномуВремени() {
        int local = day("Asia/Vladivostok", 2026, Calendar.AUGUST, 1, 23).number;
        int previous = day("Asia/Vladivostok", 2026, Calendar.AUGUST, 1, 1).number;

        assertEquals(previous, local);
    }

    @Test public void вчерашнийДеньУзнаётся() {
        Day today = new Day(500);

        assertTrue(today.isYesterday(499));
        assertFalse(today.isYesterday(500));
        assertFalse(today.isYesterday(498));
        assertFalse(today.isYesterday(Day.NEVER));
    }

    private static Day day(String zone, int year, int month, int date, int hour) {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(zone));
        calendar.clear();
        calendar.set(year, month, date, hour, 0, 0);
        return Day.of(calendar);
    }
}
