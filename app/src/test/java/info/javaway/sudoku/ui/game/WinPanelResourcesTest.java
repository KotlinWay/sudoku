package info.javaway.sudoku.ui.game;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

public class WinPanelResourcesTest {

    @Test public void прежнийРекордНаПанелиОбъясняетУсловияВОбеихЛокалях() throws Exception {
        String en = read("src/main/res/values/strings.xml");
        String ru = read("src/main/res/values-ru/strings.xml");

        assertTrue(en.contains("<string name=\"won_best\">Best time in a regular game "
                + "without hints: %1$s</string>"));
        assertTrue(ru.contains("<string name=\"won_best\">Лучшее время в обычной игре "
                + "без подсказок: %1$s</string>"));
        assertFalse(en.contains("—"));
        assertFalse(ru.contains("—"));
    }

    private static String read(String relative) throws Exception {
        Path fromModule = Paths.get(relative);
        Path file = Files.exists(fromModule) ? fromModule : Paths.get("app").resolve(relative);
        return new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
    }
}
