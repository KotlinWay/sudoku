package info.javaway.sudoku.stats;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import info.javaway.sudoku.game.Day;
import info.javaway.sudoku.game.Difficulty;

public class StatsTest {

    private static final Day TODAY = new Day(1000);
    private static final Day TOMORROW = new Day(1001);
    private static final Day LATER = new Day(1005);

    @Test public void новаяСтатистикаПуста() {
        Stats stats = Stats.empty();

        assertEquals(0, stats.played);
        assertEquals(0, stats.wins);
        assertEquals(0, stats.winRate());
        assertEquals(Stats.NO_TIME, stats.best(Difficulty.EASY));
    }

    @Test public void победаСчитаетсяИСыграннойИВыигранной() {
        Stats stats = Stats.empty().afterWin(Difficulty.EASY, 300, 0, true, TODAY).stats;

        assertEquals(1, stats.played);
        assertEquals(1, stats.wins);
        assertEquals(100, stats.winRate());
    }

    /** В прототипе счётчик сыгранных рос только при победе, и доля всегда была стопроцентной. */
    @Test public void проигрышТожеПартия() {
        Stats stats = Stats.empty().afterLoss();

        assertEquals(1, stats.played);
        assertEquals(0, stats.wins);
        assertEquals(0, stats.winRate());
    }

    @Test public void доляПобедСчитаетсяОтВсехПартий() {
        Stats stats = Stats.empty().afterLoss()
                .afterWin(Difficulty.EASY, 300, 0, true, TODAY).stats;

        assertEquals(2, stats.played);
        assertEquals(50, stats.winRate());
    }

    @Test public void первоеВремяБезПодсказокСтановитсяЛучшим() {
        Outcome outcome = Stats.empty().afterWin(Difficulty.HARD, 420, 0, true, TODAY);

        assertTrue(outcome.record);
        assertEquals(420, outcome.best);
        assertEquals(420, outcome.stats.best(Difficulty.HARD));
    }

    @Test public void подсказкиЛишаютРекорда() {
        Outcome outcome = Stats.empty().afterWin(Difficulty.HARD, 60, 1, false, TODAY);

        assertFalse(outcome.record);
        assertEquals(Stats.NO_TIME, outcome.stats.best(Difficulty.HARD));
    }

    @Test public void болееБыстраяПартияОбновляетРекорд() {
        Stats first = Stats.empty().afterWin(Difficulty.HARD, 420, 0, true, TODAY).stats;

        Outcome second = first.afterWin(Difficulty.HARD, 300, 0, true, TOMORROW);

        assertTrue(second.record);
        assertEquals(300, second.best);
    }

    @Test public void болееМедленнаяПартияРекордНеПортит() {
        Stats first = Stats.empty().afterWin(Difficulty.HARD, 300, 0, true, TODAY).stats;

        Outcome second = first.afterWin(Difficulty.HARD, 420, 0, true, TOMORROW);

        assertFalse(second.record);
        assertEquals(300, second.best);
    }

    @Test public void рекордыУровнейНеСмешиваются() {
        Stats stats = Stats.empty().afterWin(Difficulty.EASY, 100, 0, true, TODAY).stats;

        assertEquals(100, stats.best(Difficulty.EASY));
        assertEquals(Stats.NO_TIME, stats.best(Difficulty.HARD));
    }

    @Test public void перваяПобедаНачинаетСерию() {
        assertEquals(1, Stats.empty().afterWin(Difficulty.EASY, 60, 0, true, TODAY).stats.streak);
    }

    @Test public void вчерашняяПобедаПродолжаетСерию() {
        Stats first = Stats.empty().afterWin(Difficulty.EASY, 60, 0, true, TODAY).stats;

        Stats second = first.afterWin(Difficulty.EASY, 60, 0, true, TOMORROW).stats;

        assertEquals(2, second.streak);
    }

    @Test public void втораяПобедаЗаДеньСериюНеУдлиняет() {
        Stats first = Stats.empty().afterWin(Difficulty.EASY, 60, 0, true, TODAY).stats;

        Stats second = first.afterWin(Difficulty.EASY, 60, 0, true, TODAY).stats;

        assertEquals(1, second.streak);
        assertEquals(2, second.wins);
    }

    @Test public void пропущенныйДеньНачинаетСериюЗаново() {
        Stats first = Stats.empty().afterWin(Difficulty.EASY, 60, 0, true, TODAY).stats;

        Stats second = first.afterWin(Difficulty.EASY, 60, 0, true, LATER).stats;

        assertEquals(1, second.streak);
    }

    @Test public void лучшаяСерияПомнитсяПослеОбрыва() {
        Stats stats = Stats.empty()
                .afterWin(Difficulty.EASY, 60, 0, true, TODAY).stats
                .afterWin(Difficulty.EASY, 60, 0, true, TOMORROW).stats
                .afterWin(Difficulty.EASY, 60, 0, true, LATER).stats;

        assertEquals(1, stats.streak);
        assertEquals(2, stats.bestStreak);
    }

    @Test public void потраченныеПодсказкиНакапливаются() {
        Stats stats = Stats.empty()
                .afterWin(Difficulty.EASY, 60, 2, false, TODAY).stats
                .afterWin(Difficulty.EASY, 60, 1, false, TOMORROW).stats;

        assertEquals(3, stats.hintsUsed);
    }

    @Test public void спокойнаяПобедаСчитаетсяНоНеСтавитРекорд() {
        Outcome outcome = Stats.empty().afterWin(
                Difficulty.HARD, 60, 0, false, TODAY);

        assertEquals(1, outcome.stats.played);
        assertEquals(1, outcome.stats.wins);
        assertEquals(1, outcome.stats.streak);
        assertFalse(outcome.record);
        assertEquals(Stats.NO_TIME, outcome.stats.best(Difficulty.HARD));
    }
}
