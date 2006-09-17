package edu.sc.seis.fissuresUtil.rt130;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Map;
import junit.framework.TestCase;
import edu.iris.Fissures.Location;
import edu.iris.Fissures.LocationType;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.UnitImpl;

public class XYReaderTest extends TestCase {

    public void testEmpty() throws IOException {
        assertFalse(readFromString("").containsKey("SNEP46"));
    }

    public void testSimple() throws IOException {
        Map locs = readFromString(SNP_56);
        test56(locs);
        assertFalse(locs.containsKey("NONSENSE"));
    }

    public void testMultipleLocs() throws IOException {
        Map locs = createMultiStationMap();
        assertTrue(locs.size() == 2);
        test56(locs);
        Location secondLoc = (Location)locs.get("SNP16");
        assertEquals(120.3, secondLoc.longitude, 0.0001);
        assertEquals(7.41211, secondLoc.latitude, 0.0001);
        assertEquals(new QuantityImpl(-12, UnitImpl.METER), secondLoc.elevation);
        checkXYLocationInvariants(secondLoc);
    }

    private void checkXYLocationInvariants(Location loc) {
        assertEquals(LocationType.GEOGRAPHIC, loc.type);
        assertEquals(new QuantityImpl(0, UnitImpl.METER), loc.depth);
    }

    private void test56(Map locs) {
        assertTrue(locs.containsKey("SNP56"));
        Location loc = (Location)locs.get("SNP56");
        assertEquals(-119.1693, loc.longitude, 0.0001);
        assertEquals(37.9739, loc.latitude, 0.0001);
        assertEquals(new QuantityImpl(2944, UnitImpl.METER), loc.elevation);
        checkXYLocationInvariants(loc);
    }
    
    public Map createDefault() throws IOException{
        return readFromString(SNP_56);
    }

    private static Map readFromString(String s) throws IOException {
        return XYReader.read(new BufferedReader(new StringReader(s)));
    }

    private static final String SNP_56 = "-119.1693 37.9739 2944. SNP56\n";

    public static Map createMultiStationMap() throws IOException {
        return readFromString(SNP_56
                + "120.3 7.41211 -12. SNP16 # This is a comment\n");
    }
}
