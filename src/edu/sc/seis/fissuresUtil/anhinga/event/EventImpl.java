package edu.sc.seis.fissuresUtil.anhinga.event;

import java.sql.SQLException;
import org.apache.log4j.Logger;
import edu.iris.Fissures.AuditElement;
import edu.iris.Fissures.IfEvent.Event;
import edu.iris.Fissures.IfEvent.EventAttr;
import edu.iris.Fissures.IfEvent.EventChannelFinder;
import edu.iris.Fissures.IfEvent.EventFactory;
import edu.iris.Fissures.IfEvent.EventFinder;
import edu.iris.Fissures.IfEvent.EventOperations;
import edu.iris.Fissures.IfEvent.EventPOA;
import edu.iris.Fissures.IfEvent.EventPOATie;
import edu.iris.Fissures.IfEvent.Locator;
import edu.iris.Fissures.IfEvent.Magnitude;
import edu.iris.Fissures.IfEvent.NoPreferredOrigin;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.IfEvent.OriginNotFound;
import edu.iris.Fissures.IfParameterMgr.ParameterRef;
import edu.iris.Fissures.event.OriginImpl;
import edu.sc.seis.fissuresUtil.database.NotFound;
import edu.sc.seis.fissuresUtil.database.event.JDBCEventAccess;

/**
 * EventImpl.java Created: Fri Mar 23 09:30:07 2001
 * 
 * @author Srinivasa Telukutla
 * @version
 */
public class EventImpl extends EventAccessImpl implements EventOperations {

    public EventImpl(JDBCEventAccess jdbcEventAccess, int eventid) {
        super(jdbcEventAccess, eventid);
    }

    public void update_region(edu.iris.Fissures.FlinnEngdahlRegion region,
                              edu.iris.Fissures.AuditInfo[] audit_info) {
        try {
            jdbcEventAccess.updateFlinnEngdahlRegion(eventid, region);
        } catch(SQLException sqle) {
            logger.error("Problem with SQL ", sqle);
            throw new org.omg.CORBA.INTERNAL(sqle.toString());
        } catch(NotFound nfe) {
            logger.error(" The Event with id " + eventid + " is Not Found", nfe);
            throw new org.omg.CORBA.INTERNAL(nfe.toString());
        }
    }

    public void destroy() {
    //not implemented.
    }

    /**
     * Add a origin to the event. If no origin exist the origin will become the
     * default until the default is reset by a add_origin or
     * set_preferred_origin.
     */
    public String add_origin(edu.iris.Fissures.Time origin_time,
                             edu.iris.Fissures.Location the_location,
                             Magnitude[] magnitudes,
                             Locator[] locators,
                             boolean preferred,
                             String the_catalog,
                             String the_contributor,
                             edu.iris.Fissures.AuditInfo[] audit_info) {
        try {
            ParameterRef[] params = new ParameterRef[0];
            Origin newOrigin = (Origin)new OriginImpl("id",
                                                      the_catalog,
                                                      the_contributor,
                                                      origin_time,
                                                      the_location,
                                                      magnitudes,
                                                      params);
            int originid = jdbcOrigin.put(newOrigin, eventid);
            if(preferred) set_preferred_origin(new Integer(originid).toString(),
                                               audit_info);
            //jdbcEventAttr.putPreferredOrigin(newOrigin, eventid);
            add_locators(new Integer(originid).toString(), locators, audit_info);
            return new Integer(originid).toString();
        } catch(SQLException sqle) {
            logger.error("Problem with SQL ", sqle);
            throw new org.omg.CORBA.INTERNAL(sqle.toString());
        } catch(edu.sc.seis.anhinga.database.NotFound nfe) {
            logger.error(" The Event with id " + eventid + " is Not Found", nfe);
            throw new org.omg.CORBA.INTERNAL(nfe.toString());
        } catch(edu.iris.Fissures.IfEvent.OriginNotFound onfe) {
            logger.error(" The origin is not Found ", onfe);
            throw new org.omg.CORBA.INTERNAL(onfe.toString());
        }
    }

    public void add_locators(String the_origin,
                             Locator[] locators,
                             edu.iris.Fissures.AuditInfo[] audit_info)
            throws OriginNotFound {
        try {
            int counter;
            int originid;
            int locatorid;
            for(counter = 0; counter < locators.length; counter++) {
                originid = Integer.parseInt(the_origin);
                locatorid = jdbcLocator.put(locators[counter]);
                jdbcLocator.updateLocatorOriginId(originid, locatorid);
            }
        } catch(SQLException sqle) {
            logger.error("Problem with SQL ", sqle);
            throw new org.omg.CORBA.INTERNAL(sqle.toString());
        } catch(edu.sc.seis.anhinga.database.NotFound nfe) {
            logger.error(" The Event with id " + eventid + " is Not Found", nfe);
            throw new org.omg.CORBA.INTERNAL(nfe.toString());
        } catch(NumberFormatException nfee) {
            logger.error(" Error while parsing the Stringified originid ", nfee);
            throw new org.omg.CORBA.INTERNAL(nfee.toString());
        }
    }

    public void add_magnitude(String the_origin,
                              Magnitude a_magnitude,
                              edu.iris.Fissures.AuditInfo[] audit_info)
            throws OriginNotFound {
        try {
            int originid = Integer.parseInt(the_origin);
            Magnitude[] magnitudes = new Magnitude[1];
            magnitudes[0] = a_magnitude;
            jdbcOrigin.putOriginMagnitudesArray(magnitudes, originid);
        } catch(SQLException sqle) {
            logger.error("Problem with SQL ", sqle);
            throw new org.omg.CORBA.INTERNAL(sqle.toString());
        } catch(edu.sc.seis.anhinga.database.NotFound nfe) {
            logger.error(" The Event with id " + eventid + " is Not Found", nfe);
            throw new org.omg.CORBA.INTERNAL(nfe.toString());
        } catch(NumberFormatException nfee) {
            logger.error(" Error while parsing the Stringified originid ", nfee);
            throw new org.omg.CORBA.INTERNAL(nfee.toString());
        }
    }

    public void delete_origin(String the_origin,
                              edu.iris.Fissures.AuditInfo[] audit_info)
            throws OriginNotFound {
        try {
            int originid = Integer.parseInt(the_origin);
            jdbcOrigin.deleteOrigin(originid);
        } catch(SQLException sqle) {
            logger.error("Problem with SQL ", sqle);
            throw new org.omg.CORBA.INTERNAL(sqle.toString());
        } catch(edu.sc.seis.anhinga.database.NotFound nfe) {
            logger.error(" The Event with id " + eventid + " is Not Found", nfe);
            throw new org.omg.CORBA.INTERNAL(nfe.toString());
        } catch(NumberFormatException nfee) {
            logger.error(" Error while parsing the Stringified originid ", nfee);
            throw new org.omg.CORBA.INTERNAL(nfee.toString());
        }
    }

    public void set_preferred_origin(String the_origin,
                                     edu.iris.Fissures.AuditInfo[] audit_info)
            throws OriginNotFound {
        try {
            int originid = Integer.parseInt(the_origin);
            jdbcEventAttr.putPreferredOrigin(jdbcOrigin.get(originid), eventid);
        } catch(SQLException sqle) {
            logger.error("Problem with SQL ", sqle);
        } catch(edu.sc.seis.anhinga.database.NotFound nfe) {
            logger.error(" The Event with id " + eventid + " is Not Found", nfe);
        } catch(NumberFormatException nfee) {
            logger.error(" Error while parsing the Stringified originid ", nfee);
        }
    }

    public Event a_writeable() {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public edu.iris.Fissures.IfParameterMgr.ParameterComponent parm_svc() {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public edu.iris.Fissures.AuditElement[] get_audit_trail_for_origin(String the_origin)
            throws OriginNotFound, edu.iris.Fissures.NotImplemented {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public EventFactory a_factory() {
        return eventFactory;
    }

    public EventFinder a_finder() {
        return eventFinder;
    }

    public EventChannelFinder a_channel_finder() {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public AuditElement[] get_audit_trail() {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public void setEventFinder(EventFinder eventFinder) {
        this.eventFinder = eventFinder;
    }

    public void setEventFactory(EventFactory eventFactory) {
        this.eventFactory = eventFactory;
    }

    private EventFinder eventFinder = null;

    private EventFactory eventFactory = null;

    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(EventImpl.class);
} // EventImpl
