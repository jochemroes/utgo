package nl.utwente.utgo;

import android.util.Pair;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class PrettyPrintTest {
    List<Pair<String, Float>> rgbColors;
    public static final float RGB_TO_HUE_DELTA = 1f;
    public static final float RGB_TO_HUE_UPPER_BOUND = 360f;
    public static final float RGB_TO_HUE_LOWER_BOUND = 0f;

    @Before
    public void before() {
        //populate color testing array////////////////////////
        rgbColors = new ArrayList<>();
        rgbColors.add(new Pair<String, Float>("#FF0000",12f));
        rgbColors.add(new Pair<String, Float>("#00FF00",12f));
        rgbColors.add(new Pair<String, Float>("#0000FF",12f));
        rgbColors.add(new Pair<String, Float>("#FF8800",12f));
        rgbColors.add(new Pair<String, Float>("#8800FF",12f));
        rgbColors.add(new Pair<String, Float>("#00FF88",12f));
        //////////////////////////////////////////////////////

    }

    @Test
    public void integerPrettyPrintTest() {

    }

    @Test
    public void timePrettyPrintTest() {

    }

    @Test
    public void rgbToHueTest() {
        for(Pair<String,Float> p : rgbColors) {
            float actual = PrettyPrint.rgbToHue(p.first);
            //check that the expected value matches
            System.out.println("from " + p.first + " to " + actual);
            Assert.assertEquals(p.second, actual, RGB_TO_HUE_DELTA);
            //check that the output stays within bounds
            Assert.assertTrue(actual >= RGB_TO_HUE_LOWER_BOUND + RGB_TO_HUE_DELTA
                    && actual <= RGB_TO_HUE_UPPER_BOUND + RGB_TO_HUE_DELTA);
        }
    }
}
