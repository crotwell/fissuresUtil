package edu.sc.seis.fissuresUtil.bag;

import junit.framework.TestCase;


public class RTrendTest extends TestCase  {

    edu.sc.seis.fissuresUtil.bag.RTrend rtrend = null;
    int[] intTestData;
    short[] shortTestData;
    float[] floatTestData;
    double[] doubleTestData;
    short[] bumps;
    float bumpSlope;
    float bimpIntercept;

    public RTrendTest(String name)  {
        super(name);
    }

    public edu.sc.seis.fissuresUtil.bag.RTrend createInstance() throws Exception  {
        return new edu.sc.seis.fissuresUtil.bag.RTrend();
    }

    protected void setUp() throws Exception  {
        super.setUp();
        short size = 400;
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

        // linear least squares applet
        // http://www.dartmouth.edu/~chemlab/info/resources/linear/linear.html
        bumpSlope = 4.2367647f;
        bimpIntercept = 9.4117647f;
        bumps = new short[16];
        bumps[0] = 7;
        bumps[1] = -12;
        bumps[2] = 46;
        bumps[3] = 30;
        bumps[4] = 17;
        bumps[5] = 33;
        bumps[6] = 27;
        bumps[7] = 39;
        bumps[8] = 51;
        bumps[9] = 48;
        bumps[10] = 51;
        bumps[11] = 60;
        bumps[12] = 59;
        bumps[13] = 70;
        bumps[14] = 78;
        bumps[15] = 55;
        rtrend = createInstance();
    }

    protected void tearDown() throws Exception  {
        rtrend = null;
        super.tearDown();
    }

    public void testApplyBumps() throws Exception  {
        short[] sOut = rtrend.apply(bumps);
        for (int i = 0; i < sOut.length; i++) {
            assertEquals("short "+i, (short)Math.round(bumps[i]-(bumpSlope*i+bimpIntercept)), sOut[i]);
        }
    }

    public void testApplyIntBumps() throws Exception  {
        int[] iBumps = new int[bumps.length];
        for (int i = 0; i < bumps.length; i++) {
            iBumps[i] = bumps[i];
        }
        int[] sOut = rtrend.apply(iBumps);
        for (int i = 0; i < sOut.length; i++) {
            assertEquals("intBumps "+i, (int)Math.round(bumps[i]-(bumpSlope*i+bimpIntercept)), sOut[i]);
        }
    }
    public void testApplyShort() throws Exception  {
        short[] sOut = rtrend.apply(shortTestData);
        for (int i = 0; i < sOut.length; i++) {
            assertEquals("short", 0, sOut[i]);
        }
    }

    public void testApplyInt() throws Exception  {
        int[] iOut = rtrend.apply(intTestData);
        for (int i = 0; i < iOut.length; i++) {
            assertEquals("int", 0, iOut[i]);
        }
    }

    public void testApplyFloat() throws Exception  {
        float[] fOut = rtrend.apply(floatTestData);
        for (int i = 0; i < fOut.length; i++) {
            assertEquals("float", 0, fOut[i], 0.0000001);
        }
    }

    public void testApplyDouble() throws Exception  {
        double[] dOut = rtrend.apply(doubleTestData);
        for (int i = 0; i < dOut.length; i++) {
            assertEquals("double", 0, dOut[i], 0.000001);
        }
    }

}
