
package edu.sc.seis.fissuresUtil.cache;

import edu.iris.Fissures.IfEvent.*;

import edu.iris.Fissures.Location;
import edu.iris.Fissures.NotImplemented;
import edu.iris.Fissures.Quantity;
import edu.iris.Fissures.event.EventAttrImpl;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.utility.Assert;
import edu.sc.seis.fissuresUtil.display.ParseRegions;
import edu.sc.seis.fissuresUtil.display.UnitDisplayUtil;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import edu.iris.Fissures.Time;
import edu.iris.Fissures.FlinnEngdahlRegion;

/**
 * CacheEvent.java
 *
 *
 * Created: Mon Jan  8 16:33:52 2001
 *
 * @author Philip Crotwell
 * @version
 */

public class CacheEvent implements EventAccessOperations {
    public CacheEvent(EventAttr attr, Origin[] origins, Origin preferred) {
        Assert.isNotNull(attr, "EventAttr cannot be null");
        Assert.isNotNull(origins, "origins cannot be null");
        this.attr = attr;
        this.origins = origins;
        this.preferred = preferred;
    }

    public CacheEvent(EventAccessOperations event) {
        Assert.isNotNull(event, "EventAccess cannot be null");
        this.event = event;
        this.attr = null;
        this.origins = null;
        this.preferred = null;
    }

    public EventAccessOperations getEventAccess() {
        return event;
    }

    //
    // IDL:iris.edu/Fissures/IfEvent/EventMgr/a_factory:1.0
    //
    /***/

    public EventFactory
        a_factory(){
        if (event != null) {
            return event.a_factory();
        } else {
            throw new org.omg.CORBA.NO_IMPLEMENT();
        }
    }

    //
    // IDL:iris.edu/Fissures/IfEvent/EventDC/a_finder:1.0
    //
    /***/

    public EventFinder
        a_finder() {
        if (event != null) {
            return event.a_finder();
        } else {
            throw new org.omg.CORBA.NO_IMPLEMENT();
        }
    }
    //
    // IDL:iris.edu/Fissures/IfEvent/EventDC/a_channel_finder:1.0
    //
    /***/

    public EventChannelFinder
        a_channel_finder() {
        if (event != null) {
            return event.a_channel_finder();
        } else {
            throw new org.omg.CORBA.NO_IMPLEMENT();
        }
    }

    //
    // IDL:iris.edu/Fissures/AuditSystemAccess/get_audit_trail:1.0
    //
    /***/

    public edu.iris.Fissures.AuditElement[] get_audit_trail()
        throws NotImplemented {
        if (event != null) {
            return event.get_audit_trail();
        } else {
            throw new org.omg.CORBA.NO_IMPLEMENT();
        }
    }


    //
    // IDL:iris.edu/Fissures/IfEvent/EventAccess/a_writeable:1.0
    //
    /***/

    public Event a_writeable() {
        if (event != null) {
            return event.a_writeable();
        } else {
            throw new org.omg.CORBA.NO_IMPLEMENT();
        }
    }

    //
    // IDL:iris.edu/Fissures/IfEvent/EventAccess/parm_svc:1.0
    //
    /** Defines the ParameterMgr where parameters for this Event reside */

    public edu.iris.Fissures.IfParameterMgr.ParameterComponent
        parm_svc() {
        if (event != null) {
            return event.parm_svc();
        } else {
            throw new org.omg.CORBA.NO_IMPLEMENT();
        }
    }

    //
    // IDL:iris.edu/Fissures/IfEvent/EventAccess/get_attributes:1.0
    //
    /***/

    public EventAttr
        get_attributes() {
        if (attr == null) {
            this.attr = event.get_attributes();
            if (attr == null) {
                // remote doesn't implement
                attr = EventAttrImpl.createEmpty();
            }
        }
        return attr;
    }

    //
    // IDL:iris.edu/Fissures/IfEvent/EventAccess/get_origins:1.0
    //
    /***/

    public Origin[] get_origins() {
        if (origins == null) {
            origins = event.get_origins();
        }
        return origins;
    }

    //
    // IDL:iris.edu/Fissures/IfEvent/EventAccess/get_origin:1.0
    //
    /***/

    public Origin get_origin(String the_origin)
        throws OriginNotFound {
        if (event != null) {
            return event.get_origin(the_origin);
        } else {
            for (int i=0; i<origins.length; i++) {
                if (origins[i].get_id().equals(the_origin)) {
                    return origins[i];
                }
            }
        }
        throw new OriginNotFound();
    }

    //
    // IDL:iris.edu/Fissures/IfEvent/EventAccess/get_preferred_origin:1.0
    //
    /***/
    public Origin get_preferred_origin()
        throws NoPreferredOrigin {
        if (preferred == null) {
            if (event != null) {
                preferred = event.get_preferred_origin();
            } else {
                throw new NoPreferredOrigin();
            }
        }
        return preferred;
    }

    //
    // IDL:iris.edu/Fissures/IfEvent/EventAccess/get_locators:1.0
    //
    /***/

    public Locator[] get_locators(String an_origin)
        throws OriginNotFound, NotImplemented {
        if (event != null) {
            return event.get_locators(an_origin);
        }
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    //
    // IDL:iris.edu/Fissures/IfEvent/EventAccess/get_audit_trail_for_origin:1.0
    //
    /***/

    public edu.iris.Fissures.AuditElement[]
        get_audit_trail_for_origin(String the_origin)
        throws OriginNotFound,
        edu.iris.Fissures.NotImplemented {
        if (event != null) {
            return event.get_audit_trail_for_origin(the_origin);
        }
        throw new edu.iris.Fissures.NotImplemented();
    }

    public boolean equals(Object o){
        if (getEventAccess() != null && o instanceof CacheEvent && ((CacheEvent)o).getEventAccess() != null) {
            return getEventAccess().equals(((CacheEvent)o).getEventAccess());
        }

        // must be local only event (ie no corba)
        if(o == this) return true;
        if(!(o instanceof EventAccessOperations)) return false;
        EventAccessOperations oEvent = (EventAccessOperations)o;
        if(!equalAttr(oEvent) || !equalOrigin(oEvent)){
            return false;
        }
        return true;
    }

    public int hashCode(){
        int result = 52;
        result = 48*result + hashOrigins();
        //result = 48*result + event.get_attributes().hashCode();
        return result;
    }

    private int hashAttr(){
        EventAttr attr = event.get_attributes();
        int result = 87;
        result = result*34 + attr.name.hashCode();
        result = result*34 + attr.region.number;
        return result;
    }

    private int hashOrigins(){
        int result = 29;
        Origin o = getOrigin();
        result = 89* result + hashLocation(o.my_location);
        result = 89*result + o.origin_time.date_time.hashCode();
        result = 89*result + o.contributor.hashCode();
        result = 89*result + o.catalog.hashCode();
        return result;
    }

    private int hashLocation(Location l){
        int result = 47;
        result = 38*result + l.depth.hashCode();
        result = 38*result + l.elevation.hashCode();
        result = 38*result + (int)l.latitude;
        result = 38*result + (int)l.longitude;
        return result;
    }

    private boolean equalOrigin(EventAccessOperations oEvent) {
        Origin oOrigin = null;
        Origin thisOrigin = getOrigin();
        try{
            oOrigin = oEvent.get_preferred_origin();
        }catch(NoPreferredOrigin e){
            Origin[] oArray = oEvent.get_origins();
            if (oArray.length> 0) {
                oOrigin = oArray[0];
            }
        }
        if(!equals(oOrigin.my_location, thisOrigin.my_location)||
           !oOrigin.catalog.equals(thisOrigin.catalog) ||
           !oOrigin.contributor.equals(thisOrigin.contributor) ||
           !equals(oOrigin.origin_time, thisOrigin.origin_time)){
            return false;
        }
        return true;
    }

    private Origin getOrigin(){
        Origin thisOrigin = null;
        try{
            thisOrigin = get_preferred_origin();
        }catch(NoPreferredOrigin e){
            Origin[] oArray = get_origins();
            if (oArray.length> 0) {
                thisOrigin = oArray[0];
            }
        }
        return thisOrigin;
    }

    private static boolean equals(Time one, Time two) {
        MicroSecondDate msdOne = new MicroSecondDate(one);
        MicroSecondDate msdTwo = new MicroSecondDate(two);
        return msdOne.equals(msdTwo);
    }

    private static boolean equals(Location one, Location two){
        if(one.depth.equals(two.depth) && one.elevation.equals(two.elevation) &&
           one.latitude == two.latitude && one.longitude == two.longitude){
            return true;
        }
        return false;
    }

    private static boolean equals(FlinnEngdahlRegion one, FlinnEngdahlRegion two) {
        if(one.number == two.number) return true;
        return false;
    }

    private boolean equalAttr(EventAccessOperations event) {
        EventAttr oAttr = event.get_attributes();
        EventAttr thisAttr = get_attributes();
        if(!oAttr.name.equals(thisAttr.name)  ||
           !equals(oAttr.region, thisAttr.region)){
            return false;
        }
        return true;
    }

    /**
     *@ returns a string for the form "Event: Location | Time | Magnitude | Depth"
     */
    public static String getEventInfo(EventAccessOperations event){
        return getEventInfo(event, NO_ARG_STRING);
    }

    public static String getEventInfo(EventAccessOperations event, String format) {
        return getEventInfo(event, format, new SimpleDateFormat("MM/dd/yyyy HH:mm:ss z"));
    }

    /**
     *@ formats a string for the given event.  To insert information about a
     * certain item magic strings are used in the format string
     * Magic Strings
     * LOC adds the location of the event
     * TIME adds the event time
     * MAG adds event magnitude
     * DEPTH adds the depth
     *
     *For example the string
     *"Event: " + LOC + " | " + TIME + " | Mag: " + MAG + " | Depth: " + DEPTH + " " + DEPTH_UNIT
     *produces the same thing as the no format call to getEventInfo
     */
    public static String getEventInfo(EventAccessOperations event, String format, DateFormat sdf){
        //Get geographic name of origin
        ParseRegions regions = ParseRegions.getInstance();
        String location = regions.getGeographicRegionName(event.get_attributes().region.number);

        //Get Date and format it accordingly
        Origin origin;
        try{
            origin = event.get_preferred_origin();
        }catch(NoPreferredOrigin e){
            origin = event.get_origins()[0];
        }
        MicroSecondDate msd = new MicroSecondDate(origin.origin_time);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        String originTimeString = sdf.format(msd);

        //Get Magnitude
        float mag = origin.magnitudes[0].value;

        //get depth

        Quantity depth = origin.my_location.depth;

        StringBuffer buf = new StringBuffer(format);
        for (int i = 0; i < magicStrings.length; i++) {
            int index = buf.indexOf(magicStrings[i]);
            if(index != -1){
                buf.delete(index, index + magicStrings[i].length());
                if(magicStrings[i].equals(LOC)){
                    buf.insert(index, location);
                }else if(magicStrings[i].equals(TIME)){
                    buf.insert(index, originTimeString);
                }else if(magicStrings[i].equals(MAG)){
                    buf.insert(index, mag);
                }else if(magicStrings[i].equals(DEPTH)){
                    buf.insert(index, depthFormatter.format(depth.value));
                }else if(magicStrings[i].equals(DEPTH_UNIT)){
                    buf.insert(index, UnitDisplayUtil.getNameForUnit((UnitImpl)depth.the_units));
                }
            }
        }
        return buf.toString();
    }

    private static DecimalFormat depthFormatter = new DecimalFormat("###0.00");

    public static final String LOC = "LOC", TIME = "TIME", MAG = "MAG", DEPTH = "DEPTH", DEPTH_UNIT = "DEPTH_UNIT";

    private static final String[] magicStrings = { LOC, TIME, MAG, DEPTH, DEPTH_UNIT};

    private static final String NO_ARG_STRING = "Event: " + LOC + " | " + TIME + " | Mag: " + MAG + " | Depth " + DEPTH + " " + DEPTH_UNIT;

    protected EventAccessOperations event;
    protected EventAttr attr;
    protected Origin[] origins;
    protected Origin preferred;

} // CacheEvent
