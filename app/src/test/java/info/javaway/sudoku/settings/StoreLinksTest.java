package info.javaway.sudoku.settings;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class StoreLinksTest {

    private static final String SUDOKU = "info.javaway.sudoku";
    private static final String RACCOON = "info.javaway.raccoon_notes";

    @Test
    public void googlePlayValueSelectsOnlyGooglePlayLinks() {
        assertTrue(Links.isGooglePlay(Links.GOOGLE_PLAY));
        assertEquals("https://play.google.com/store/apps/dev?id=6023648979127962332",
                Links.developerUrl(Links.GOOGLE_PLAY));
        assertEquals("market://details?id=" + SUDOKU,
                Links.appUri(Links.GOOGLE_PLAY, SUDOKU));
        assertEquals("https://play.google.com/store/apps/details?id=" + SUDOKU,
                Links.webUrl(Links.GOOGLE_PLAY, SUDOKU));
        assertEquals("https://play.google.com/store/apps/details?id=" + RACCOON,
                Links.webUrl(Links.GOOGLE_PLAY, RACCOON));
    }

    @Test
    public void rustoreValueSelectsOnlyRuStoreLinks() {
        assertFalse(Links.isGooglePlay(Links.RUSTORE));
        assertEquals("https://www.rustore.ru/catalog/developer/a83331c1",
                Links.developerUrl(Links.RUSTORE));
        assertEquals("rustore://apps.rustore.ru/app/" + SUDOKU,
                Links.appUri(Links.RUSTORE, SUDOKU));
        assertEquals("https://apps.rustore.ru/app/" + SUDOKU,
                Links.webUrl(Links.RUSTORE, SUDOKU));
        assertEquals("https://apps.rustore.ru/app/" + RACCOON,
                Links.webUrl(Links.RUSTORE, RACCOON));
    }

    @Test
    public void unknownStoreValueIsRejectedAtTheBoundary() {
        assertThrows(IllegalArgumentException.class,
                () -> Links.developerUrl("OTHER"));
    }
}
