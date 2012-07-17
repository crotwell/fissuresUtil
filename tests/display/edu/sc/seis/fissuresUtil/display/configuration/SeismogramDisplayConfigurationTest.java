package edu.sc.seis.fissuresUtil.display.configuration;

import junit.framework.TestCase;

/**
 * @author groves Created on Feb 17, 2005
 */
public class SeismogramDisplayConfigurationTest extends TestCase {

    public void testSimplest() throws Exception {
        SeismogramDisplayConfiguration sdc = create("recsec");
        assertEquals("recordSection", sdc.getType());
    }

    public void testRecord() throws Exception {
        SeismogramDisplayConfiguration sdc = create("simplest");
        assertEquals("basic", sdc.getType());
    }

    public static SeismogramDisplayConfiguration create(String name)
            throws Exception {
        return SeismogramDisplayConfiguration.create(DOMHelper.createElement("edu/sc/seis/fissuresUtil/display/configuration/"
                + name + ".xml"));
    }
}