package edu.sc.seis.fissuresUtil.bag;

import junit.framework.TestCase;
import edu.sc.seis.fissuresUtil.bag.RTrend;


public class RTrendTest extends TestCase  {

    edu.sc.seis.fissuresUtil.bag.RTrend rtrend = null;
    int[] intTestData;
    short[] shortTestData;
    float[] floatTestData;
    double[] doubleTestData;

    public RTrendTest(String name)  {
        super(name);
    }

    public edu.sc.seis.fissuresUtil.bag.RTrend createInstance() throws Exception  {
        return new edu.sc.seis.fissuresUtil.bag.RTrend();
    }

    protected void setUp() throws Exception  {
        super.setUp();
        short size = 40;
        intTestData = new int[size];
        shortTestData = new short[size];
        floatTestData = new float[size];
        doubleTestData = new double[size];
        for (short i=0; i<size; i++)  {
            shortTestData[i] = i;
            intTestData[i] = i;
            floatTestData[i] = i;
            doubleTestData[i] = i;
        } // end of for (int i=0; i<intTestData.length; i++)
        rtrend = createInstance();
    }

    protected void tearDown() throws Exception  {
        rtrend = null;
        super.tearDown();
    }

    public void testApplyShort() throws Exception  {
        short[] sOut = rtrend.apply(shortTestData);
        assertEquals("short", 0, sOut[0]);
    }

    public void testApplyInt() throws Exception  {
        int[] iOut = rtrend.apply(intTestData);
        assertEquals("int", 0, iOut[0]);
    }

    public void testApplyFloat() throws Exception  {
        float[] fOut = rtrend.apply(floatTestData);
        assertEquals("float", 0, fOut[0], 0.0000001);
    }

    public void testApplyDouble() throws Exception  {
        double[] dOut = rtrend.apply(doubleTestData);
        assertEquals("double", 0, dOut[0], 0.000001);
    }

}
