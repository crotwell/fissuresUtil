package edu.sc.seis.fissuresUtil.display;

import junit.framework.TestCase;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
/**
 * BasicSeismogramDisplayTest.java
 *
 */


public class BasicSeismogramDisplayTest extends TestCase
{
    public BasicSeismogramDisplayTest(String name){
        super(name);
    }
    
    public void setUp(){
        twoSineSeismos[0] = new DataSetSeismogram(simpleSineWave,
                                                  null,
                                                  "Simple");
        twoSineSeismos[1] = new DataSetSeismogram(customSineWave,
                                                  null,
                                                  "Custom");
        twoSineDisplay = new BasicSeismogramDisplay(twoSineSeismos, null);
        
        complexSeismos[0] = twoSineSeismos[0];
        complexSeismos[1] = twoSineSeismos[1];
        complexSeismos[2] = new DataSetSeismogram(spike,
                                                  null,
                                                  "spike");
        complexDisplay = new BasicSeismogramDisplay(complexSeismos, null);
        
        spikeSeismos[0] = complexSeismos[2];
        spikeDisplay = new BasicSeismogramDisplay(spikeSeismos, null);
    }
    public void testRemoveSeismogramArray(){
        assertEquals(twoSineDisplay.getSeismogramList().size(), 2);
        twoSineDisplay.remove(spikeSeismos);
        assertEquals(twoSineDisplay.getSeismogramList().size(), 2);
        assertTrue(twoSineDisplay.contains(twoSineSeismos[0]));
        assertFalse(twoSineDisplay.contains(spikeSeismos[0]));
        
        assertEquals(complexDisplay.getSeismogramList().size(), 3);
        complexDisplay.remove(spikeSeismos);
        assertEquals(complexDisplay.getSeismogramList().size(), 2);
        assertFalse(complexDisplay.contains(spikeSeismos[0]));
        
        assertEquals(spikeDisplay.getSeismogramList().size(), 1);
        spikeDisplay.remove(spikeSeismos);
        assertEquals(spikeDisplay.getSeismogramList().size(), 0);
        assertFalse(spikeDisplay.contains(spikeSeismos[0]));
        
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
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(BasicSeismogramDisplayTest.class);
    }
}

