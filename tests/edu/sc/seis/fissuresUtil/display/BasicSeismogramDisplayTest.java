package edu.sc.seis.fissuresUtil.display;

import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;
import edu.sc.seis.fissuresUtil.xml.MemoryDataSetSeismogram;
import junit.framework.TestCase;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.PatternLayout;

/**
 * BasicSeismogramDisplayTest.java
 *
 */


public class BasicSeismogramDisplayTest extends TestCase {
    public BasicSeismogramDisplayTest(String name){
        super(name);
        // Set up a simple configuration that logs on the console.
        PatternLayout layout = new PatternLayout("%t %C{1} %r %m%n");
        BasicConfigurator.configure(new ConsoleAppender(layout,
                                                        ConsoleAppender.SYSTEM_OUT));
    }

    public void setUp(){
        SingleSeismogramWindowDisplay vsd =
            new SingleSeismogramWindowDisplay(null, null, null);
        twoSineSeismos[0] = new MemoryDataSetSeismogram(simpleSineWave,
                                                        "Simple");
        twoSineSeismos[1] = new MemoryDataSetSeismogram(customSineWave,
                                                        "Custom");
        twoSineDisplay = new BasicSeismogramDisplay(twoSineSeismos, vsd);

        complexSeismos[0] = twoSineSeismos[0];
        complexSeismos[1] = twoSineSeismos[1];
        complexSeismos[2] = new MemoryDataSetSeismogram(spike,
                                                        "spike");
        complexDisplay = new BasicSeismogramDisplay(complexSeismos, vsd);

        spikeSeismos[0] = complexSeismos[2];
        spikeDisplay = new BasicSeismogramDisplay(spikeSeismos, vsd);
    }

    public void testSineRemove() {
        assertEquals(twoSineDisplay.getSeismogramList().size(), 2);
        twoSineDisplay.remove(spikeSeismos);
        assertEquals(twoSineDisplay.getSeismogramList().size(), 2);
        assertTrue(twoSineDisplay.contains(twoSineSeismos[0]));
        assertFalse(twoSineDisplay.contains(spikeSeismos[0]));

    }

    public void testComplexRemove(){
        assertEquals(complexDisplay.getSeismogramList().size(), 3);
        complexDisplay.remove(spikeSeismos);
        assertEquals(complexDisplay.getSeismogramList().size(), 2);
        assertFalse(complexDisplay.contains(spikeSeismos[0]));
    }

    public void testSpikeRemove(){
        assertEquals(spikeDisplay.getSeismogramList().size(), 1);
        spikeDisplay.remove(spikeSeismos);
        assertEquals(spikeDisplay.getSeismogramList().size(), 0);
        assertFalse(spikeDisplay.contains(spikeSeismos[0]));
    }

    public void testComplexRemoveComplex(){
        complexDisplay.remove(complexSeismos);
        assertEquals(complexDisplay.getSeismogramList().size(), 0);
        assertFalse(complexDisplay.contains(complexSeismos[0]));
    }

    private LocalSeismogramImpl simpleSineWave = SimplePlotUtil.createSineWave();

    private LocalSeismogramImpl customSineWave = SimplePlotUtil.createCustomSineWave();

    private LocalSeismogramImpl spike = SimplePlotUtil.createSpike();

    private DataSetSeismogram[] twoSineSeismos = new DataSetSeismogram[2];

    private DataSetSeismogram[] complexSeismos = new DataSetSeismogram[3];

    private DataSetSeismogram[] spikeSeismos = new DataSetSeismogram[1];

    private BasicSeismogramDisplay twoSineDisplay;

    private BasicSeismogramDisplay complexDisplay;

    private BasicSeismogramDisplay spikeDisplay;

}

