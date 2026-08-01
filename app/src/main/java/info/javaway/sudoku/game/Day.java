package info.javaway.sudoku.game;

import java.util.Calendar;

/**
 * Сегодняшний день по часам телефона — одним числом: сколько дней прошло от эпохи по местному
 * времени. Даты строкой в приложении нет нигде: серия побед и отметка «задача дня решена»
 * сравнивают номера, а не текст, и не зависят ни от локали, ни от формата.
 *
 * Прототип считал номер дня по UTC, а дату — по местному времени; вечером на востоке это
 * давало «сегодняшнюю» задачу из завтрашнего дня и отметку о решении не в том дне.
 */
public final class Day {

    private static final long MILLIS_IN_DAY = 24L * 60 * 60 * 1000;

    /** Уровни задачи дня по кругу: неделя не должна состоять из одинаковых по трудности задач. */
    private static final Difficulty[] WEEK = {
            Difficulty.EASY, Difficulty.MEDIUM, Difficulty.MEDIUM, Difficulty.HARD
    };

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

    /**
     * Seed задачи дня. Номер умножается на большое нечётное число: подряд идущие дни иначе
     * дают почти одинаковые начальные состояния генератора и похожие задачи.
     */
    public int seed() {
        return number * 0x9E3779B1;
    }

    public Difficulty level() {
        return WEEK[Math.floorMod(number, WEEK.length)];
    }

    /**
     * Номер задачи для показа. Полный номер дня от эпохи — пятизначное число, которое человеку
     * ничего не говорит и не влезает на вкладку; четырёх цифр хватает, чтобы отличать задачи.
     */
    public int label() {
        return Math.floorMod(number, 10000);
    }

    /** Победа была вчера — значит серия продолжается, а не начинается заново. */
    public boolean isYesterday(int day) {
        return day == number - 1;
    }
}
