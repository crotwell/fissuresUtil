package edu.sc.seis.fissuresUtil.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.EventAttr;
import edu.iris.Fissures.IfEvent.NoPreferredOrigin;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.IfEvent.OriginNotFound;
import edu.iris.Fissures.event.EventAttrImpl;
import edu.iris.Fissures.event.OriginImpl;

/**
 * CacheEvent.java Created: Mon Jan 8 16:33:52 2001
 * 
 * @author Philip Crotwell
 * @version
 */
public class CacheEvent extends ProxyEventAccessOperations {

    /** for use by hibernate */
    protected CacheEvent() {}

    public CacheEvent(EventAttr attr, Origin preferred) {
        this(attr, (OriginImpl)preferred);
    }
    
    /**
     * Initializes the origins array to be just the single prefferred origin.
     */
    public CacheEvent(EventAttr attr, OriginImpl preferred) {
        this(attr, new Origin[] {preferred}, preferred);
    }

    public CacheEvent(EventAttr attr, Origin[] origins, OriginImpl preferred) {
        if(attr == null) { throw new IllegalArgumentException("EventAttr cannot be null"); }
        if(origins == null) { throw new IllegalArgumentException("origins cannot be null"); }
        this.attr = attr;
        this.origins = origins;
        this.preferred = preferred;
    }

    public CacheEvent(EventAccessOperations event) {
        if(event == null) { throw new IllegalArgumentException("EventAccess cannot be null"); }
        setEventAccess(event);
        get_attributes();
        try {
            getPreferred();
        } catch (NoPreferredOrigin e) {
            // oh well...
        }
    }

    public EventAttr get_attributes() {
        if(attr == null && event != null) {
            this.attr = event.get_attributes();
        }
        if(attr == null) {
            // remote doesn't implement
            attr = EventAttrImpl.createEmpty();
        }
        return attr;
    }

    public EventAttrImpl getAttributes() {
        return (EventAttrImpl)get_attributes();
    }

    public Origin[] get_origins() {
        if(origins == null && event != null) {
            origins = event.get_origins();
        }
        if (origins == null) {
            try {
                origins = new OriginImpl[] {getPreferred()};
            } catch(NoPreferredOrigin e) {
                // shouldn't happen
                origins = new Origin[0];
            }
        }
        return origins;
    }
    
    public Origin[] getOrigins() {
        return get_origins();
    }
    
    /** for use by hibernate */
    protected void setOrigins(Origin[] origins) {
        this.origins = origins;
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
        return getPreferred();
    }

    public OriginImpl getPreferred() throws NoPreferredOrigin {
        if(preferred == null) {
            if(event != null) {
                preferred = (OriginImpl)event.get_preferred_origin();
            } else {
                throw new NoPreferredOrigin();
            }
        }
        return preferred;
    }
    
    protected void setPreferred(OriginImpl o) {
        this.preferred = o;
    }

    /**
     * @return true if both the attributes and the preferred origin are cached
     */
    public boolean isLoaded() {
        return attr != null && preferred != null;
    }

    public boolean hasDbid(){
        return dbid > -1;
    }
    
    public int getDbid() {
        return dbid;
    }

    public void setDbid(int id) {
        dbid = id;
    }

    private int dbid;

    protected EventAttr attr;

    protected Origin[] origins;

    protected OriginImpl preferred;

    private static final Logger logger = LoggerFactory.getLogger(CacheEvent.class);
} // CacheEvent
