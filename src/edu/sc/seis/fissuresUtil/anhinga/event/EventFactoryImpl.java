package edu.sc.seis.fissuresUtil.anhinga.event;

import java.sql.SQLException;
import org.apache.log4j.Logger;
import edu.iris.Fissures.IfEvent.Event;
import edu.iris.Fissures.IfEvent.EventAttr;
import edu.iris.Fissures.IfEvent.EventChannelFinder;
import edu.iris.Fissures.IfEvent.EventFactory;
import edu.iris.Fissures.IfEvent.EventFactoryPOA;
import edu.iris.Fissures.IfEvent.EventFinder;
import edu.iris.Fissures.IfEvent.EventHelper;
import edu.sc.seis.fissuresUtil.database.event.JDBCEventAccess;

/**
 * EventFactoryImpl.java Created: Mon Oct 1 13:12:13 2001
 * 
 * @author <a href="mailto: "Srinivasa Telukutla </a>
 * @version
 */
public class EventFactoryImpl extends EventFactoryPOA {

    public EventFactoryImpl(JDBCEventAccess jdbcEventAccess,
            org.omg.PortableServer.POA poa) {
        this.jdbcEventAccess = jdbcEventAccess;
        this.poa = poa;
    }

    public Event create(EventAttr attributes,
                        edu.iris.Fissures.AuditInfo[] audit_info) {
        logger.debug("In The method create");
        Event event = null;
        try {
            int eventid;
            eventid = jdbcEventAccess.put(attributes);
            logger.debug("The event id obtained is " + eventid);
            org.omg.CORBA.Object obj = poa.create_reference_with_id(new Integer(eventid).toString()
                                                                            .getBytes(),
                                                                    EventHelper.id());
            logger.debug("After creating the obj reference with id");
            event = EventHelper.narrow(obj);
            if(event == null) logger.debug("The event to be returned is null");
            else logger.debug("IOR: " + _orb().object_to_string(event));
            // return event;
        } catch(SQLException sqle) {
            logger.error("SQLException caught while inserting eventattr into database from EventFactoryImpl create method",
                         sqle);
        } catch(edu.sc.seis.anhinga.database.NotFound nfe) {
            logger.error("NOTFOUNDException caught while inserting eventattr into database from EventFactoryImpl create method",
                         nfe);
        } catch(Exception e) {
            logger.error(" CAUGHT THE EXCEPTION ", e);
        }
        return event;
    }

    public EventFactory a_factory() {
        return eventFactory;
    }

    public EventFinder a_finder() {
        return eventFinder;
    }

    public EventChannelFinder a_channel_finder() {
        return null;
    }

    public void setEventFinder(EventFinder eventFinder) {
        this.eventFinder = eventFinder;
    }

    public void setEventFactory(EventFactory eventFactory) {
        this.eventFactory = eventFactory;
    }

    private EventFinder eventFinder = null;

    private EventFactory eventFactory = null;

    private JDBCEventAccess jdbcEventAccess;

    private org.omg.PortableServer.POA poa;

    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(EventFactoryImpl.class);
}// EventFactoryImpl
