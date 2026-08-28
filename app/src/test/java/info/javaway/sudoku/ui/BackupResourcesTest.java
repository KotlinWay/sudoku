package info.javaway.sudoku.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;

public class BackupResourcesTest {

    private static final String ANDROID = "http://schemas.android.com/apk/res/android";
    private static final Set<String> APP_DOMAINS = new HashSet<>(Arrays.asList(
            "root", "file", "database", "sharedpref", "external",
            "device_root", "device_file", "device_database", "device_sharedpref"));

    @Test public void manifestDisablesBackupAndReferencesBothRuleFormats() throws Exception {
        Element application = (Element) document("src/main/AndroidManifest.xml")
                .getElementsByTagName("application").item(0);

        assertEquals("false", application.getAttributeNS(ANDROID, "allowBackup"));
        assertEquals("@xml/backup_rules",
                application.getAttributeNS(ANDROID, "fullBackupContent"));
        assertEquals("@xml/data_extraction_rules",
                application.getAttributeNS(ANDROID, "dataExtractionRules"));
    }

    @Test public void legacyRulesExcludeEveryAppOwnedDomain() throws Exception {
        Element root = document("src/main/res/xml/backup_rules.xml");

        assertEquals("full-backup-content", root.getTagName());
        assertEquals(APP_DOMAINS, excludedDomains(root));
    }

    @Test public void android12RulesExcludeCloudAndDeviceTransfer() throws Exception {
        Element root = document("src/main/res/xml/data_extraction_rules.xml");

        assertEquals("data-extraction-rules", root.getTagName());
        assertEquals(APP_DOMAINS, excludedDomains(child(root, "cloud-backup")));
        assertEquals(APP_DOMAINS, excludedDomains(child(root, "device-transfer")));
    }

    @Test public void releaseShrinkerKeepsBackupRuleResources() throws Exception {
        Element resources = document("src/main/res/raw/info_javaway_sudoku_backup_keep.xml");

        assertEquals("resources", resources.getTagName());
        String kept = resources.getAttributeNS("http://schemas.android.com/tools", "keep");
        assertTrue(kept.contains("@xml/backup_rules"));
        assertTrue(kept.contains("@xml/data_extraction_rules"));
    }

    private static Set<String> excludedDomains(Element parent) {
        Set<String> domains = new HashSet<>();
        NodeList excludes = parent.getElementsByTagName("exclude");
        for (int i = 0; i < excludes.getLength(); i++) {
            Element exclude = (Element) excludes.item(i);
            assertEquals(".", exclude.getAttribute("path"));
            domains.add(exclude.getAttribute("domain"));
        }
        return domains;
    }

    private static Element child(Element parent, String name) {
        return (Element) parent.getElementsByTagName(name).item(0);
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
