package edu.sc.seis.fissuresUtil;

import junit.framework.TestCase;

/**
 * @author groves Created on Apr 1, 2005
 */
public class DoubleIntConversionTest extends TestCase {

    public void testDivision() {
        int intDivisor = 2;
        int intNumerator = 1;
        double doubleDivisor = 2d;
        double doubleNumerator = 1d;
        assertEquals(0, intNumerator / intDivisor, 0);
        assertEquals(.5, intNumerator / doubleDivisor, 0);
        assertEquals(.5, doubleNumerator / intDivisor, 0);
        assertEquals(.5, doubleNumerator / doubleDivisor, 0);
    }

    public void testMultiplication() {
        int first = 1;
        double second = .5;
        assertEquals(.5, first * second, 0);
        assertEquals(.5, second * first, 0);
    }
}