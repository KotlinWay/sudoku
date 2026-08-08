package info.javaway.sudoku.ui;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

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

    @Test public void portraitKeyboardHasExtraSpaceAboveSystemGestures() throws Exception {
        Element root = document("src/main/res/layout/activity_game.xml");
        Element content = (Element) root.getElementsByTagName("LinearLayout").item(0);

        assertEquals("@dimen/space_m", content.getAttributeNS(ANDROID, "paddingBottom"));
    }

    @Test public void statusBarUsesDarkIconsOnYellowInBothThemes() throws Exception {
        String day = styleItem("src/main/res/values/themes.xml",
                "AppTheme", "android:windowLightStatusBar");
        String night = styleItem("src/main/res/values-night/themes.xml",
                "AppTheme", "android:windowLightStatusBar");

        assertEquals("true", day);
        assertEquals("true", night.isEmpty() ? day : night);
    }

    private static String styleItem(String file, String styleName, String itemName)
            throws Exception {
        NodeList styles = document(file).getElementsByTagName("style");
        for (int i = 0; i < styles.getLength(); i++) {
            Element style = (Element) styles.item(i);
            if (!styleName.equals(style.getAttribute("name"))) continue;
            NodeList items = style.getElementsByTagName("item");
            for (int j = 0; j < items.getLength(); j++) {
                Element item = (Element) items.item(j);
                if (itemName.equals(item.getAttribute("name"))) {
                    return item.getTextContent().trim();
                }
            }
        }
        return "";
    }

    private static Element document(String relative) throws Exception {
        DocumentBuilderFactory parser = DocumentBuilderFactory.newInstance();
        parser.setNamespaceAware(true);
        return parser.newDocumentBuilder()
                .parse(projectFile(relative).toFile())
                .getDocumentElement();
    }

    private static Path projectFile(String relative) {
        Path fromModule = Paths.get(relative);
        if (Files.exists(fromModule)) return fromModule;
        return Paths.get("app").resolve(relative);
    }
}
