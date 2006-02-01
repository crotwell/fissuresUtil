package edu.sc.seis.fissuresUtil.bag;

import junit.framework.TestCase;
import edu.iris.Fissures.BoxArea;
import edu.iris.Fissures.Location;
import edu.iris.Fissures.LocationType;
import edu.iris.Fissures.PointDistanceArea;
import edu.iris.Fissures.model.BoxAreaImpl;
import edu.iris.Fissures.model.GlobalAreaImpl;
import edu.iris.Fissures.model.PointDistanceAreaImpl;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.UnitImpl;

public class AreaUtilTest extends TestCase {

    public void testSimpleConvertToBox() {
        BoxArea b = new BoxAreaImpl(-90, 90, -180, 180);
        assertEquals(b, AreaUtil.makeContainingBox(b));
        assertEquals(b, AreaUtil.makeContainingBox(new GlobalAreaImpl()));
    }

    public void testDonutConvertToBox() {
        PointDistanceArea meridian = new PointDistanceAreaImpl(0,
                                                               0,
                                                               ZERO,
                                                               TEN_DEG);
        assertEquals(new BoxAreaImpl(-10, 10, -10, 10),
                     AreaUtil.makeContainingBox(meridian));
        PointDistanceArea westOfDateLine = new PointDistanceAreaImpl(0,
                                                                     175,
                                                                     ZERO,
                                                                     TEN_DEG);
        assertEquals(new BoxAreaImpl(-10, 10, 165, -175),
                     AreaUtil.makeContainingBox(westOfDateLine));
        PointDistanceArea eastOfDateLine = new PointDistanceAreaImpl(0,
                                                                     -175,
                                                                     ZERO,
                                                                     TEN_DEG);
        assertEquals(new BoxAreaImpl(-10, 10, 175, -165),
                     AreaUtil.makeContainingBox(eastOfDateLine));
    }

    public void testInPolygon() {
        QuantityImpl el = new QuantityImpl(0, UnitImpl.METER);
        QuantityImpl depth = new QuantityImpl(0, UnitImpl.METER);
        Location[] bounds = new Location[] {new Location(1,
                                                         1,
                                                         el,
                                                         depth,
                                                         LocationType.GEOGRAPHIC),
                                            new Location(2,
                                                         3,
                                                         el,
                                                         depth,
                                                         LocationType.GEOGRAPHIC),
                                            new Location(2,
                                                         2,
                                                         el,
                                                         depth,
                                                         LocationType.GEOGRAPHIC),
                                            new Location(-1,
                                                         1,
                                                         el,
                                                         depth,
                                                         LocationType.GEOGRAPHIC),
                                            new Location(-2,
                                                         -1,
                                                         el,
                                                         depth,
                                                         LocationType.GEOGRAPHIC),
                                            new Location(1,
                                                         -1,
                                                         el,
                                                         depth,
                                                         LocationType.GEOGRAPHIC)};
        Location point;
        point = new Location(0, 0, el, depth, LocationType.GEOGRAPHIC);
        assertTrue("in 0,0", AreaUtil.inArea(bounds, point));
        point = new Location(4, 4, el, depth, LocationType.GEOGRAPHIC);
        assertFalse("out 4,4", AreaUtil.inArea(bounds, point));
        point = new Location(1, 1, el, depth, LocationType.GEOGRAPHIC);
        assertTrue("on boundary 1,1", AreaUtil.inArea(bounds, point));
        point = new Location(3.00001f,
                             2.00001f,
                             el,
                             depth,
                             LocationType.GEOGRAPHIC);
        assertFalse("barely out 3+,2+", AreaUtil.inArea(bounds, point));
    }

    public static final QuantityImpl ZERO = new QuantityImpl(0, UnitImpl.DEGREE);

    public static final QuantityImpl TEN_DEG = new QuantityImpl(10,
                                                                UnitImpl.DEGREE);
}
