package edu.sc.seis.fissuresUtil.database.problem;

import junit.framework.TestCase;

public class ProblemTest extends TestCase {

    public void testEqualsObject() {
        Problem problem1 = new Problem("SNP12",
                                       "flatline",
                                       "automatically generated");
        Problem problem2 = new Problem("SNP12",
                                       "flatline",
                                       "automatically generated");
        assertEquals(problem2, problem1);
        problem2 = new Problem("SNP12", "noisy", "automatically generated");
        assertFalse(problem2.equals(problem1));
    }
}
