package edu.sc.seis.fissuresUtil.cache;

import org.apache.log4j.Logger;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.EventAttr;
import edu.iris.Fissures.IfEvent.NoPreferredOrigin;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.IfEvent.OriginNotFound;
import edu.iris.Fissures.event.EventAttrImpl;

/**
 * CacheEvent.java Created: Mon Jan 8 16:33:52 2001
 * 
 * @author Philip Crotwell
 * @version
 */
public class CacheEvent extends ProxyEventAccessOperations {

    /**
     * Initializes the origins array to be just the single prefferred origin.
     */
    public CacheEvent(EventAttr attr, Origin preferred) {
        this(attr, new Origin[] {preferred}, preferred);
    }

    public CacheEvent(EventAttr attr, Origin[] origins, Origin preferred) {
        if(attr == null) { throw new IllegalArgumentException("EventAttr cannot be null"); }
        if(origins == null) { throw new IllegalArgumentException("origins cannot be null"); }
        this.attr = attr;
        this.origins = origins;
        this.preferred = preferred;
    }

    public CacheEvent(EventAccessOperations event) {
        if(event == null) { throw new IllegalArgumentException("EventAccess cannot be null"); }
        setEventAccess(event);
    }

    public EventAttr get_attributes() {
        if(attr == null) {
            this.attr = event.get_attributes();
            if(attr == null) {
                // remote doesn't implement
                attr = EventAttrImpl.createEmpty();
            }
        }
        return attr;
    }

    public Origin[] get_origins() {
        if(origins == null) {
            origins = event.get_origins();
        }
        return origins;
    }

    public Origin get_origin(String the_origin) throws OriginNotFound {
        if(event != null) {
            return event.get_origin(the_origin);
        } else {
            for(int i = 0; i < origins.length; i++) {
                if(origins[i].get_id().equals(the_origin)) { return origins[i]; }
            }
        }
        throw new OriginNotFound();
    }

    public Origin get_preferred_origin() throws NoPreferredOrigin {
        if(preferred == null) {
            if(event != null) {
                preferred = event.get_preferred_origin();
            } else {
                throw new NoPreferredOrigin();
            }
        }
        return preferred;
    }

    /**
     * @return true if both the attributes and the preferred origin are cached
     */
    public boolean isLoaded() {
        return attr != null && preferred != null;
    }

    public boolean hasDbId(){
        return dbid > -1;
    }
    
    public int getDbId() {
        if(hasDbId()) { return dbid; }
        throw new UnsupportedOperationException("This event didn't come from our database, it doesn't have a dbid");
    }

    public void setDbId(int id) {
        dbid = id;
    }

    private int dbid = -1;

    protected EventAttr attr;

    protected Origin[] origins;

    protected Origin preferred;

    private static final Logger logger = Logger.getLogger(CacheEvent.class);
} // CacheEvent
