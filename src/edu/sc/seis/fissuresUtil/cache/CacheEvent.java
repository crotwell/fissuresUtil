package edu.sc.seis.fissuresUtil.cache;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import org.apache.log4j.Logger;

import edu.iris.Fissures.AuditElement;
import edu.iris.Fissures.NotImplemented;
import edu.iris.Fissures.Quantity;
import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.IfParameterMgr.ParameterComponent;
import edu.iris.Fissures.event.EventAttrImpl;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.display.ParseRegions;
import edu.sc.seis.fissuresUtil.display.UnitDisplayUtil;

/**
 * CacheEvent.java
 * 
 * 
 * Created: Mon Jan 8 16:33:52 2001
 * 
 * @author Philip Crotwell
 * @version
 */

public class CacheEvent implements EventAccessOperations {
    public CacheEvent(EventAttr attr, Origin[] origins, Origin preferred) {
        if (attr == null) { throw new IllegalArgumentException(
                "EventAttr cannot be null"); }
        if (origins == null) { throw new IllegalArgumentException(
                "origins cannot be null"); }
        this.attr = attr;
        this.origins = origins;
        this.preferred = preferred;
    }

    public CacheEvent(EventAccessOperations event) {
        if (event == null) { throw new IllegalArgumentException(
                "EventAccess cannot be null"); }
        this.event = event;
    }

    public EventAccessOperations getEventAccess() {
        if (event instanceof CacheEvent) {
            logger.debug("CacheEvent nested inside of CacheEvent!  NO!  NO!");
            return ((CacheEvent) event).getEventAccess();
        } else {
            return event;
        }
    }

    public EventFactory a_factory() {
        if (event != null) {
            return event.a_factory();
        } else {
            throw new org.omg.CORBA.NO_IMPLEMENT();
        }
    }

    public EventFinder a_finder() {
        if (event != null) {
            return event.a_finder();
        } else {
            throw new org.omg.CORBA.NO_IMPLEMENT();
        }
    }

    public EventChannelFinder a_channel_finder() {
        if (event != null) {
            return event.a_channel_finder();
        } else {
            throw new org.omg.CORBA.NO_IMPLEMENT();
        }
    }

    public AuditElement[] get_audit_trail() throws NotImplemented {
        if (event != null) {
            return event.get_audit_trail();
        } else {
            throw new org.omg.CORBA.NO_IMPLEMENT();
        }
    }

    public Event a_writeable() {
        if (event != null) {
            return event.a_writeable();
        } else {
            throw new org.omg.CORBA.NO_IMPLEMENT();
        }
    }

    public ParameterComponent parm_svc() {
        if (event != null) {
            return event.parm_svc();
        } else {
            throw new org.omg.CORBA.NO_IMPLEMENT();
        }
    }

    public EventAttr get_attributes() {
        if (attr == null) {
            this.attr = event.get_attributes();
            if (attr == null) {
                // remote doesn't implement
                attr = EventAttrImpl.createEmpty();
            }
        }
        return attr;
    }

    public Origin[] get_origins() {
        if (origins == null) {
            origins = event.get_origins();
        }
        return origins;
    }

    public Origin get_origin(String the_origin) throws OriginNotFound {
        if (event != null) {
            return event.get_origin(the_origin);
        } else {
            for (int i = 0; i < origins.length; i++) {
                if (origins[i].get_id().equals(the_origin)) { return origins[i]; }
            }
        }
        throw new OriginNotFound();
    }

    public Origin get_preferred_origin() throws NoPreferredOrigin {
        if (preferred == null) {
            if (event != null) {
                preferred = event.get_preferred_origin();
            } else {
                throw new NoPreferredOrigin();
            }
        }
        return preferred;
    }

    public Locator[] get_locators(String an_origin) throws OriginNotFound,
            NotImplemented {
        if (event != null) { return event.get_locators(an_origin); }
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public AuditElement[] get_audit_trail_for_origin(String the_origin)
            throws OriginNotFound, NotImplemented {
        if (event != null) { return event.get_audit_trail_for_origin(the_origin); }
        throw new NotImplemented();
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (getEventAccess() != null && o instanceof CacheEvent
                && ((CacheEvent) o).getEventAccess() != null
                && getEventAccess().equals(((CacheEvent) o).getEventAccess())) {
            return true;
        } else if (o instanceof EventAccessOperations) {
            EventAccessOperations oEvent = (EventAccessOperations) o;
            if (get_attributes().equals(oEvent.get_attributes())) {
                Origin thisOrigin = getOrigin();
                if (thisOrigin == null && thisOrigin == extractOrigin(oEvent)) {
                    return true;
                } else if (thisOrigin.equals(extractOrigin(oEvent))) { return true; }
            }
        }
        return false;
    }

    public int hashCode() {
        if (!hashSet) {
            int result = 52;
            result = 48 * result + getOrigin().hashCode();
            result = 48 * result + get_attributes().hashCode();
            hashValue = result;
            hashSet = true;
        }
        return hashValue;
    }

    private boolean hashSet = false;

    private int hashValue;

    /**
     * @return true if both the attributes and the preferred origin are cached
     */
    public boolean isLoaded() {
        return attr != null && preferred != null;
    }

    public Origin getOrigin() {
        return extractOrigin(this);
    }

    /**
     * This gets around the NoPreferredOrigin exception
     */
    public static Origin extractOrigin(EventAccessOperations ev) {
        try {
            return ev.get_preferred_origin();
        } catch (NoPreferredOrigin e) {
            Origin[] oArray = ev.get_origins();
            if (oArray.length > 0) {
                return oArray[0];
            } else {
                throw new RuntimeException("No preferred origin", e);
            }
        }
    }

    public String toString() {
        return getEventInfo(this);
    }

    /**
     * @ returns a string for the form "Event: Location | Time | Magnitude |
     *   Depth"
     */
    public static String getEventInfo(EventAccessOperations event) {
        return getEventInfo(event, NO_ARG_STRING);
    }

    public static String getEventInfo(EventAccessOperations event, String format) {
        return getEventInfo(event, format, new SimpleDateFormat(
                "MM/dd/yyyy HH:mm:sss z"));
    }

    /**
     * @ formats a string for the given event. To insert information about a
     *   certain item magic strings are used in the format string Magic Strings
     *   LOC adds the location of the event TIME adds the event time MAG adds
     *   event magnitude DEPTH adds the depth
     * 
     * For example the string "Event: " + LOC + " | " + TIME + " | Mag: " + MAG + " |
     * Depth: " + DEPTH + " " + DEPTH_UNIT produces the same thing as the no
     * format call to getEventInfo
     */
    public static String getEventInfo(EventAccessOperations event,
            String format, DateFormat sdf) {
        //Get geographic name of origin
        ParseRegions regions = ParseRegions.getInstance();
        String location = regions.getGeographicRegionName(event.get_attributes().region.number);

        //Get Date and format it accordingly
        Origin origin = extractOrigin(event);
        MicroSecondDate msd = new MicroSecondDate(origin.origin_time);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        String originTimeString = sdf.format(msd);

        //Get Magnitude
        float mag = Float.NaN;
        if (origin.magnitudes.length > 0) {
            mag = origin.magnitudes[0].value;
        }

        //get depth
        Quantity depth = origin.my_location.depth;

        float latitude = origin.my_location.latitude;
        float longitude = origin.my_location.longitude;

        StringBuffer buf = new StringBuffer(format);
        for (int i = 0; i < magicStrings.length; i++) {
            int index = buf.indexOf(magicStrings[i]);
            if (index != -1) {
                buf.delete(index, index + magicStrings[i].length());
                if (magicStrings[i].equals(LOC)) {
                    buf.insert(index, location);
                } else if (magicStrings[i].equals(TIME)) {
                    buf.insert(index, originTimeString);
                } else if (magicStrings[i].equals(MAG)) {
                    if (Float.isNaN(mag)) {
                        buf.insert(index, "...");
                    } else {
                        buf.insert(index, mag);
                    }
                } else if (magicStrings[i].equals(DEPTH)) {
                    buf.insert(index, depthFormatter.format(depth.value));
                } else if (magicStrings[i].equals(DEPTH_UNIT)) {
                    buf.insert(
                            index,
                            UnitDisplayUtil.getNameForUnit((UnitImpl) depth.the_units));
                } else if (magicStrings[i].equals(LAT)) {
                    buf.insert(index, latitude);
                } else if (magicStrings[i].equals(LON)) {
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

    private static final String[] magicStrings = { LOC, TIME, MAG, DEPTH,
            DEPTH_UNIT, LAT, LON };

    private static final String NO_ARG_STRING = "Event: " + LOC + " | " + TIME
            + " | Mag: " + MAG + " | Depth " + DEPTH + " " + DEPTH_UNIT;

    protected EventAccessOperations event;

    protected EventAttr attr;

    protected Origin[] origins;

    protected Origin preferred;

    private static final Logger logger = Logger.getLogger(CacheEvent.class);

} // CacheEvent
