package edu.sc.seis.fissuresUtil.bag;

import junit.framework.TestCase;
// JUnitDoclet begin import
import edu.sc.seis.fissuresUtil.bag.RTrend;
// JUnitDoclet end import

/**
* Generated by JUnitDoclet, a tool provided by
* ObjectFab GmbH under LGPL.
* Please see www.junitdoclet.org, www.gnu.org
* and www.objectfab.de for informations about
* the tool, the licence and the authors.
*/


public class RTrendTest
// JUnitDoclet begin extends_implements
extends TestCase
// JUnitDoclet end extends_implements
{
  // JUnitDoclet begin class
    edu.sc.seis.fissuresUtil.bag.RTrend rtrend = null;
    int[] intTestData;
    short[] shortTestData;
    float[] floatTestData;
    double[] doubleTestData;

  // JUnitDoclet end class
  
  public RTrendTest(String name) {
    // JUnitDoclet begin method RTrendTest
    super(name);
    // JUnitDoclet end method RTrendTest
  }
  
  public edu.sc.seis.fissuresUtil.bag.RTrend createInstance() throws Exception {
    // JUnitDoclet begin method testcase.createInstance
    return new edu.sc.seis.fissuresUtil.bag.RTrend();
    // JUnitDoclet end method testcase.createInstance
  }
  
  protected void setUp() throws Exception {
    // JUnitDoclet begin method testcase.setUp
    super.setUp();
      short size = 4;
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
      rtrend = createInstance();
    // JUnitDoclet end method testcase.setUp
  }
  
  protected void tearDown() throws Exception {
    // JUnitDoclet begin method testcase.tearDown
    rtrend = null;
    super.tearDown();
    // JUnitDoclet end method testcase.tearDown
  }
  
  public void testApply() throws Exception {
    // JUnitDoclet begin method apply
      short[] sOut = rtrend.apply(shortTestData);
      assertEquals("short", 0, sOut[0]);
      int[] iOut = rtrend.apply(intTestData);
      assertEquals("int", 0, iOut[0]);
      float[] fOut = rtrend.apply(floatTestData);
      assertEquals("float", 0, fOut[0], 0.0000001);
      double[] dOut = rtrend.apply(doubleTestData);
      assertEquals("double", 0, dOut[0], 0.000001);
    // JUnitDoclet end method apply
  }
  
  public void testApplyInPlace() throws Exception {
    // JUnitDoclet begin method applyInPlace
    // JUnitDoclet end method applyInPlace
  }
  
  
  
  /**
  * JUnitDoclet moves marker to this method, if there is not match
  * for them in the regenerated code and if the marker is not empty.
  * This way, no test gets lost when regenerating after renaming.
  * Method testVault is supposed to be empty.
  */
  public void testVault() throws Exception {
    // JUnitDoclet begin method testcase.testVault
    // JUnitDoclet end method testcase.testVault
  }
  
  public static void main(String[] args) {
    // JUnitDoclet begin method testcase.main
    junit.textui.TestRunner.run(RTrendTest.class);
    // JUnitDoclet end method testcase.main
  }
}