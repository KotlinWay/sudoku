package info.javaway.sudoku.ui.game;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class BackNavigationTest {

    @Test public void platformCallbackTracksPanelVisibilityOnApi33AndNewer()
            throws Exception {
        String source = read("src/main/java/info/javaway/sudoku/ui/game/GameActivity.java");

        assertTrue(source.contains(
                "Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU"));
        assertTrue(source.contains("if (state.panel && backCallback == null)"));
        assertTrue(source.contains("registerOnBackInvokedCallback("));
        assertTrue(source.contains("else if (!state.panel && backCallback != null)"));
        assertTrue(source.contains("unregisterOnBackInvokedCallback("));
    }

    @Test public void legacyFallbackIsGuardedAndNarrowlySuppressed() throws Exception {
        String source = read("src/main/java/info/javaway/sudoku/ui/game/GameActivity.java");

        assertTrue(source.contains("@SuppressLint(\"GestureBackNavigation\")"));
        assertTrue(source.contains("if (!handleBack()) super.onBackPressed();"));
        assertFalse(source.contains("KEYCODE_BACK"));
    }

    @Test public void manifestKeepsPredictiveBackEnabledWithoutAndroidX() throws Exception {
        String manifest = read("src/main/AndroidManifest.xml");
        String build = read("build.gradle");

        assertTrue(manifest.contains("android:enableOnBackInvokedCallback=\"true\""));
        assertFalse(build.contains("androidx.activity"));
    }

    private static String read(String relative) throws Exception {
        return new String(Files.readAllBytes(projectFile(relative)), StandardCharsets.UTF_8);
    }

    private static Path projectFile(String relative) {
        Path fromModule = Paths.get(relative);
        if (Files.exists(fromModule)) return fromModule;
        return Paths.get("app").resolve(relative);
    }
}
