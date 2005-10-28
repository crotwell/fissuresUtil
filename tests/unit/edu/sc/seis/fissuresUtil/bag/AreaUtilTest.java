package edu.sc.seis.fissuresUtil.bag;

import edu.iris.Fissures.Location;
import edu.iris.Fissures.LocationType;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.UnitImpl;
import junit.framework.TestCase;


public class AreaUtilTest extends TestCase {

    public AreaUtilTest() {
        super();
        // TODO Auto-generated constructor stub
    }

    public AreaUtilTest(String arg0) {
        super(arg0);
        // TODO Auto-generated constructor stub
    }
    
    public void testInPolygon() {
        QuantityImpl el = new QuantityImpl(0, UnitImpl.METER);
        QuantityImpl depth = new QuantityImpl(0, UnitImpl.METER);
        Location[] bounds = new Location[] {
                                            new Location(1,1,el, depth, LocationType.GEOGRAPHIC),
                                            new Location(2,3,el, depth, LocationType.GEOGRAPHIC),
                                            new Location(2,2,el, depth, LocationType.GEOGRAPHIC),
                                            new Location(-1,1,el, depth, LocationType.GEOGRAPHIC),
                                            new Location(-2,-1,el, depth, LocationType.GEOGRAPHIC),
                                            new Location(1,-1,el, depth, LocationType.GEOGRAPHIC)
        };
        Location point;
        point=new Location(0,0,el, depth, LocationType.GEOGRAPHIC);
        assertTrue("in 0,0", AreaUtil.inArea(bounds, point));
        point=new Location(4,4,el, depth, LocationType.GEOGRAPHIC);
        assertFalse("out 4,4", AreaUtil.inArea(bounds, point));
        point=new Location(1,1,el, depth, LocationType.GEOGRAPHIC);
        assertTrue("on boundary 1,1", AreaUtil.inArea(bounds, point));
        point=new Location(3.00001f, 2.00001f,el, depth, LocationType.GEOGRAPHIC);
        assertFalse("barely out 3+,2+", AreaUtil.inArea(bounds, point));
    }
}
