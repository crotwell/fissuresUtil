package edu.sc.seis.fissuresUtil.bag;

import edu.iris.Fissures.model.SamplingImpl;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.display.SimplePlotUtil;
import junit.framework.TestCase;

// JUnitDoclet end import

/**
 * Generated by JUnitDoclet, a tool provided by ObjectFab GmbH under LGPL.
 * Please see www.junitdoclet.org, www.gnu.org and www.objectfab.de for
 * informations about the tool, the licence and the authors.
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

	public edu.sc.seis.fissuresUtil.bag.Calculus createInstance()
			throws Exception {
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

	public void testFloatIntegrate() throws Exception {
		float[] diff = new float[10];
		for (int i = 0; i < diff.length; i++) {
			diff[i] = 1;
		}
		LocalSeismogramImpl diffSeis = SimplePlotUtil.createTestData("est", new int[0]);
		diffSeis.data.flt_values(diff);
		diffSeis.num_points = diff.length;
		diffSeis.sampling_info = new SamplingImpl(1, new TimeInterval(1, UnitImpl.SECOND));
		LocalSeismogramImpl intSeis = Calculus.integrate(diffSeis);
		float[] intData = intSeis.get_as_floats();
		for (int i = 1; i < diff.length; i++) {
			assertEquals(""+i, i,intData[i], 0.001f);
		}
	}
	public void testIntegrate() throws Exception {
		int[] diff = new int[10];
		for (int i = 0; i < diff.length; i++) {
			diff[i] = 1;
		}
		LocalSeismogramImpl diffSeis = SimplePlotUtil.createTestData("est", diff);
		diffSeis.sampling_info = new SamplingImpl(1, new TimeInterval(1, UnitImpl.SECOND));
		LocalSeismogramImpl intSeis = Calculus.integrate(diffSeis);
		float[] intData = intSeis.get_as_floats();
		for (int i = 1; i < diff.length; i++) {
			assertEquals(""+i, (float)i,intData[i]);
		}
	}

}
