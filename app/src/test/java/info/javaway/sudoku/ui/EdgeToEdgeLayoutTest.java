package info.javaway.sudoku.ui;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.w3c.dom.Element;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.xml.parsers.DocumentBuilderFactory;

public class EdgeToEdgeLayoutTest {

    private static final String ANDROID = "http://schemas.android.com/apk/res/android";

    @Test public void activityLayoutsKeepControlsOutsideSystemUi() throws Exception {
        String[] layouts = {
                "src/main/res/layout/activity_game.xml",
                "src/main/res/layout-land/activity_game.xml",
                "src/main/res/layout/activity_new_game.xml",
                "src/main/res/layout/activity_settings.xml"
        };

        for (String layout : layouts) {
            DocumentBuilderFactory parser = DocumentBuilderFactory.newInstance();
            parser.setNamespaceAware(true);
            Element root = parser.newDocumentBuilder()
                    .parse(projectFile(layout).toFile())
                    .getDocumentElement();

            assertEquals(layout, "true", root.getAttributeNS(ANDROID, "fitsSystemWindows"));
        }
    }

    private static Path projectFile(String relative) {
        Path fromModule = Paths.get(relative);
        if (Files.exists(fromModule)) return fromModule;
        return Paths.get("app").resolve(relative);
    }
}
