/*
 * Created on Jul 20, 2004
 */
package edu.sc.seis.fissuresUtil.cache;

import org.apache.log4j.Logger;
import org.omg.CORBA.SystemException;

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
 */
public class RetryEventAccessOperations extends ProxyEventAccessOperations {
	
	public RetryEventAccessOperations(EventAccessOperations evo, int retry){
		setEventAccess(evo);
		this.retry = retry;
	}
	
	public Event a_writeable() {
		int i = 0;
		SystemException t = null;
		while (i < retry) {
			try {
				return event.a_writeable();
			} catch (OutOfMemoryError e){
				throw new RuntimeException(e);
			} catch (SystemException e) {
				i++;
				if (i == retry - 1) { reset(); }
				t = e;
				logger.warn("Caught exception, retrying " + i + " of " + retry, t);
			}
		}
		throw t;
	}
	
	public ParameterComponent parm_svc() {
		int i = 0;
		SystemException t = null;
		while (i < retry) {
			try {
				return event.parm_svc();
			} catch (OutOfMemoryError e){
				throw new RuntimeException(e);
			} catch (SystemException e) {
				i++;
				if (i == retry - 1) { reset(); }
				t = e;
				logger.warn("Caught exception, retrying " + i + " of " + retry, t);
			}
		}
		throw t;
	}
	
	public EventAttr get_attributes() {
		int i = 0;
		SystemException t = null;
		while (i < retry) {
			try {
				return event.get_attributes();
			} catch (OutOfMemoryError e){
				throw new RuntimeException(e);
			} catch (SystemException e) {
				i++;
				if (i == retry - 1) { reset(); }
				t = e;
				logger.warn("Caught exception, retrying " + i + " of " + retry, t);
			}
		}
		throw t;
	}
	
	public Origin[] get_origins() {
		int i = 0;
		SystemException t = null;
		while (i < retry) {
			try {
				return event.get_origins();
			} catch (OutOfMemoryError e){
				throw new RuntimeException(e);
			} catch (SystemException e) {
				i++;
				if (i == retry - 1) { reset(); }
				t = e;
				logger.warn("Caught exception, retrying " + i + " of " + retry, t);
			}
		}
		throw t;
	}
	
	public Origin get_origin(String the_origin) throws OriginNotFound {
		int i = 0;
		SystemException t = null;
		while (i < retry) {
			try {
				return event.get_origin(the_origin);
			} catch (OutOfMemoryError e){
				throw new RuntimeException(e);
			} catch (SystemException e) {
				i++;
				if (i == retry - 1) { reset(); }
				t = e;
				logger.warn("Caught exception, retrying " + i + " of " + retry, t);
			}
		}
		throw t;
	}
	
	public Origin get_preferred_origin() throws NoPreferredOrigin{
		int i = 0;
		SystemException t = null;
		while (i < retry) {
			try {
				return event.get_preferred_origin();
			} catch (OutOfMemoryError e){
				throw new RuntimeException(e);
			} catch (SystemException e) {
				i++;
				if (i == retry - 1) { reset(); }
				t = e;
				logger.warn("Caught exception, retrying " + i + " of " + retry, t);
			}
		}
		throw t;
	}
	
	public Locator[] get_locators(String an_origin) throws OriginNotFound, NotImplemented {
		int i = 0;
		SystemException t = null;
		while (i < retry) {
			try {
				return event.get_locators(an_origin);
			} catch (OutOfMemoryError e){
				throw new RuntimeException(e);
			} catch (SystemException e) {
				i++;
				if (i == retry - 1) { reset(); }
				t = e;
				logger.warn("Caught exception, retrying " + i + " of " + retry, t);
			}
		}
		throw t;
	}
	
	public AuditElement[] get_audit_trail_for_origin(String the_origin) throws OriginNotFound, NotImplemented {
		int i = 0;
		SystemException t = null;
		while (i < retry) {
			try {
				return event.get_audit_trail_for_origin(the_origin);
			} catch (OutOfMemoryError e){
				throw new RuntimeException(e);
			} catch (SystemException e) {
				i++;
				if (i == retry - 1) { reset(); }
				t = e;
				logger.warn("Caught exception, retrying " + i + " of " + retry, t);
			}
		}
		throw t;
	}
	
	public AuditElement[] get_audit_trail() throws NotImplemented {
		int i = 0;
		SystemException t = null;
		while (i < retry) {
			try {
				return event.get_audit_trail();
			} catch (OutOfMemoryError e){
				throw new RuntimeException(e);
			} catch (SystemException e) {
				i++;
				if (i == retry - 1) { reset(); }
				t = e;
				logger.warn("Caught exception, retrying " + i + " of " + retry, t);
			}
		}
		throw t;
	}
	
	public EventFactory a_factory() {
		int i = 0;
		SystemException t = null;
		while (i < retry) {
			try {
				return event.a_factory();
			} catch (OutOfMemoryError e){
				throw new RuntimeException(e);
			} catch (SystemException e) {
				i++;
				if (i == retry - 1) { reset(); }
				t = e;
				logger.warn("Caught exception, retrying " + i + " of " + retry, t);
			}
		}
		throw t;
	}
	
	public EventFinder a_finder() {
		int i = 0;
		SystemException t = null;
		while (i < retry) {
			try {
				return event.a_finder();
			} catch (OutOfMemoryError e){
				throw new RuntimeException(e);
			} catch (SystemException e) {
				i++;
				if (i == retry - 1) { reset(); }
				t = e;
				logger.warn("Caught exception, retrying " + i + " of " + retry, t);
			}
		}
		throw t;
	}
	
	public EventChannelFinder a_channel_finder() {
		int i = 0;
		SystemException t = null;
		while (i < retry) {
			try {
				return event.a_channel_finder();
			} catch (OutOfMemoryError e){
				throw new RuntimeException(e);
			} catch (SystemException e) {
				i++;
				if (i == retry - 1) { reset(); }
				t = e;
				logger.warn("Caught exception, retrying " + i + " of " + retry, t);
			}
		}
		throw t;
	}
	
	private int retry;
	private static final Logger logger = Logger.getLogger(RetryEventAccessOperations.class);
}
