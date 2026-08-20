package info.javaway.sudoku.game;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class GameModeTest {
    @Test public void известноеИмяВосстанавливается() {
        assertEquals(GameMode.RELAXED,
                GameMode.byName("RELAXED", GameMode.STANDARD));
    }

    @Test public void неизвестноеИмяДаётОбычныйРежим() {
        assertEquals(GameMode.STANDARD,
                GameMode.byName("future", GameMode.STANDARD));
        assertEquals(GameMode.STANDARD,
                GameMode.byName(null, GameMode.STANDARD));
    }
}
