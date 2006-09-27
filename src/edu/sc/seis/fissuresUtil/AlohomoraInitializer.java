package edu.sc.seis.fissuresUtil;

import org.apache.log4j.Logger;
import org.omg.CORBA.LocalObject;
import org.omg.PortableInterceptor.ORBInitInfo;
import org.omg.PortableInterceptor.ORBInitializer;

public class AlohomoraInitializer extends LocalObject implements ORBInitializer {

    public void post_init(ORBInitInfo info) {}

    public void pre_init(ORBInitInfo info) {
        try {
            logger.debug("Alohomora pre_init");
            // Allocate a slot id to use for the interceptor to indicate
            // that it is making an outcall. This is used to avoid
            // infinite recursion.
            info.allocate_slot_id();
            info.add_client_request_interceptor(new Alohomora());
            logger.debug("Alohomora registered with orb");
        } catch(Throwable t) {
            logger.error("Exception adding Alohomora to orb", t);
        }
    }

    private static final Logger logger = Logger.getLogger(AlohomoraInitializer.class);
}
