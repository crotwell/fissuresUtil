package edu.sc.seis.fissuresUtil.bag;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * StatisticsTest.java
 *
 *
 * Created: Fri Nov 15 12:02:46 2002
 *
 * @author <a href="mailto:www@seis.sc.edu">Philip Crotwell</a>
 * @version
 */

public class StatisticsTest extends TestCase {
    public StatisticsTest (final String name){
	super(name);
    }
    
    /**
     * Returns the test suite.
     */
     public static Test suite() {
         return new TestSuite(StatisticsTest.class);
     }

    /**
     * Set up common objects used for all tests.
     */
    protected void setUp() throws Exception {
        super.setUp();
        int size = 4;
	intTestData = new int[size];
	shortTestData = new short[size];
	floatTestData = new float[size];
	doubleTestData = new double[size];
	for (int i=0; i<size; i++) {
	    shortTestData[i] = i;
	    intTestData[i] = i;
	    floatTestData[i] = i;
	    doubleTestData[i] = i;
	} // end of for (int i=0; i<intTestData.length; i++)
    }

    public void testMinShort() {
	Statistics stat = new Statistics(shortTestData);
	assertEquals( 0, stat.min(), 0.0000001);
    }

    public void testMaxShort() {
	Statistics stat = new Statistics(shortTestData);
	assertEquals(shortTestData.length-1, stat.max(), 0.000001);
    }

    public void testMeanShort() {
	Statistics stat = new Statistics(shortTestData);
	assertEquals((shortTestData.length-1.0)/2, stat.mean(), 0.0000001);
    }


    public void testMinFloat() {
	Statistics stat = new Statistics(floatTestData);
	assertEquals( 0, stat.min(), 0.0000001);
    }

    public void testMaxFloat() {
	Statistics stat = new Statistics(floatTestData);
	assertEquals(floatTestData.length-1, stat.max(), 0.000001);
    }

    public void testMeanFloat() {
	Statistics stat = new Statistics(floatTestData);
	assertEquals((floatTestData.length-1.0)/2, stat.mean(), 0.0000001);
    }


    public void testMinDouble() {
	Statistics stat = new Statistics(doubleTestData);
	assertEquals( 0, stat.min(), 0.0000001);
    }

    public void testMaxDouble() {
	Statistics stat = new Statistics(doubleTestData);
	assertEquals(doubleTestData.length-1, stat.max(), 0.000001);
    }

    public void testMeanDouble() {
	Statistics stat = new Statistics(doubleTestData);
	assertEquals((doubleTestData.length-1.0)/2, stat.mean(), 0.0000001);
    }


    public void testMinInt() {
	Statistics stat = new Statistics(intTestData);
	assertEquals( 0, stat.min(), 0.0000001);
    }

    public void testMaxInt() {
	Statistics stat = new Statistics(intTestData);
	assertEquals(intTestData.length-1, stat.max(), 0.000001);
    }

    public void testMeanInt() {
	Statistics stat = new Statistics(intTestData);
	assertEquals((intTestData.length-1.0)/2, stat.mean(), 0.0000001);
    }

    private short[] shortTestData;

    private int[] intTestData;

    private float[] floatTestData;

    private double[] doubleTestData;

}// StatisticsTest
