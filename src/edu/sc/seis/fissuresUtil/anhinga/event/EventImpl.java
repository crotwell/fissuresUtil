 package edu.sc.seis.anhinga.event;

import java.sql.SQLException;
import org.apache.log4j.Logger;
import edu.iris.Fissures.AuditElement;
import edu.iris.Fissures.IfEvent.Event;
import edu.iris.Fissures.IfEvent.EventAttr;
import edu.iris.Fissures.IfEvent.EventChannelFinder;
import edu.iris.Fissures.IfEvent.EventFactory;
import edu.iris.Fissures.IfEvent.EventFinder;
import edu.iris.Fissures.IfEvent.EventPOA;
import edu.iris.Fissures.IfEvent.Locator;
import edu.iris.Fissures.IfEvent.Magnitude;
import edu.iris.Fissures.IfEvent.NoPreferredOrigin;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.IfEvent.OriginNotFound;
import edu.iris.Fissures.IfParameterMgr.ParameterRef;
import edu.iris.Fissures.event.OriginImpl;
import edu.sc.seis.anhinga.database.JDBCEventAttr;
import edu.sc.seis.anhinga.database.JDBCLocation;
import edu.sc.seis.anhinga.database.JDBCLocator;
import edu.sc.seis.anhinga.database.JDBCOrigin;
import edu.sc.seis.anhinga.database.NotFound;


/**
 * EventImpl.java
 *
 *
 * Created: Fri Mar 23 09:30:07 2001
 *
 * @author Srinivasa Telukutla
 * @version
 */

public class EventImpl extends EventPOA {

    public EventImpl(JDBCEventAttr jdbcEventAttr, int eventid,
             JDBCOrigin jdbcOrigin, JDBCLocator jdbcLocator,
             JDBCLocation jdbcLocation) {
    this.eventid = eventid;
    this.jdbcEventAttr = jdbcEventAttr;
    this.jdbcLocator = jdbcLocator;
    this.jdbcOrigin = jdbcOrigin;
    this.jdbcLocation = jdbcLocation;

    }

     //
    // IDL:iris.edu/Fissures/IfEvent/Event/update_region:1.0
    //
    /***/

    public void
    update_region(edu.iris.Fissures.FlinnEngdahlRegion region,
                  edu.iris.Fissures.AuditInfo[] audit_info) {
    try {
        jdbcEventAttr.updateFlinnEngdahlRegion(eventid, region);
    } catch(SQLException sqle) {
        logger.error("Problem with SQL ", sqle);
        throw new org.omg.CORBA.INTERNAL(sqle.toString());
    } catch(edu.sc.seis.anhinga.database.NotFound nfe) {
        logger.error(" The Event with id " + eventid +" is Not Found", nfe);
        throw new org.omg.CORBA.INTERNAL(nfe.toString());
    }
    //to know wheter to update eventattr or FlinnEngdahl.
    }

    //
    // IDL:iris.edu/Fissures/IfEvent/Event/destroy:1.0
    //
    /***/

    public void
    destroy() {
    //not implemented.

    }

    //
    // IDL:iris.edu/Fissures/IfEvent/Event/add_origin:1.0
    //
    /** Add a origin to the event. If no origin exist the origin will
     * become the default until the default is reset by a add_origin
     * or set_preferred_origin.
     **/

    public String
    add_origin(edu.iris.Fissures.Time origin_time,
           edu.iris.Fissures.Location the_location,
           Magnitude[] magnitudes,
           Locator[] locators,
           boolean preferred,
           String the_catalog,
           String the_contributor,
           edu.iris.Fissures.AuditInfo[] audit_info) {

    try {

        ParameterRef[] params = new ParameterRef[0];

        Origin newOrigin =  (Origin)  new OriginImpl("id",
                          the_catalog,
                          the_contributor,
                          origin_time,
                          the_location,
                          magnitudes,
                          params);
        int originid = jdbcOrigin.put(newOrigin, eventid);
        if( preferred)
        set_preferred_origin(new Integer(originid).toString(), audit_info);
        //jdbcEventAttr.putPreferredOrigin(newOrigin, eventid);
        add_locators(new Integer(originid).toString(), locators, audit_info);
        return new Integer(originid).toString();

    }
    catch(SQLException sqle) {
        logger.error("Problem with SQL ", sqle);
        throw new org.omg.CORBA.INTERNAL(sqle.toString());
    } catch(edu.sc.seis.anhinga.database.NotFound nfe) {
        logger.error(" The Event with id " + eventid +" is Not Found", nfe);
        throw new org.omg.CORBA.INTERNAL(nfe.toString());
    } catch(edu.iris.Fissures.IfEvent.OriginNotFound onfe) {
        logger.error(" The origin is not Found ", onfe);
        throw new org.omg.CORBA.INTERNAL(onfe.toString());
    }


    //add_locators("3", locators, audit_info);

    //didnot understand.
    }

    //
    // IDL:iris.edu/Fissures/IfEvent/Event/add_locators:1.0
    //
    /***/

    public void
    add_locators(String the_origin,
                 Locator[] locators,
                 edu.iris.Fissures.AuditInfo[] audit_info)

        throws OriginNotFound {
    try {
        int counter;
        int originid;
        int locatorid;
        for ( counter = 0; counter < locators.length; counter++ ) {
        originid = Integer.parseInt( the_origin );
        locatorid = jdbcLocator.put(locators[counter]);
        jdbcLocator.updateLocatorOriginId(originid, locatorid);
        }
    } catch(SQLException sqle) {
        logger.error("Problem with SQL ", sqle);
        throw new org.omg.CORBA.INTERNAL(sqle.toString());
    } catch(edu.sc.seis.anhinga.database.NotFound nfe) {
        logger.error(" The Event with id " + eventid +" is Not Found", nfe);
        throw new org.omg.CORBA.INTERNAL(nfe.toString());
    } catch(NumberFormatException nfee) {
        logger.error(" Error while parsing the Stringified originid ",nfee);
        throw new org.omg.CORBA.INTERNAL(nfee.toString());
    }

    }

    //
    // IDL:iris.edu/Fissures/IfEvent/Event/add_magnitude:1.0
    //
    /***/

    public void
    add_magnitude(String the_origin,
              Magnitude a_magnitude,
              edu.iris.Fissures.AuditInfo[] audit_info)
        throws OriginNotFound {
    try {
        int originid = Integer.parseInt(the_origin);
        Magnitude[] magnitudes = new Magnitude[1];
        magnitudes[0] = a_magnitude;
        jdbcOrigin.putOriginMagnitudesArray(magnitudes, originid);
    }  catch(SQLException sqle) {
        logger.error("Problem with SQL ", sqle);
        throw new org.omg.CORBA.INTERNAL(sqle.toString());
    } catch(edu.sc.seis.anhinga.database.NotFound nfe) {
        logger.error(" The Event with id " + eventid +" is Not Found", nfe);
        throw new org.omg.CORBA.INTERNAL(nfe.toString());
    } catch(NumberFormatException nfee) {
        logger.error(" Error while parsing the Stringified originid ",nfee);
        throw new org.omg.CORBA.INTERNAL(nfee.toString());
    }

    }

    //
    // IDL:iris.edu/Fissures/IfEvent/Event/delete_origin:1.0
    //
    /***/

    public void
    delete_origin(String the_origin,
                  edu.iris.Fissures.AuditInfo[] audit_info)
        throws OriginNotFound {
    try {
        int originid = Integer.parseInt(the_origin);
        jdbcOrigin.deleteOrigin(originid);
    } catch(SQLException sqle) {
        logger.error("Problem with SQL ", sqle);
        throw new org.omg.CORBA.INTERNAL(sqle.toString());
    } catch(edu.sc.seis.anhinga.database.NotFound nfe) {
        logger.error(" The Event with id " + eventid +" is Not Found", nfe);
        throw new org.omg.CORBA.INTERNAL(nfe.toString());
    } catch(NumberFormatException nfee) {
        logger.error(" Error while parsing the Stringified originid ",nfee);
        throw new org.omg.CORBA.INTERNAL(nfee.toString());
    }


    }

    //
    // IDL:iris.edu/Fissures/IfEvent/Event/set_preferred_origin:1.0
    //
    /***/

    public void
    set_preferred_origin(String the_origin,
                         edu.iris.Fissures.AuditInfo[] audit_info)
        throws OriginNotFound {
    try {
        int originid = Integer.parseInt(the_origin);
        jdbcEventAttr.putPreferredOrigin(jdbcOrigin.get(originid),
                         eventid
                         );
    }  catch(SQLException sqle) {
        logger.error("Problem with SQL ", sqle);
    } catch(edu.sc.seis.anhinga.database.NotFound nfe) {
        logger.error(" The Event with id " + eventid +" is Not Found", nfe);
    } catch(NumberFormatException nfee) {
        logger.error(" Error while parsing the Stringified originid ",nfee);
    }
    }




     //
    // IDL:iris.edu/Fissures/IfEvent/EventAccess/a_writeable:1.0
    //
    /***/

    public Event
    a_writeable() {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    //
    // IDL:iris.edu/Fissures/IfEvent/EventAccess/parm_svc:1.0
    //
    /** Defines the ParameterMgr where parameters for this Event reside */

    public edu.iris.Fissures.IfParameterMgr.ParameterComponent
    parm_svc(){
        throw new org.omg.CORBA.NO_IMPLEMENT();

    }


     //
    // IDL:iris.edu/Fissures/IfEvent/EventAccess/get_audit_trail_for_origin:1.0
    //
    /***/

    public edu.iris.Fissures.AuditElement[]
    get_audit_trail_for_origin(String the_origin)
        throws OriginNotFound,
    edu.iris.Fissures.NotImplemented{
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

     //
    // IDL:iris.edu/Fissures/IfEvent/EventMgr/a_factory:1.0
    //
    /***/

    public EventFactory a_factory() {

    return eventFactory;

    }

     //
    // IDL:iris.edu/Fissures/IfEvent/EventDC/a_finder:1.0
    //
    /***/

    public EventFinder  a_finder() {

    return eventFinder;

    }

    //
    // IDL:iris.edu/Fissures/IfEvent/EventDC/a_channel_finder:1.0
    //
    /***/

    public EventChannelFinder
    a_channel_finder() {

        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

     public AuditElement[]
    get_audit_trail() {

        throw new org.omg.CORBA.NO_IMPLEMENT();

    }


      //
    // IDL:iris.edu/Fissures/IfEvent/EventAccess/get_attributes:1.0
    //
    /***/

    public EventAttr
    get_attributes() {
    System.out.println("In the Method get attributes");
    try {
        return jdbcEventAttr.get(this.eventid);
    } catch(NotFound nfe) {
        // System.out.println("The attributes are not found");
          System.out.println("SQL Exception "+nfe);
          nfe.printStackTrace();
        throw new org.omg.CORBA.INTERNAL(nfe.toString()); // send exception back to client
    } catch(SQLException sqle) {
          System.out.println("SQL Exception "+sqle);
          sqle.printStackTrace();
        throw new org.omg.CORBA.INTERNAL(sqle.toString()); // send exception back to client
    }

    }

    //
    // IDL:iris.edu/Fissures/IfEvent/EventAccess/get_origins:1.0
    //
    /***/

    public Origin[]
    get_origins(){

    try {

        return jdbcOrigin.getOrigins(eventid);
    } catch(SQLException sqle) {
          System.out.println("SQL Exception "+sqle);
          sqle.printStackTrace();
        throw new org.omg.CORBA.INTERNAL(sqle.toString()); // send exception back to client
    } catch(NotFound nfe) {
          System.out.println("SQL Exception "+nfe);
          nfe.printStackTrace();
        throw new org.omg.CORBA.INTERNAL(nfe.toString()); // send exception back to client
    }


    }

    //
    // IDL:iris.edu/Fissures/IfEvent/EventAccess/get_origin:1.0
    //
    /***/

    public Origin
    get_origin(String the_origin)
        {
    int originid;
    try {
        originid = Integer.parseInt(the_origin);
        return jdbcOrigin.get(originid);


    } catch(NumberFormatException nfe) {
        System.out.println("Illegal Number format "+nfe);
         throw new org.omg.CORBA.INTERNAL(nfe.toString());
    } catch(NotFound ne) {
          System.out.println("NotFound Exception "+ne);
          ne.printStackTrace();
        throw new org.omg.CORBA.INTERNAL(ne.toString()); // send exception back to client
    } catch(SQLException sqle) {

      System.out.println("SQL Exception "+sqle);
          sqle.printStackTrace();
        throw new org.omg.CORBA.INTERNAL(sqle.toString()); // send exception back to client

    }

    //throw new OriginNotFound("origin not found");

    }

    //
    // IDL:iris.edu/Fissures/IfEvent/EventAccess/get_preferred_origin:1.0
    //
    /***/

   public  Origin
    get_preferred_origin()
        throws NoPreferredOrigin{
       try {
       return jdbcEventAttr.getPreferredOriginOnEventId(eventid);
       } catch (NotFound nfe) {

         System.out.println("NotFound Exception "+nfe);
          nfe.printStackTrace();
        throw new org.omg.CORBA.INTERNAL(nfe.toString()); // send exception back to client

       } catch(SQLException sqle) {

         System.out.println("SQL Exception "+sqle);
          sqle.printStackTrace();
        throw new org.omg.CORBA.INTERNAL(sqle.toString()); // send exception back to client
       }


    }

    //
    // IDL:iris.edu/Fissures/IfEvent/EventAccess/get_locators:1.0
    //
    /**This function returns the Locators for a given origin
    given the origin_dbid as a string.**/

    public Locator[]
    get_locators(String an_origin)
        throws OriginNotFound,
    edu.iris.Fissures.NotImplemented{
    int originid;
    try {
        originid = Integer.parseInt(an_origin);


        return jdbcLocator.getLocatorsGivenOriginId(originid);

    } catch(NumberFormatException nfe) {
        System.out.println("Illegal Number format "+nfe);
    } catch(SQLException sqle) {
        System.out.println("SQL Exception "+sqle);
        sqle.printStackTrace();
        throw new org.omg.CORBA.INTERNAL(sqle.toString()); // send exception back to client
    } catch(NotFound nfe) {
          System.out.println("NotFound Exception "+nfe);
          nfe.printStackTrace();
        throw new org.omg.CORBA.INTERNAL(nfe.toString()); // send exception back to client
    }

    throw new OriginNotFound("origin not found");

    }


    public void setEventFinder(EventFinder eventFinder) {

    this.eventFinder = eventFinder;
    }

    public void setEventFactory(EventFactory eventFactory) {

    this.eventFactory = eventFactory;
    }

    private EventFinder eventFinder = null;

    private EventFactory eventFactory = null;

    protected int eventid = 3;

    protected JDBCEventAttr jdbcEventAttr;

    protected JDBCOrigin jdbcOrigin;

    protected JDBCLocator jdbcLocator;

    protected JDBCLocation jdbcLocation;

    private static final Logger logger = Logger.getLogger(EventImpl.class);

} // EventImpl
