// ********************************************************************* 
//
// Copyright (c) 2000
// Object Oriented Concepts, Inc.
// Billerica, MA, USA
// 
// All Rights Reserved
//
// **********************************************************************
package edu.sc.seis.anhinga.event;

import edu.sc.seis.anhinga.database.JDBCCatalog;
import edu.sc.seis.anhinga.database.JDBCEventAttr;
import edu.sc.seis.anhinga.database.JDBCLocation;
import edu.sc.seis.anhinga.database.JDBCLocator;
import edu.sc.seis.anhinga.database.JDBCOrigin;


class EventLocator_impl extends org.omg.PortableServer.ServantLocatorPOA 
{
    
    public 
    EventLocator_impl(JDBCEventAttr jdbcEventAttr, int eventid, JDBCOrigin jdbcOrigin,
		      JDBCLocator jdbcLocator, JDBCLocation jdbcLocation,
		      JDBCCatalog jdbcCatalog,
		        org.omg.PortableServer.POA poa)
    {
	this.jdbcEventAttr = jdbcEventAttr;
	this.jdbcOrigin = jdbcOrigin;
	this.jdbcLocation = jdbcLocation;
	this.jdbcLocator = jdbcLocator;
	this.jdbcCatalog = jdbcCatalog;
	this.poa = poa;
    }
     public org.omg.PortableServer.Servant 
    preinvoke(
	byte[] oid, 
	org.omg.PortableServer.POA poa,        
	String operation, 
	org.omg.PortableServer.ServantLocatorPackage.CookieHolder cookie) 
	throws org.omg.PortableServer.ForwardRequest
    {
	String oid_string = new String(oid);

	System.out.println("The oid_string is "+oid_string);

	if(oid_string.indexOf("EventAccessImpl") != -1) {
	    System.out.println("The type of the object is EventAccess "+operation);

	    EventAccessImpl efinderimpl = new EventAccessImpl(jdbcEventAttr, 
							      Integer.parseInt(oid_string),
							      jdbcOrigin,
							      jdbcLocator);
	    return efinderimpl;

	} else { 

	    System.out.println("The type of the object is Event not EventAccess "+operation);

	    EventImpl eventImpl = new EventImpl(jdbcEventAttr,
						Integer.parseInt(oid_string),
						jdbcOrigin,
						jdbcLocator,
						jdbcLocation);
	    return eventImpl;
	}
					    
	// System.out.println("Int the preinvoke after building eventfinderimpl");


	   //        return eventImpl;
	
    }
    
    public void 
    postinvoke( 
	byte[] oid, 
	org.omg.PortableServer.POA poa, 
	String operation, 
	java.lang.Object cookie, 
	org.omg.PortableServer.Servant servant) 
    {
	// Do nothing if this was an operation on the controller.
        //if(servant == m_ctrl)
            //return;
	
        // If the operation that just ran was a remove, we delete the
        // servant.
        //if(operation.equals("remove"))
            //servant = null;

	//m_ctrl = null;
    } 


    protected JDBCEventAttr jdbcEventAttr;

    protected JDBCOrigin jdbcOrigin;

    protected JDBCLocation jdbcLocation;

    protected JDBCLocator jdbcLocator;

    protected JDBCCatalog jdbcCatalog;

    org.omg.PortableServer.POA poa;
    
}
