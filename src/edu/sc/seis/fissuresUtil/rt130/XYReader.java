package edu.sc.seis.fissuresUtil.rt130;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import edu.iris.Fissures.Location;
import edu.iris.Fissures.LocationType;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.UnitImpl;

public class XYReader {

    /**
     * Populates a map with station locations from the Reader
     * 
     * Expects the reader to point to data with on station per line with float
     * representations of longitude, latitude, elevation and then the station
     * code seperated by spaces. an optional comment can be appended after the
     * code prefixed with a #
     * 
     * ie
     * 
     * <pre>
     *      -118.3516 37.6599 1721. SNP27 
     *      -118.6468 37.6832 2457. SNP36 
     *      -120.2526 38.3331 1754. SNP95 # Provisional
     * </pre>
     * 
     */
    public static Map read(BufferedReader reader) throws IOException {
        Map locs = new HashMap();
        String line;
        while((line = reader.readLine()) != null) {
            StringTokenizer st = new StringTokenizer(line, " ");
            Location loc = new Location();
            String stationCode = st.nextToken();
            loc.longitude = Float.parseFloat(stationCode);
            loc.latitude = Float.parseFloat(stationCode);
            loc.elevation = new QuantityImpl(Float.parseFloat(stationCode),
                                             UnitImpl.METER);
            loc.depth = new QuantityImpl(0, UnitImpl.METER);
            loc.type = LocationType.GEOGRAPHIC;
            locs.put(stationCode, loc);
            logger.debug("Read in location for " + stationCode + " "
                    + toString(loc));
        }
        return locs;
    }

    public static String toString(Location loc) {
        return "Lat: " + loc.latitude + " Long: " + loc.longitude + " Elev: "
                + loc.elevation;
    }

    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(XYReader.class);
}
