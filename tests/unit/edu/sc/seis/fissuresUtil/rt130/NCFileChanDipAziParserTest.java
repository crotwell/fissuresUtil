package edu.sc.seis.fissuresUtil.rt130;

import edu.iris.Fissures.Orientation;
import edu.sc.seis.fissuresUtil.bag.OrientationUtil;
import junit.framework.TestCase;

public class NCFileChanDipAziParserTest extends TestCase {

    public void testParseDefault() {
        ChannelNameAndOrientation[] chanNameAndOrientation = NCFileChanDipAziParser.parse("default");
        assertEquals('Z', chanNameAndOrientation[0].getChannelName());
        assertTrue(OrientationUtil.areEqual(new Orientation(0, -90),
                                            chanNameAndOrientation[0].getOrientation()));
        assertEquals('N', chanNameAndOrientation[1].getChannelName());
        assertTrue(OrientationUtil.areEqual(new Orientation(0, 0),
                                            chanNameAndOrientation[1].getOrientation()));
        assertEquals('E', chanNameAndOrientation[2].getChannelName());
        assertTrue(OrientationUtil.areEqual(new Orientation(90, 0),
                                            chanNameAndOrientation[2].getOrientation()));
    }

    public void testParseNonDefault() {
        ChannelNameAndOrientation[] chanNameAndOrientation = NCFileChanDipAziParser.parse("1/-90/0:2/0/352:3/0/82");
        assertEquals('1', chanNameAndOrientation[0].getChannelName());
        assertTrue(OrientationUtil.areEqual(new Orientation(0, -90),
                                            chanNameAndOrientation[0].getOrientation()));
        assertEquals('2', chanNameAndOrientation[1].getChannelName());
        assertTrue(OrientationUtil.areEqual(new Orientation(352, 0),
                                            chanNameAndOrientation[1].getOrientation()));
        assertEquals('3', chanNameAndOrientation[2].getChannelName());
        assertTrue(OrientationUtil.areEqual(new Orientation(82, 0),
                                            chanNameAndOrientation[2].getOrientation()));
    }
}
