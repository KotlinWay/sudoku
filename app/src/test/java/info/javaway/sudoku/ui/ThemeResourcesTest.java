package info.javaway.sudoku.ui;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.xml.parsers.DocumentBuilderFactory;

public class ThemeResourcesTest {

    @Test public void appThemeUsesApiSafeQualifiedPlatformParent() throws Exception {
        assertEquals("@style/PlatformTheme",
                parent("src/main/res/values/themes.xml", "AppTheme"));
        assertEquals("@android:style/Theme.DeviceDefault",
                parent("src/main/res/values/themes.xml", "PlatformTheme"));
        assertEquals("@android:style/Theme.DeviceDefault.DayNight",
                parent("src/main/res/values-v29/themes.xml", "PlatformTheme"));
    }

    @Test public void qualifiedFileOverridesOnlyThePlatformParent() throws Exception {
        NodeList styles = document("src/main/res/values-v29/themes.xml")
                .getElementsByTagName("style");

        assertEquals(1, styles.getLength());
        assertEquals("PlatformTheme", ((Element) styles.item(0)).getAttribute("name"));
    }

    private static String parent(String file, String styleName) throws Exception {
        NodeList styles = document(file).getElementsByTagName("style");
        for (int i = 0; i < styles.getLength(); i++) {
            Element style = (Element) styles.item(i);
            if (styleName.equals(style.getAttribute("name"))) {
                return style.getAttribute("parent");
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
