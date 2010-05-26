package edu.sc.seis.fissuresUtil.rt130;

import edu.iris.Fissures.model.ISOTime;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import junit.framework.TestCase;

public class FileNameParserTest extends TestCase {

    public void setUp() {}

    public void testGetBeginTime() {
        MicroSecondDate beginTime = FileNameParser.getBeginTime("2005001",
                                                                "195714270_0036EE80");
        MicroSecondDate trueBeginTime = new ISOTime(2005, 1, 19, 57, 14).getDate();
        assertEquals(trueBeginTime, beginTime);
    }

    public void testGetLengthOfData() throws RT130FormatException {
        TimeInterval length = FileNameParser.getLengthOfData("195714270_0036EE80");
        assertEquals(new QuantityImpl(1, UnitImpl.HOUR),
                     length.convertTo(UnitImpl.HOUR));
    }
}
