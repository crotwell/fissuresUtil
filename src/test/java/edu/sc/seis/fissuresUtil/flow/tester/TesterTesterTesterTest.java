package edu.sc.seis.fissuresUtil.flow.tester;

import junit.framework.TestCase;

public abstract class TesterTesterTesterTest extends TestCase {

    public void testNull() {
        assertTrue(getTester().test(null) instanceof NoTestSubject);
    }

    public void testUnknownObject() {
        assertTrue(getTester().test(this) instanceof NoTestSubject);
    }

    public abstract Tester getTester();
}
