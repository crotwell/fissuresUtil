/*
 * Created on Jul 20, 2004
 *
 */
package edu.sc.seis.fissuresUtil.cache;

import org.apache.log4j.Logger;

import edu.iris.Fissures.AuditElement;
import edu.iris.Fissures.NotImplemented;
import edu.iris.Fissures.IfEvent.Event;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.EventAttr;
import edu.iris.Fissures.IfEvent.EventChannelFinder;
import edu.iris.Fissures.IfEvent.EventFactory;
import edu.iris.Fissures.IfEvent.EventFinder;
import edu.iris.Fissures.IfEvent.Locator;
import edu.iris.Fissures.IfEvent.NoPreferredOrigin;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.IfEvent.OriginNotFound;
import edu.iris.Fissures.IfParameterMgr.ParameterComponent;

/**
 * @author oliverpa
 *  
 */
public abstract class ProxyEventAccessOperations implements
		EventAccessOperations {

	public void reset() {
		if (event instanceof ProxyEventAccessOperations) {
			((ProxyEventAccessOperations) event).reset();
		}
	}

	public EventAccessOperations getEventAccess() {
		if (event instanceof ProxyEventAccessOperations) {
			logger
					.debug("ProxyEventAccessOperations nested inside of ProxyEventAccessOperations! NO! NO!");
			return ((ProxyEventAccessOperations) event).getEventAccess();
		} else {
			return event;
		}
	}

	protected void setEventAccess(EventAccessOperations evo) {
		event = evo;
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
		return event.get_attributes();
	}

	public Origin[] get_origins() {
		return event.get_origins();
	}

	public Origin get_origin(String the_origin) throws OriginNotFound {
		return event.get_origin(the_origin);
	}

	public Origin get_preferred_origin() throws NoPreferredOrigin {
		return event.get_preferred_origin();
	}

	public Locator[] get_locators(String an_origin) throws OriginNotFound,
			NotImplemented {
		if (event != null) {
			return event.get_locators(an_origin);
		}
		throw new org.omg.CORBA.NO_IMPLEMENT();
	}

	public AuditElement[] get_audit_trail_for_origin(String the_origin)
			throws OriginNotFound, NotImplemented {
		if (event != null) {
			return event.get_audit_trail_for_origin(the_origin);
		}
		throw new NotImplemented();
	}

	public AuditElement[] get_audit_trail() throws NotImplemented {
		if (event != null) {
			return event.get_audit_trail();
		} else {
			throw new org.omg.CORBA.NO_IMPLEMENT();
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
				if (thisOrigin == null && thisOrigin == EventUtil.extractOrigin(oEvent)) {
					return true;
				} else if (thisOrigin.equals(EventUtil.extractOrigin(oEvent))) {
					return true;
				}
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

	public Origin getOrigin() {
		return EventUtil.extractOrigin(this);
	}

	public String toString() {
		return EventUtil.getEventInfo(this);
	}

	private boolean hashSet = false;

	private int hashValue;

	protected EventAccessOperations event;

	private static final Logger logger = Logger
			.getLogger(ProxyEventAccessOperations.class);
}