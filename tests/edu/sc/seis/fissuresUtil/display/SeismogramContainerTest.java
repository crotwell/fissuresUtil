package edu.sc.seis.fissuresUtil.display;

import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;
import edu.sc.seis.fissuresUtil.xml.MemoryDataSetSeismogram;
import edu.sc.seis.fissuresUtil.xml.SeisDataChangeEvent;
import junit.framework.TestCase;
import junitx.framework.ArrayAssert;

public class SeismogramContainerTest extends TestCase{
    public SeismogramContainerTest(String name){
        super(name);
    }

    public void setUp(){
        seismograms = DisplayUtilsTest.createThreeSeisArray();
        dss = new MemoryDataSetSeismogram(seismograms, null);
        container = new SeismogramContainer(dss);
    }

    public void testPushAlreadyAddedData(){
        LocalSeismogramImpl[] alreadyContained = container.getSeismograms();
        while(alreadyContained.length < 3){
            try {
                Thread.sleep(5);
            }
            catch (InterruptedException e) {}
            alreadyContained = container.getSeismograms();
        }
        container.pushData(new SeisDataChangeEvent(DisplayUtilsTest.createThreeSeisArray(),
                                                   null,
                                                   container));
        LocalSeismogramImpl[] nowContains = container.getSeismograms();
        ArrayAssert.assertEquivalenceArrays(alreadyContained, nowContains);
    }

    public void testPushNewData(){
        LocalSeismogramImpl[] alreadyContained = container.getSeismograms();
        LocalSeismogramImpl[] otherSeis = DisplayUtilsTest.createOtherSeisArray();
        container.pushData(new SeisDataChangeEvent(otherSeis, null, container));
        LocalSeismogramImpl[] nowContains = container.getSeismograms();
        assertEquals("must be exactly 6 seismograms", 6, nowContains.length);
        for (int i = 0; i < nowContains.length; i++){
            boolean found = false;
            for (int j = 0; j < alreadyContained.length && !found; j++) {
                if(nowContains[i] == alreadyContained[j]){
                    found = true;
                }
            }
            for (int j = 0; j < otherSeis.length && !found; j++) {
                if(nowContains[i] == otherSeis[j]){
                    found = true;
                }
            }
            assertTrue(found);
        }
    }

    public void testGetSeismograms(){
        LocalSeismogramImpl[] contained = container.getSeismograms();
        while(contained.length < 3){
            try {
                Thread.sleep(5);
            }
            catch (InterruptedException e) {}
            contained = container.getSeismograms();
        }
        ArrayAssert.assertEquivalenceArrays(seismograms, contained);
    }

    private LocalSeismogramImpl[] seismograms;

    private DataSetSeismogram dss;

    private SeismogramContainer container;
}

