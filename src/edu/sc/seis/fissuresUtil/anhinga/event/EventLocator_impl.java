package edu.sc.seis.fissuresUtil.anhinga.event;

import edu.sc.seis.fissuresUtil.database.event.JDBCEventAccess;

class EventLocator_impl extends org.omg.PortableServer.ServantLocatorPOA {

    public EventLocator_impl(JDBCEventAccess jdbcEventAccess, int eventid,
            org.omg.PortableServer.POA poa) {
        this.jdbcEventAccess = jdbcEventAccess;
        this.poa = poa;
    }

    public org.omg.PortableServer.Servant preinvoke(byte[] oid,
                                                    org.omg.PortableServer.POA poa,
                                                    String operation,
                                                    org.omg.PortableServer.ServantLocatorPackage.CookieHolder cookie)
            throws org.omg.PortableServer.ForwardRequest {
        String oid_string = new String(oid);
        System.out.println("The oid_string is " + oid_string);
        if(oid_string.indexOf("EventAccessImpl") != -1) {
            System.out.println("The type of the object is EventAccess "
                    + operation);
            EventAccessImpl efinderimpl = new EventAccessImpl(jdbcEventAccess,
                                                              Integer.parseInt(oid_string));
            return efinderimpl;
        } else {
            System.out.println("The type of the object is Event not EventAccess "
                    + operation);
            EventImpl eventImpl = new EventImpl(jdbcEventAccess,
                                                Integer.parseInt(oid_string));
            return eventImpl;
        }
    }

    public void postinvoke(byte[] oid,
                           org.omg.PortableServer.POA poa,
                           String operation,
                           java.lang.Object cookie,
                           org.omg.PortableServer.Servant servant) {
    // Do nothing if this was an operation on the controller.
    //if(servant == m_ctrl)
    //return;
    // If the operation that just ran was a remove, we delete the
    // servant.
    //if(operation.equals("remove"))
    //servant = null;
    //m_ctrl = null;
    }

    protected JDBCEventAccess jdbcEventAccess;

    org.omg.PortableServer.POA poa;
}