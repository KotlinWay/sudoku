package info.javaway.sudoku.game;

import java.util.Calendar;

/**
 * Сегодняшний день по часам телефона — одним числом: сколько дней прошло от эпохи по местному
 * времени. Даты строкой в приложении нет нигде: серия побед сравнивает номера, а не текст,
 * и не зависит ни от локали, ни от формата.
 *
 * Прототип считал номер дня по UTC, а дату — по местному времени; вечером на востоке это
 * обрывало серию победившему накануне.
 */
public final class Day {

    private static final long MILLIS_IN_DAY = 24L * 60 * 60 * 1000;

    /** День, которого не было: с ним сравнивается «ещё ни разу не побеждал». */
    public static final int NEVER = Integer.MIN_VALUE;

    public final int number;

    /** Открыт нарочно: подсчёт серий и рекордов проверяется на номерах дней, без календаря. */
    public Day(int number) {
        this.number = number;
    }

    public static Day now() {
        return of(Calendar.getInstance());
    }

    static Day of(Calendar calendar) {
        long local = calendar.getTimeInMillis()
                + calendar.get(Calendar.ZONE_OFFSET) + calendar.get(Calendar.DST_OFFSET);
        return new Day((int) Math.floorDiv(local, MILLIS_IN_DAY));
    }

    /** Победа была вчера — значит серия продолжается, а не начинается заново. */
    public boolean isYesterday(int day) {
        return day == number - 1;
    }
}
