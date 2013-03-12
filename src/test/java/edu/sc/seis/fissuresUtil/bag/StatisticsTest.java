package edu.sc.seis.fissuresUtil.bag;


import org.junit.Test;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;


public class StatisticsTest {
    edu.sc.seis.fissuresUtil.bag.Statistics[] stat = null;

    int size = 10000;
    int[] intTestData;
    short[] shortTestData;
    float[] floatTestData;
    double[] doubleTestData;

    public edu.sc.seis.fissuresUtil.bag.Statistics[] createInstanceArray() throws Exception {
        intTestData = new int[size];
        shortTestData = new short[size];
        floatTestData = new float[size];
        doubleTestData = new double[size];
        for (short i=0; i<size; i++) {
            shortTestData[i] = i;
            intTestData[i] = i;
            floatTestData[i] = i;
            doubleTestData[i] = i;
        } // end of for (int i=0; i<intTestData.length; i++)
        Statistics[] out = new edu.sc.seis.fissuresUtil.bag.Statistics[4];
        out[0] = new Statistics(shortTestData);
        out[1] = new Statistics(intTestData);
        out[2] = new Statistics(floatTestData);
        out[3] = new Statistics(doubleTestData);
        return out;
    }



    protected void setUp() throws Exception {
        stat = createInstanceArray();
    }

    protected void tearDown() throws Exception {
        stat = null;
    }

    public void testMin() throws Exception {
        for ( int i = 0; i<stat.length; i++) {
            assertEquals("stat["+i+"]",  0, stat[i].min(), 0.0000001);
        } // end of for ()

    }

    public void testMax() throws Exception {
        for ( int i = 0; i<stat.length; i++) {
            assertEquals("stat["+i+"]",  stat[i].getLength()-1, stat[i].max(), 0.0000001);
        } // end of for ()
    }

    public void testMean() throws Exception {
        for ( int i = 0; i<stat.length; i++) {
            assertEquals("stat["+i+"]", (stat[i].getLength()-1.0)/2, stat[i].mean(), 0.0000001);
        } // end of for ()
    }


    public void testLinearLeastSquares() throws Exception {
        for ( int i = 0; i<stat.length; i++) {
            double[] out = stat[i].linearLeastSquares();
            assertEquals("stat["+i+"]", 0, out[0], 0.0000001*size);
            assertEquals("stat["+i+"]", 1, out[1], 0.0000001*size);
        } // end of for ()
    }

    public void testLinearLeastSquaresConst() throws Exception {
        int[] data = new int[size];
        for (int i = 0; i < data.length; i++) {
            data[i] = 1;
        }
        Statistics stat = new Statistics(data);
        double[] out = stat.linearLeastSquares();
        assertEquals("stat0", 1, out[0], 0.0000001);
        assertEquals("stat1", 0, out[1], 0.0000001);
    }

    public void testLinearLeastSquaresDown() throws Exception {
        int[] data = new int[size];
        for (int i = 0; i < data.length; i++) {
            data[i] = -2*i+1;
        }
        Statistics stat = new Statistics(data);
        double[] out = stat.linearLeastSquares();
        assertEquals("stat0", 1, out[0], 0.0000001);
        assertEquals("stat1", -2, out[1], 0.0000001);
    }

    public void testLinearLeastSquaresBumps() throws Exception {
        // linear least squares applet
        // http://www.dartmouth.edu/~chemlab/info/resources/linear/linear.html
        // slope = 4.24
        // intercept = 9.4
        short[] bumps = new short[16];
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
        Statistics stat = new Statistics(bumps);
        double[] out = stat.linearLeastSquares();
        assertEquals("stat bumps 0", 9.4, out[0], 0.1);
        assertEquals("stat bumps 1", 4.24, out[1], 0.01);

    }

    public void testSumValues() {
        for ( int i = 0; i<stat.length; i++) {
            assertEquals( size, stat[i].getLength(), 0.0000001);
        } // end of for ()
    }

    public void testGetLength() throws Exception {
        for ( int i = 0; i<stat.length; i++) {
            assertEquals( size, stat[i].getLength(), 0.0000001);
        } // end of for ()
    }


    public void testBinarySum() throws Exception {
        for ( int i = 0; i<stat.length; i++) {
            int n=stat[i].getLength()-1;
            double out = stat[i].binarySum(0, stat[i].getLength());
            assertEquals("BinarySum", n*(n+1)/2, out, 0.0000001);
        } // end of for ()

    }

    public void testBinaryIndexSum() throws Exception {
        // JUnitDoclet begin method binaryIndexSum
        for ( int i = 0; i<stat.length; i++) {
            int n=stat[i].getLength()-1;
            double sumSquare = 1.0*n*(n+1)*(2*n+1)/6;
            assertEquals("stat["+i+"]", sumSquare, stat[i].binaryIndexSum(0, stat[i].getLength()), 0.00001*sumSquare);
        } // end of for ()
        // JUnitDoclet end method binaryIndexSum
    }

    public void testACF_PACF() throws Exception {
        int[] testSeries = new int[10];
        testSeries[0] = 13;
        testSeries[1] = 8;
        testSeries[2] = 15;
        testSeries[3] = 4;
        testSeries[4] = 4;
        testSeries[5] = 12;
        testSeries[6] = 11;
        testSeries[7] = 7;
        testSeries[8] = 14;
        testSeries[9] = 12;
        Statistics s = new Statistics(testSeries);
        //System.out.println("Mean = "+s.mean());
        //System.out.println("Variance = "+s.var());
        double[] testACF = s.acf(5);
        for (int i=0; i<testACF.length; i++) {
           // System.out.println("acf "+i+" = "+testACF[i]);
        }
        double[] testPACF = s.pacf(5);
        for (int i=0; i<testPACF.length; i++) {
        //    System.out.println("pacf "+i+" = "+testPACF[i]);
        }
    }

    @Test(expected=ArithmeticException.class)
    public void testCorrelationNoVariance() {
        Statistics stat;
        double[] testData = new double[10];
        double[] otherData = new double[10];
        
        for(int i = 0; i < otherData.length; i++) {
            otherData[i] = 1;
            testData[i] = 1;
        }
        stat = new Statistics(testData);
        assertEquals("no variation, so no correlation", 0, stat.correlation(otherData), 0.00001);
    }
    public void testCorrelation() {
        Statistics stat;
        double[] testData = new double[10];
        double[] otherData = new double[10];
        
        for(int i = 0; i < otherData.length; i++) {
            otherData[i] = i;
            testData[i] = i;
        }
        stat = new Statistics(testData);
        assertEquals("perfect correlation", 1, stat.correlation(otherData), 0.00001);
        

        for(int i = 0; i < otherData.length; i++) {
            otherData[i] = i;
            testData[i] = -i;
        }
        stat = new Statistics(testData);
        assertEquals("perfect anticorrelation", -1, stat.correlation(otherData), 0.00001);
        

        for(int i = 0; i < otherData.length; i++) {
            otherData[i] = i;
            testData[i] = i % 2;
        }
        stat = new Statistics(testData);
        assertEquals("uncorrelated", 0, stat.correlation(otherData), 0.00001);
    }

}
