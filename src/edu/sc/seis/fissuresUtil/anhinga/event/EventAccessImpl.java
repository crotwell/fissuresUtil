
package edu.sc.seis.anhinga.event;

import java.sql.SQLException;
import org.apache.log4j.Category;
import org.omg.CORBA.CompletionStatus;
import edu.iris.Fissures.AuditElement;
import edu.iris.Fissures.IfEvent.Event;
import edu.iris.Fissures.IfEvent.EventAccessPOA;
import edu.iris.Fissures.IfEvent.EventAttr;
import edu.iris.Fissures.IfEvent.EventChannelFinder;
import edu.iris.Fissures.IfEvent.EventFactory;
import edu.iris.Fissures.IfEvent.EventFinder;
import edu.iris.Fissures.IfEvent.Locator;
import edu.iris.Fissures.IfEvent.NoPreferredOrigin;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.IfEvent.OriginNotFound;
import edu.sc.seis.anhinga.database.JDBCEventAttr;
import edu.sc.seis.anhinga.database.JDBCLocator;
import edu.sc.seis.anhinga.database.JDBCOrigin;
import edu.sc.seis.anhinga.database.NotFound;


/**
 * EventAccessImpl.java
 *
 *
 * Created: Wed Mar 21 09:16:40 2001
 *
 * @author Srinivasa Telukutla
 * @version
 */

public class EventAccessImpl extends EventAccessPOA {


    public EventAccessImpl(JDBCEventAttr jdbcEventAttr, int eventid,
                           JDBCOrigin jdbcOrigin, JDBCLocator jdbcLocator) {
        this.eventid = eventid;
        this.jdbcEventAttr = jdbcEventAttr;
        this.jdbcLocator = jdbcLocator;
        this.jdbcOrigin = jdbcOrigin;

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
    /** */

    public edu.iris.Fissures.IfParameterMgr.ParameterComponent
        parm_svc(){
        throw new org.omg.CORBA.NO_IMPLEMENT();

    }

    //
    // IDL:iris.edu/Fissures/IfEvent/EventAccess/get_attributes:1.0
    //
    /***/

    public EventAttr
        get_attributes() {
        try {
            return jdbcEventAttr.get(this.eventid);
        } catch(NotFound nfe) {
            // System.out.println("The attributes are not found");
            logger.error("NotFound Exception ",nfe);
            throw new org.omg.CORBA.UNKNOWN(nfe.toString()); // send exception back to client
        } catch(SQLException sqle) {
            logger.error("SQL Exception ",sqle);
            sqle.printStackTrace();
            throw new org.omg.CORBA.UNKNOWN(sqle.toString()); // send exception back to client
        } catch (Throwable e) {
            logger.error("Caught a Throwable", e);
            throw new org.omg.CORBA.UNKNOWN(e.toString(), 1, CompletionStatus.COMPLETED_NO);
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
            logger.error("SQL Exception ",sqle);
            throw new org.omg.CORBA.UNKNOWN(sqle.toString()); // send exception back to client
        } catch(NotFound nfe) {
            logger.error("NotFound Exception ",nfe);
            throw new org.omg.CORBA.UNKNOWN(nfe.toString()); // send exception back to client
        } catch (Throwable e) {
            logger.error("Caught a Throwable", e);
            throw new org.omg.CORBA.UNKNOWN(e.toString(), 1, CompletionStatus.COMPLETED_NO);
        }


    }

    //
    // IDL:iris.edu/Fissures/IfEvent/EventAccess/get_origin:1.0
    //
    /***/

    public Origin
        get_origin(String the_origin)
        throws OriginNotFound{
        int originid;
        try {
            originid = Integer.parseInt(the_origin);
            logger.info("The origin id obtained is "+originid);
            Origin myOrigin = jdbcOrigin.get(originid);
            logger.info("The catalog is "+myOrigin.catalog);
            return myOrigin;
        } catch(NumberFormatException nfe) {
            logger.error("Illegal Number format ",nfe);
            throw new org.omg.CORBA.UNKNOWN(nfe.toString()); // send exception back to client
        } catch(NotFound ne) {
            logger.error("NotFound Exception ",ne);
            throw new org.omg.CORBA.UNKNOWN(ne.toString()); // send exception back to client
        } catch(SQLException sqle) {

            logger.error("SQL Exception ",sqle);
            sqle.printStackTrace();
            throw new org.omg.CORBA.UNKNOWN(sqle.toString()); // send exception back to client
        } catch (Throwable e) {
            logger.error("Caught a Throwable", e);
            throw new org.omg.CORBA.UNKNOWN(e.toString(), 1, CompletionStatus.COMPLETED_NO);

        }
    }

    //
    // IDL:iris.edu/Fissures/IfEvent/EventAccess/get_preferred_origin:1.0
    //
    /***/

    public  Origin
        get_preferred_origin()
        throws NoPreferredOrigin{
        logger.debug("Int  The Function get_preferred_origin");
        logger.debug("THis MUST BE PRINTED");
        try {
            if(jdbcEventAttr == null)
                logger.debug("THE Event Attribute is NULL ");
            else
                logger.debug("The EventAttribute is Not NULL "+this.eventid);

            Origin myOrigin;
            myOrigin = jdbcEventAttr.getPreferredOriginOnEventId(this.eventid);
            logger.debug("The name of the Preferred Origin is "+myOrigin.catalog);
            return myOrigin;
        } catch (NotFound nfe) {

            logger.error("NotFound Exception ",nfe);
            throw new org.omg.CORBA.UNKNOWN(nfe.toString()); // send exception back to client

        } catch(SQLException sqle) {

            logger.error("SQL Exception ",sqle);
            throw new org.omg.CORBA.UNKNOWN(sqle.toString()); // send exception back to client
        } catch (Throwable e) {
            logger.error("Caught a Throwable", e);
            throw new org.omg.CORBA.UNKNOWN(e.toString(), 1, CompletionStatus.COMPLETED_NO);
        }


    }

    //
    // IDL:iris.edu/Fissures/IfEvent/EventAccess/get_locators:1.0
    //
    /**This Function returns an array of locators given the origin_dbid as a string.**/

    public Locator[]
        get_locators(String an_origin)
        throws OriginNotFound,
        edu.iris.Fissures.NotImplemented{
        int originid;
        try {
            originid = Integer.parseInt(an_origin);
            Locator[] locators = jdbcLocator.getLocatorsGivenOriginId(originid);


            for(int i = 0; i <  locators.length; i++)
                logger.debug("The pick is "+ locators[i].a_pick.name);
            return locators;

        } catch(NumberFormatException nfe) {
            logger.error("Illegal Number format ",nfe);
            throw new org.omg.CORBA.UNKNOWN(nfe.toString()); // send exception back to client
        } catch(SQLException sqle) {
            logger.error("SQL Exception ",sqle);
            throw new org.omg.CORBA.UNKNOWN(sqle.toString()); // send exception back to client
        } catch(NotFound nfe) {
            logger.error("NotFound Exception ",nfe);
            throw new org.omg.CORBA.UNKNOWN(nfe.toString()); // send exception back to client
        } catch (Throwable e) {
            logger.error("Caught a Throwable", e);
            throw new org.omg.CORBA.UNKNOWN(e.toString(), 1, CompletionStatus.COMPLETED_NO);
        }
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

    public EventFactory
        a_factory() {

        throw new org.omg.CORBA.NO_IMPLEMENT();

    }

    //
    // IDL:iris.edu/Fissures/IfEvent/EventDC/a_finder:1.0
    //
    /***/

    public EventFinder
        a_finder() {

        throw new org.omg.CORBA.NO_IMPLEMENT();

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


    protected int eventid;

    protected JDBCEventAttr jdbcEventAttr;

    protected JDBCOrigin jdbcOrigin;

    protected JDBCLocator jdbcLocator;

    private static Category logger =
        Category.getInstance(EventAccessImpl.class.getName());

} // EventAccessImpl
