package edu.sc.seis.fissuresUtil.bag;

import junit.framework.TestCase;
// JUnitDoclet begin import
import edu.sc.seis.fissuresUtil.bag.Calculus;
// JUnitDoclet end import

/**
* Generated by JUnitDoclet, a tool provided by
* ObjectFab GmbH under LGPL.
* Please see www.junitdoclet.org, www.gnu.org
* and www.objectfab.de for informations about
* the tool, the licence and the authors.
*/


public class CalculusTest
// JUnitDoclet begin extends_implements
extends TestCase
// JUnitDoclet end extends_implements
{
  // JUnitDoclet begin class
  edu.sc.seis.fissuresUtil.bag.Calculus calculus = null;
  // JUnitDoclet end class
  
  public CalculusTest(String name) {
    // JUnitDoclet begin method CalculusTest
    super(name);
    // JUnitDoclet end method CalculusTest
  }
  
  public edu.sc.seis.fissuresUtil.bag.Calculus createInstance() throws Exception {
    // JUnitDoclet begin method testcase.createInstance
    return new edu.sc.seis.fissuresUtil.bag.Calculus();
    // JUnitDoclet end method testcase.createInstance
  }
  
  protected void setUp() throws Exception {
    // JUnitDoclet begin method testcase.setUp
    super.setUp();
    calculus = createInstance();
    // JUnitDoclet end method testcase.setUp
  }
  
  protected void tearDown() throws Exception {
    // JUnitDoclet begin method testcase.tearDown
    calculus = null;
    super.tearDown();
    // JUnitDoclet end method testcase.tearDown
  }
  
  public void testDifference() throws Exception {
    // JUnitDoclet begin method difference
    // JUnitDoclet end method difference
  }
  
  public void testDifferentiate() throws Exception {
    // JUnitDoclet begin method differentiate
    // JUnitDoclet end method differentiate
  }
  
  public void testIntegrate() throws Exception {
    // JUnitDoclet begin method integrate
    // JUnitDoclet end method integrate
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
    junit.textui.TestRunner.run(CalculusTest.class);
    // JUnitDoclet end method testcase.main
  }
}