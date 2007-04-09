package edu.sc.seis.fissuresUtil.cache;

import junit.framework.TestCase;
import edu.iris.Fissures.IfEvent.Magnitude;

public class EventUtilTest extends TestCase {

    public void testSortMagnitudes() {
        String[] controlTypes = {"MO", "Mw", "M", "A", "ME"};
        Magnitude[] controlMags = createMagArray(controlTypes);
        String[] unsortedTypes = {"A", "M", "ME", "MO", "Mw"};
        Magnitude[] unsortedMags = createMagArray(unsortedTypes);
        Magnitude[] sortedMagnitudes = EventUtil.sortMagnitudes(unsortedMags);
        for(int i = 0; i < sortedMagnitudes.length; i++) {
            assertEquals(controlMags[i].type, sortedMagnitudes[i].type);
        }
    }

    public static Magnitude[] createMagArray(String[] magTypes) {
        Magnitude[] mags = new Magnitude[5];
        String contributor = "contributor";
        float magValue = 8.0f;
        for(int i = 0; i < mags.length; i++) {
            mags[i] = new Magnitude(magTypes[i], magValue, contributor);
        }
        return mags;
    }
}
