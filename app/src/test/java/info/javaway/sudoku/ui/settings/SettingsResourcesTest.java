package info.javaway.sudoku.ui.settings;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;

public class SettingsResourcesTest {

    @Test public void настройкаЭкранаЕстьВРазметкеИДвухЛокалях() throws Exception {
        String layout = read("src/main/res/layout/activity_settings.xml");
        String en = read("src/main/res/values/strings.xml");
        String ru = read("src/main/res/values-ru/strings.xml");

        assertTrue(layout.contains("android:id=\"@+id/keep_screen_on\""));
        assertTrue(layout.contains("android:text=\"@string/keep_screen_on\""));
        assertTrue(layout.contains("android:text=\"@string/keep_screen_on_hint\""));
        assertTrue(en.contains("<string name=\"keep_screen_on\">Keep screen on</string>"));
        assertTrue(en.contains("<string name=\"keep_screen_on_hint\">While a game is running, I will keep the screen on.</string>"));
        assertTrue(ru.contains("<string name=\"keep_screen_on\">Не выключать экран</string>"));
        assertTrue(ru.contains("<string name=\"keep_screen_on_hint\">Пока идёт партия, я не дам экрану погаснуть.</string>"));
    }

    private static String read(String relative) throws Exception {
        Path fromModule = Paths.get(relative);
        Path file = Files.exists(fromModule) ? fromModule : Paths.get("app").resolve(relative);
        return new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
    }
}
