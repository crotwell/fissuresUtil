package edu.sc.seis.anhinga.event;



import java.sql.SQLException;
import org.apache.log4j.Category;
import org.omg.CORBA.CompletionStatus;
import edu.iris.Fissures.BoxArea;
import edu.iris.Fissures.GlobalArea;
import edu.iris.Fissures.IfEvent.EventAccess;
import edu.iris.Fissures.IfEvent.EventAccessHelper;
import edu.iris.Fissures.IfEvent.EventChannelFinder;
import edu.iris.Fissures.IfEvent.EventFactory;
import edu.iris.Fissures.IfEvent.EventFinder;
import edu.iris.Fissures.IfEvent.EventFinderPOA;
import edu.iris.Fissures.IfEvent.EventSeqIterHolder;
import edu.sc.seis.anhinga.database.JDBCCatalog;
import edu.sc.seis.anhinga.database.JDBCEventAttr;
import edu.sc.seis.anhinga.database.JDBCFlinnEngdahl;
import edu.sc.seis.anhinga.database.JDBCLocation;
import edu.sc.seis.anhinga.database.JDBCLocator;
import edu.sc.seis.anhinga.database.JDBCMagnitude;
import edu.sc.seis.anhinga.database.JDBCOrigin;
import edu.sc.seis.anhinga.database.JDBCParameterRef;
import edu.sc.seis.anhinga.database.JDBCPick;
import edu.sc.seis.anhinga.database.JDBCPredictedArrival;
import edu.sc.seis.anhinga.database.JDBCQuantity;
import edu.sc.seis.anhinga.database.JDBCUnit;
import edu.sc.seis.anhinga.database.NotFound;

/**
 * EventFinderImpl.java
 *
 *
 * Created: Thu Mar 22 16:15:27 2001
 *
 * @author Srinivasa Telukutla
 * @version $Id: EventFinderImpl.java 10959 2004-10-22 02:16:37Z crotwell $
 */

public class EventFinderImpl extends EventFinderPOA {

    public EventFinderImpl(JDBCEventAttr jdbcEventAttr,
               JDBCOrigin jdbcOrigin,
               JDBCLocator jdbcLocator,
               JDBCLocation jdbcLocation,
               JDBCCatalog jdbcCatalog,
               org.omg.PortableServer.POA poa) {

    this.poa = poa;
    this.jdbcEventAttr = jdbcEventAttr;
    this.jdbcOrigin = jdbcOrigin;
    this.jdbcLocator = jdbcLocator;
    this.jdbcLocation = jdbcLocation;
    this.jdbcCatalog = jdbcCatalog;

    }

    //
    // IDL:iris.edu/Fissures/IfEvent/EventFinder/query_events:1.0
    //
    /***/

   public  EventAccess[]
    query_events(edu.iris.Fissures.Area the_area,
                 edu.iris.Fissures.Quantity min_depth,
                 edu.iris.Fissures.Quantity max_depth,
                 edu.iris.Fissures.TimeRange time_range,
                 String[] search_types,
                 float min_magnitude,
                 float max_magnitude,
                 String[] catalogs,
                 String[] contributors,
                 int seq_max,
                 EventSeqIterHolder iter) {
    EventAccess[] eventAccess;
    logger.debug("In the method query_events of EventFinder");
    float minLat;
    float minLon;
    float maxLat;
    float maxLon;
    if(the_area instanceof BoxArea) {
        BoxArea box = (BoxArea)the_area;
        minLat = box.min_latitude;
        maxLat = box.max_latitude;

        minLon = box.min_longitude;
        maxLon = box.max_longitude;
    } else if (the_area instanceof GlobalArea) {
        minLat = -1*Float.MAX_VALUE;
        minLon = -1*Float.MAX_VALUE;
        maxLat = Float.MAX_VALUE;
        maxLon = Float.MAX_VALUE;
    } else {
        minLat = -1*Float.MAX_VALUE;
        minLon = -1*Float.MAX_VALUE;
        maxLat = Float.MAX_VALUE;
        maxLon = Float.MAX_VALUE;
    }
    Integer[]  eventIds;
    int counter;
    try {


        logger.debug("The minimum depth is "+min_depth.value);
        logger.debug("The maximum depth is "+max_depth.value);
        logger.debug("The min_magnitude is "+min_magnitude);
        logger.debug("The max_magnitude is "+max_magnitude);


        jdbcEventAttr.setJDBCOrigin(jdbcOrigin);
        eventIds = jdbcEventAttr.getOnConstraint(min_depth.value,
                             max_depth.value,
                             minLat,
                             maxLat,
                             minLon,
                             maxLon,
                             time_range.start_time,
                             time_range.end_time,
                             min_magnitude,
                             max_magnitude,
                             search_types,
                             catalogs,
                             contributors);
        eventAccess = new EventAccess[eventIds.length];
        for(counter = 0; counter < eventIds.length; counter++ ) {

        EventAccessImpl eventImpl = new EventAccessImpl(jdbcEventAttr,
                                eventIds[counter].intValue(),
                                jdbcOrigin,
                                jdbcLocator);

        org.omg.CORBA.Object obj = poa.create_reference_with_id( eventIds[counter].toString().getBytes(),
                                     EventAccessHelper.id()
                                     );


        eventAccess[counter] = EventAccessHelper.narrow(obj);
        if(eventAccess[counter] == null)
            logger.debug("The returned reference is null");


        }
        return eventAccess;

    }
    catch(SQLException sqle) {
        logger.error("SQL problem", sqle);
        throw new org.omg.CORBA.UNKNOWN(sqle.toString());  //send exception back to client.
    } catch(NotFound nfe) {
        logger.error("Not found", nfe);
        throw new org.omg.CORBA.UNKNOWN(nfe.toString()); //send exceptino back to client.
    } catch(Throwable e) {
        logger.error("Generic Exception ",e);
        throw new org.omg.CORBA.UNKNOWN(e.toString(), 1, CompletionStatus.COMPLETED_NO); // send exceptin back to client.

    }



    //and all the conditions and get eventattr's

    //return eventAccess;

    }

    //
    // IDL:iris.edu/Fissures/IfEvent/EventFinder/get_by_name:1.0
    //
    /***/

    public EventAccess[]
    get_by_name(String name)  {

    Integer[]  eventIds;
    int counter;
        EventAccess[] eventAccess;
    try {

        logger.debug("Inside the Function get_byte_name ");
        eventIds = jdbcEventAttr.getByName(name);
        eventAccess = new EventAccess[eventIds.length];
        for(counter = 0; counter < eventIds.length; counter++ ) {

        /*EventAccessImpl eventImpl = new EventAccessImpl(jdbcEventAttr,
                                eventIds[counter].intValue(),
                                jdbcOrigin,
                                jdbcLocator);
        */
        org.omg.CORBA.Object obj = poa.create_reference_with_id( eventIds[counter].toString().getBytes(),
                                     EventAccessHelper.id()
                                     );

        eventAccess[counter] = EventAccessHelper.narrow(obj);
        //System.out.println("Before calling getpreferredorigin int eventfinderimpl");
        //  eventAccess[counter].get_preferred_origin();
        if(eventAccess[counter] == null)
            logger.debug("The returned reference is null");

        String ior = _orb().object_to_string(obj);
        logger.debug("The IOR is "+ior);
        }
        return eventAccess;

    } catch(SQLException sqle) {
        logger.error("SQL Exception ",sqle);
        throw new org.omg.CORBA.UNKNOWN(sqle.toString()); // send exception back to client
    } catch(NotFound nfe) {
        logger.error("Not Found Exception ", nfe);
        throw new org.omg.CORBA.UNKNOWN(nfe.toString()); // send exceptin back to client.
    } catch(Exception e) {
        logger.error("Generic Exception ",e);
        throw new org.omg.CORBA.UNKNOWN(e.toString()); // send exceptin back to client.

    }

    }


      //
    // IDL:iris.edu/Fissures/IfEvent/EventMgr/a_factory:1.0
    //
    /***/

    public EventFactory
    a_factory() {
    /*  EventFactory eventFactory = (EventFactory) EventFactoryImpl(jdbcParameterRef,
                                    jdbcEventAttr,
                                    jdbcFlinnEngdahl,
                                    poa);
    */
    //throw new org.omg.CORBA.NO_IMPLEMENT();
    return eventFactory;

    }

     //
    // IDL:iris.edu/Fissures/IfEvent/EventDC/a_finder:1.0
    //


    public EventFinder
    a_finder() {

    return eventFinder;//this._this();

    }

    //
    // IDL:iris.edu/Fissures/IfEvent/EventDC/a_channel_finder:1.0
    //


    public EventChannelFinder
    a_channel_finder() {

        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public String[] known_catalogs() {
    try {
        return jdbcCatalog.getAllCatalogs();
    } catch(SQLException sqle) {
        logger.error("sql problem", sqle);
        throw new org.omg.CORBA.UNKNOWN(sqle.toString()); // send exception back to client
    }
    }

    public String[] known_contributors() {

    try {
        return jdbcCatalog.getAllContributors();
    } catch(SQLException sqle) {
        logger.error("sql problem", sqle);
        throw new org.omg.CORBA.UNKNOWN(sqle.toString()); // send exception back to client
    }

    }

    public String[] catalogs_from(String the_contributor) {
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


    protected JDBCEventAttr jdbcEventAttr;

    protected JDBCLocator jdbcLocator;

    protected JDBCOrigin jdbcOrigin;

    protected JDBCLocation jdbcLocation;

    protected JDBCParameterRef jdbcParameterRef;

    protected JDBCPredictedArrival jdbcPredictedArrival;

    protected JDBCFlinnEngdahl jdbcFlinnEngdahl;

    protected JDBCUnit jdbcUnit;

    protected JDBCQuantity jdbcQuantity;

    protected JDBCPick jdbcPick;

    protected JDBCMagnitude jdbcMagnitude;

    protected JDBCCatalog jdbcCatalog;

    org.omg.PortableServer.POA poa;

    static Category logger =
        Category.getInstance(EventFinderImpl.class.getName());

} // EventFinderImpl
