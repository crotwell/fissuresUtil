package edu.sc.seis.fissuresUtil.display;

import edu.sc.seis.fissuresUtil.display.ChoiceDecimalFormat;
import junit.framework.TestCase;

/**
 * @author groves Created on Mar 30, 2005
 */
public class ChoiceDecimalFormatTest extends TestCase {

    public void testFormatStyleA() {
        ChoiceDecimalFormat cdf = ChoiceDecimalFormat.createTomStyleA();
        assertEquals("8.8", cdf.format(8.8));
        assertEquals("9.8", cdf.format(9.8));
        assertEquals("99.8", cdf.format(99.8));
        assertEquals("100", cdf.format(100d));
        assertEquals("13284971200", cdf.format(13284971200.13284971200));
    }

    public void testFormatStyleB() {
        ChoiceDecimalFormat cdf = ChoiceDecimalFormat.createTomStyleB();
        assertEquals("8.8", cdf.format(8.8));
        assertEquals("99", cdf.format(98.8));
        assertEquals("100", cdf.format(100d));
        assertEquals("13284971200", cdf.format(13284971200.13284971200));
    }
}