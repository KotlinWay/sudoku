package info.javaway.sudoku.ui.game;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class KeypadLayoutPolicyTest {

    @Test public void теснаяВысотаВыбираетДваРяда() {
        assertFalse(KeypadLayoutPolicy.usesTallLayout(471, 126, 11));
    }

    @Test public void ровноДостаточнаяВысотаВыбираетПятьРядов() {
        assertTrue(KeypadLayoutPolicy.usesTallLayout(674, 126, 11));
    }

    @Test public void нехваткаОдногоПикселяВыбираетДваРяда() {
        assertFalse(KeypadLayoutPolicy.usesTallLayout(673, 126, 11));
    }

    @Test public void штатнаяВысотаСохраняетПятьРядов() {
        assertTrue(KeypadLayoutPolicy.usesTallLayout(900, 126, 11));
    }
}
