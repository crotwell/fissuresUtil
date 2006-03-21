/*
 * Created on Jul 20, 2004
 */
package edu.sc.seis.fissuresUtil.cache;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import edu.iris.Fissures.Quantity;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.NoPreferredOrigin;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.display.ParseRegions;
import edu.sc.seis.fissuresUtil.display.UnitDisplayUtil;

/**
 * @author oliverpa
 */
public class EventUtil {

    /**
     * This gets around the NoPreferredOrigin exception
     */
    public static Origin extractOrigin(EventAccessOperations ev) {
        try {
            return ev.get_preferred_origin();
        } catch(NoPreferredOrigin e) {
            logger.info("No preferred origin in event.  Trying get_origins instead");
            Origin[] oArray = ev.get_origins();
            if(oArray.length > 0) {
                return oArray[0];
            }
            throw new RuntimeException("No preferred origin", e);
        }
    }

    /**
     * @ returns a string for the form "Event: Location | Time | Magnitude |
     *   Depth"
     */
    public static String getEventInfo(EventAccessOperations event) {
        return getEventInfo(event, NO_ARG_STRING);
    }

    public static String getEventInfo(EventAccessOperations event, String format) {
        return getEventInfo(event,
                            format,
                            new SimpleDateFormat("MM/dd/yyyy HH:mm:ss z"));
    }

    /**
     * @ formats a string for the given event. To insert information about a
     *   certain item magic strings are used in the format string Magic Strings
     *   LOC adds the location of the event TIME adds the event time MAG adds
     *   event magnitude DEPTH adds the depth For example the string "Event: " +
     *   LOC + " | " + TIME + " | Mag: " + MAG + " | Depth: " + DEPTH + " " +
     *   DEPTH_UNIT produces the same thing as the no format call to
     *   getEventInfo
     */
    public static String getEventInfo(EventAccessOperations event,
                                      String format,
                                      DateFormat sdf) {
        Origin origin = extractOrigin(event);
        StringBuffer buf = new StringBuffer(format);
        int index = buf.indexOf(LOC);
        if(index != -1) {
            // Get geographic name of origin
            ParseRegions regions = ParseRegions.getInstance();
            String location = regions.getGeographicRegionName(event.get_attributes().region.number);
            buf.delete(index, index + LOC.length());
            buf.insert(index, location);
        }
        return getOriginInfo(origin, buf.toString(), sdf);
    }

    /**
     * @ returns a string for the form "Event: Location | Time | Magnitude |
     *   Depth"
     */
    public static String getOriginInfo(Origin origin) {
        return getOriginInfo(origin, NO_ARG_STRING);
    }

    public static String getOriginInfo(Origin origin, String format) {
        return getOriginInfo(origin,
                             format,
                             new SimpleDateFormat("MM/dd/yyyy HH:mm:ss z"));
    }

    public static String getOriginInfo(Origin origin,
                                       String format,
                                       DateFormat sdf) {
        // Get Date and format it accordingly
        MicroSecondDate msd = new MicroSecondDate(origin.origin_time);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        String originTimeString = sdf.format(msd);
        // Get Magnitude
        float mag = Float.NaN;
        if(origin.magnitudes.length > 0) {
            mag = origin.magnitudes[0].value;
        }
        // get depth
        Quantity depth = origin.my_location.depth;
        float latitude = origin.my_location.latitude;
        float longitude = origin.my_location.longitude;
        StringBuffer buf = new StringBuffer(format);
        for(int i = 0; i < magicStrings.length; i++) {
            int index = buf.indexOf(magicStrings[i]);
            if(index != -1) {
                buf.delete(index, index + magicStrings[i].length());
                if(magicStrings[i].equals(TIME)) {
                    buf.insert(index, originTimeString);
                } else if(magicStrings[i].equals(MAG)) {
                    if(Float.isNaN(mag)) {
                        buf.insert(index, "...");
                    } else {
                        buf.insert(index, mag);
                    }
                } else if(magicStrings[i].equals(DEPTH)) {
                    buf.insert(index, depthFormatter.format(depth.value));
                } else if(magicStrings[i].equals(DEPTH_UNIT)) {
                    buf.insert(index,
                               UnitDisplayUtil.getNameForUnit((UnitImpl)depth.the_units));
                } else if(magicStrings[i].equals(LAT)) {
                    buf.insert(index, latitude);
                } else if(magicStrings[i].equals(LON)) {
                    buf.insert(index, longitude);
                }
            }
        }
        return buf.toString();
    }

    private static DecimalFormat depthFormatter = new DecimalFormat("###0.00");

    public static final String LOC = "LOC", TIME = "TIME", MAG = "MAG",
            DEPTH = "DEPTH", DEPTH_UNIT = "DEPTH_UNIT", LAT = "LAT",
            LON = "LON";

    private static final String[] magicStrings = {LOC,
                                                  TIME,
                                                  MAG,
                                                  DEPTH,
                                                  DEPTH_UNIT,
                                                  LAT,
                                                  LON};

    public static final String NO_ARG_STRING = "Event: " + LOC + " | " + TIME
            + " | Mag: " + MAG + " | Depth " + DEPTH + " " + DEPTH_UNIT + "| ("
            + LAT + ", " + LON + ")";

    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(EventUtil.class);
}
