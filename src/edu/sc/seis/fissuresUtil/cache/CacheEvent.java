
package edu.sc.seis.fissuresUtil.cache;

import edu.iris.Fissures.*;
import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.event.*;
import edu.iris.Fissures.model.*;
import edu.iris.Fissures.utility.*;

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

    public CacheEvent(EventAccess event) {
	Assert.isNotNull(event, "EventAccess cannot be null");
	this.event = event;
	this.attr = null;
	this.origins = null;
	this.preferred = null;
    }

    public EventAccess getEventAccess() {
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

    protected EventAccess event;    
    protected EventAttr attr;
    protected Origin[] origins;
    protected Origin preferred;

} // CacheEvent
