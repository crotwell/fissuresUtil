package edu.sc.seis.fissuresUtil.display;

import junitx.extensions.EqualsHashCodeTestCase;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.TestUtils;
import edu.sc.seis.fissuresUtil.time.RangeToolTest;

public class SeismogramIteratorTest extends EqualsHashCodeTestCase{
    public SeismogramIteratorTest(String name){
        super(name);
    }

    public void setUp() throws Exception{
        super.setUp();
        iterator = new SeismogramIterator(TEST_ITERATOR,
                                          TestUtils.createThreeSeisArray());
    }


    protected Object createInstance() throws Exception {
        return new SeismogramIterator(TEST_ITERATOR,
                                      TestUtils.createThreeSeisArray());
    }

    protected Object createNotEqualInstance() throws Exception {
        return new SeismogramIterator(TEST_ITERATOR,
                                      TestUtils.createOtherSeisArray());
    }

    public void testHasNext(){
        assertTrue(iterator.hasNext());
    }

    public void testHasNextPastEnd(){
        MicroSecondDate slightlyBeforeNow = new MicroSecondDate();
        try {
            Thread.sleep(40);
        }
        catch (InterruptedException e) {
        }
        MicroSecondDate now = new MicroSecondDate();
        iterator.setTimeRange(new MicroSecondTimeRange(slightlyBeforeNow, now));
        assertFalse(iterator.hasNext());
    }

    public void testHasNextAtLastPoint(){
        while(iterator.hasNext()){
            iterator.next();
        }
        assertFalse(iterator.hasNext());
    }

    public void testNextOnGappySeismograms(){
        LocalSeismogramImpl one = SimplePlotUtil.createSpike(new MicroSecondDate(0));
        LocalSeismogramImpl two = SimplePlotUtil.createSpike(new MicroSecondDate(60000000));
        TimeInterval difference = two.getBeginTime().difference(one.getEndTime());
        TimeInterval sampling = (TimeInterval)two.getSampling().getPeriod().convertTo(UnitImpl.MICROSECOND);
        double gapPoints = difference.divideBy(sampling).getValue();
        int gapSize = (int)gapPoints;
        LocalSeismogramImpl[] gappySeismograms = {one, two};
        SeismogramIterator gappyIterator = new SeismogramIterator(TEST_ITERATOR,
                                                                  gappySeismograms);
        while(gappyIterator.hasNext()){
            Object next = gappyIterator.next();
            if(next == SeismogramIterator.NOT_A_NUMBER){
                gapSize -= 1;
            }
        }
        assertEquals(0,gapSize);
    }


    private static final String TEST_ITERATOR = "TEST ITERATOR";

    private SeismogramIterator iterator;

}

