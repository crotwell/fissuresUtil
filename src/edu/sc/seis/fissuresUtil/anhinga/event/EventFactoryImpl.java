package edu.sc.seis.anhinga.event;

import java.sql.SQLException;
import org.apache.log4j.Logger;
import edu.iris.Fissures.IfEvent.Event;
import edu.iris.Fissures.IfEvent.EventAttr;
import edu.iris.Fissures.IfEvent.EventChannelFinder;
import edu.iris.Fissures.IfEvent.EventFactory;
import edu.iris.Fissures.IfEvent.EventFactoryPOA;
import edu.iris.Fissures.IfEvent.EventFinder;
import edu.iris.Fissures.IfEvent.EventHelper;
import edu.sc.seis.anhinga.database.JDBCEventAttr;
import edu.sc.seis.anhinga.database.JDBCFlinnEngdahl;
import edu.sc.seis.anhinga.database.JDBCParameterRef;

/**
 * EventFactoryImpl.java
 *
 *
 * Created: Mon Oct  1 13:12:13 2001
 *
 * @author <a href="mailto: "Srinivasa Telukutla</a>
 * @version
 */

public class EventFactoryImpl extends EventFactoryPOA{
    public EventFactoryImpl( JDBCParameterRef jdbcParameterRef, JDBCEventAttr jdbcEventAttr,JDBCFlinnEngdahl jdbcFlinnEngdahl, org.omg.PortableServer.POA poa){
    this.jdbcParameterRef = jdbcParameterRef;
    this.jdbcEventAttr = jdbcEventAttr;
    this.jdbcFlinnEngdahl = jdbcFlinnEngdahl;
    this.poa = poa;

    }
     //
    // IDL:iris.edu/Fissures/IfEvent/EventFactory/create:1.0
    //
    /***/

    public Event create(EventAttr attributes,
            edu.iris.Fissures.AuditInfo[] audit_info){
    //enImpl
    logger.debug("In The method create");
    Event event = null;
    try {
        int eventid;
        eventid = jdbcEventAttr.put(attributes);
        logger.debug("The event id obtained is "+eventid);
         org.omg.CORBA.Object obj  = poa.create_reference_with_id(new Integer(eventid).toString().getBytes(), EventHelper.id());
         logger.debug("After creating the obj reference with id");
         event = EventHelper.narrow(obj);
         if(event == null) logger.debug("The event to be returned is null");
         else
         logger.debug("IOR: "+_orb().object_to_string(event));

        // return event;
    } catch(SQLException sqle) {
        logger.error("SQLException caught while inserting eventattr into database from EventFactoryImpl create method", sqle);
    } catch(edu.sc.seis.anhinga.database.NotFound nfe) {
        logger.error("NOTFOUNDException caught while inserting eventattr into database from EventFactoryImpl create method", nfe);
    }catch(Exception e) {

        logger.error(" CAUGHT THE EXCEPTION ", e);

    }

    return event;

    }

    public EventFactory a_factory() {
    return eventFactory; //this._this();
    }
  //
    // IDL:iris.edu/Fissures/IfEvent/EventDC/a_finder:1.0
    //
    /***/

    public EventFinder a_finder(){
    /*EventFinderImpl eventFinderImpl = new EventFinderImpl((JDBCEventAttr jdbcEventAttr,
               int eventid,
               JDBCOrigin jdbcOrigin,
               JDBCLocator jdbcLocator,
               JDBCLocation jdbcLocation,
               JDBCCatalog jdbcCatalog,
               org.omg.PortableServer.POA poa)*/
    return eventFinder;
    }

    //
    // IDL:iris.edu/Fissures/IfEvent/EventDC/a_channel_finder:1.0
    //
    /***/

    public EventChannelFinder
    a_channel_finder(){return null;}


     public void setEventFinder(EventFinder eventFinder) {

    this.eventFinder = eventFinder;
    }

    public void setEventFactory(EventFactory eventFactory) {

    this.eventFactory = eventFactory;
    }

    private EventFinder eventFinder = null;

    private EventFactory eventFactory = null;

    private JDBCParameterRef jdbcParameterRef;
    private JDBCEventAttr jdbcEventAttr;
    private JDBCFlinnEngdahl jdbcFlinnEngdahl;
    private org.omg.PortableServer.POA poa;

    private static Logger logger = Logger.getLogger(EventFactoryImpl.class);
}// EventFactoryImpl
