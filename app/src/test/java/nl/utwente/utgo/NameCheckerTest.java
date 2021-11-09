package nl.utwente.utgo;

import org.junit.Assert;
import org.junit.Test;

public class NameCheckerTest {

    @Test
    public void correctName() {
        Assert.assertTrue(NameChecker.isNameCorrect("AR_Lover"));
        Assert.assertTrue(NameChecker.isNameCorrect("tokkie038!"));
    }

    @Test
    public void incorrectName() {
        Assert.assertFalse(NameChecker.isNameCorrect("Jaap"));
        Assert.assertFalse(NameChecker.isNameCorrect("ok"));
        Assert.assertFalse(NameChecker.isNameCorrect("Deze naam is veel te lang"));
    }
}
