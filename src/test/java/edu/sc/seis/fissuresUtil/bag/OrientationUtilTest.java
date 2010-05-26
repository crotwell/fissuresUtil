package edu.sc.seis.fissuresUtil.bag;

import edu.iris.Fissures.Orientation;
import edu.sc.seis.TauP.SphericalCoords;
import junit.framework.TestCase;

public class OrientationUtilTest extends TestCase {

    public void testOrthognalPlusNintyDip() {
        for(float az = -180; az <= 180; az += .1) {
            Orientation one = new Orientation(az, 1);
            Orientation two = new Orientation(az + 90, 1);
            assertFalse(az + " to az plus 90, dip1 "
                                + SphericalCoords.distance(one.dip, one.azimuth, two.dip, two.azimuth),
                        OrientationUtil.areOrthogonal(one, two));
        }
    }

    public void testOrthognalPlusNinty() {
        for(float az = -180; az <= 180; az += .1) {
            Orientation one = new Orientation(az, 0);
            Orientation two = new Orientation(az + 90, 0);
            assertTrue(az + " to az plus 90  " + SphericalCoords.distance(one.dip, one.azimuth, two.dip, two.azimuth),
                       OrientationUtil.areOrthogonal(one, two));
        }
    }

    public void testAreOrthogonal() {
        float[] az = new float[] {0, 5, 89, 359};
        float[] dip = new float[] {3, 10, -80, 80};
        // test dip=0
        for(int i = 0; i < az.length; i++) {
            Orientation one = new Orientation(az[i], 0);
            Orientation two = new Orientation(az[i] + 90, 0);
            assertTrue(az[i] + " to az plus 90", OrientationUtil.areOrthogonal(one, two));
            two = new Orientation(az[i] - 90, 0);
            assertTrue(az[i] + " to az minus 90", OrientationUtil.areOrthogonal(one, two));
            two = new Orientation(az[i], 90);
            assertTrue(az[i] + " to dip 90", OrientationUtil.areOrthogonal(one, two));
            two = new Orientation(az[i], -90);
            assertTrue(az[i] + " to dip -90", OrientationUtil.areOrthogonal(one, two));
            two = new Orientation(az[i], 0);
            assertFalse(az[i] + " to az", OrientationUtil.areOrthogonal(one, two));
            two = new Orientation(az[i] + 89, 0);
            assertFalse(az[i] + " to az plus 89", OrientationUtil.areOrthogonal(one, two));
            two = new Orientation(az[i] - 89, 0);
            assertFalse(az[i] + " to az minus 89", OrientationUtil.areOrthogonal(one, two));
            for(int j = 0; j < dip.length; j++) {
                two = new Orientation(az[i], dip[j]);
                assertFalse(az[i] + " to az+90, dip=" + dip[j], OrientationUtil.areOrthogonal(one, two));
            }
        }
    }
}
