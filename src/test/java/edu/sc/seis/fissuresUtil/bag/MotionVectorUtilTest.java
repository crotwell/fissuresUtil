package edu.sc.seis.fissuresUtil.bag;

import junit.framework.TestCase;

/**
* Generated by JUnitDoclet, a tool provided by
* ObjectFab GmbH under LGPL.
* Please see www.junitdoclet.org, www.gnu.org
* and www.objectfab.de for informations about
* the tool, the licence and the authors.
*/


public class MotionVectorUtilTest
// JUnitDoclet begin extends_implements
extends TestCase
// JUnitDoclet end extends_implements
{
  // JUnitDoclet begin class
  edu.sc.seis.fissuresUtil.bag.MotionVectorUtil motionvectorutil = null;
  // JUnitDoclet end class
  
  public MotionVectorUtilTest(String name) {
    // JUnitDoclet begin method MotionVectorUtilTest
    super(name);
    // JUnitDoclet end method MotionVectorUtilTest
  }
  
  public edu.sc.seis.fissuresUtil.bag.MotionVectorUtil createInstance() throws Exception {
    // JUnitDoclet begin method testcase.createInstance
    return new edu.sc.seis.fissuresUtil.bag.MotionVectorUtil();
    // JUnitDoclet end method testcase.createInstance
  }
  
  protected void setUp() throws Exception {
    // JUnitDoclet begin method testcase.setUp
    super.setUp();
    motionvectorutil = createInstance();
    // JUnitDoclet end method testcase.setUp
  }
  
  protected void tearDown() throws Exception {
    // JUnitDoclet begin method testcase.tearDown
    motionvectorutil = null;
    super.tearDown();
    // JUnitDoclet end method testcase.tearDown
  }
  
  public void testCreate() throws Exception {
    // JUnitDoclet begin method create
    // JUnitDoclet end method create
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
    junit.textui.TestRunner.run(MotionVectorUtilTest.class);
    // JUnitDoclet end method testcase.main
  }
}
