
package edu.sc.seis.fissuresUtil.cache;

import edu.iris.Fissures.IfEvent.*;

import edu.iris.Fissures.NotImplemented;
import edu.iris.Fissures.Quantity;
import edu.iris.Fissures.event.EventAttrImpl;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.utility.Assert;
import edu.sc.seis.fissuresUtil.display.ParseRegions;
import edu.sc.seis.fissuresUtil.display.UnitDisplayUtil;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

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
    
    /**
     *@ returns a string for the form "Event: Location | Time | Magnitude | Depth"
     */
    public static String getEventInfo(EventAccessOperations event){
        return getEventInfo(event, NO_ARG_STRING);
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
     *"Event: " + LOC + " | " + TIME + " | " + MAG + " | " + DEPTH
     *produces the same thing as the no format call to getEventInfo
     */
    public static String getEventInfo(EventAccessOperations event, String format){
        //Get geographic name of origin
        ParseRegions regions = new ParseRegions();
        String location = regions.getGeographicRegionName(event.get_attributes().region.number);
        
        //Get Date and format it accordingly
        Origin origin;
        try{
            origin = event.get_preferred_origin();
        }catch(NoPreferredOrigin e){
            origin = event.get_origins()[0];
        }
        MicroSecondDate msd = new MicroSecondDate(origin.origin_time);
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss z");
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
                    buf.insert(index, "Mag " + mag);
                }else if(magicStrings[i].equals(DEPTH)){
                    buf.insert(index, "Depth " + depth.value + " " + UnitDisplayUtil.getNameForUnit((UnitImpl)depth.the_units));
                }
            }
        }
        return buf.toString();
    }
    
    public static final String LOC = "loc", TIME = "time", MAG = "mag", DEPTH = "depth";
    
    private static final String[] magicStrings = { LOC, TIME, MAG, DEPTH};
    
    private static final String NO_ARG_STRING = "Event: " + LOC + " | " + TIME + " | " + MAG + " | " + DEPTH;

    protected EventAccessOperations event;
    protected EventAttr attr;
    protected Origin[] origins;
    protected Origin preferred;

} // CacheEvent
