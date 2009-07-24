package edu.sc.seis.fissuresUtil;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;
import org.apache.log4j.Logger;
import org.omg.CORBA.LocalObject;
import org.omg.IOP.ServiceContext;
import org.omg.PortableInterceptor.ClientRequestInfo;
import org.omg.PortableInterceptor.ClientRequestInterceptor;
import org.omg.PortableInterceptor.ForwardRequest;
import org.omg.PortableInterceptor.ORBInitInfo;
import org.omg.PortableInterceptor.ORBInitializer;

/**
 * @author crotwell Created on Feb 10, 2005
 */
public class Alohomora extends LocalObject implements ClientRequestInterceptor,
        ORBInitializer {

    public Alohomora() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        getPasswordProps().store(out, "darkMagic passwords");
        propBytes = out.toByteArray();
    }

    /** ORBInitializer Impl */
    public void post_init(ORBInitInfo info) {}

    public void pre_init(ORBInitInfo info) {
        try {
            logger.debug("Alohomora pre_init");
            info.add_client_request_interceptor(this);
            logger.debug("Alohomora registered with orb");
        } catch(Throwable t) {
            logger.error("Exception adding Alohomora to orb", t);
        }
    }

    /** ClientRequestInterceptor Impl */
    public void send_request(ClientRequestInfo info) throws ForwardRequest {
        ServiceContext context = new ServiceContext(ID, propBytes);
        info.add_request_service_context(context, true);
    }

    public void send_poll(ClientRequestInfo arg0) {}

    public void receive_reply(ClientRequestInfo arg0) {}

    public void receive_exception(ClientRequestInfo arg0) throws ForwardRequest {}

    public void receive_other(ClientRequestInfo arg0) throws ForwardRequest {}

    public String name() {
        return "Alohomora";
    }

    public void destroy() {}

    public static Properties getPasswordProps() throws FileNotFoundException,
            IOException {
        return getPasswordProps(System.getProperties());
    }

    public static Properties getPasswordProps(Properties baseProperties)
            throws FileNotFoundException, IOException {
        String darkFileName = baseProperties.getProperty(DARK_MAGIC_PASSWORD_FILE);
        Properties props = new Properties();
        if(darkFileName == null) {
            logger.info("Loading darkMagic passwords from system properties");
            Iterator it = baseProperties.keySet().iterator();
            while(it.hasNext()) {
                String key = (String)it.next();
                if(key.startsWith("darkMagic.")
                        && !key.equals(DARK_MAGIC_PASSWORD_FILE)) {
                    logger.info("Adding password for " + key);
                    props.put(key, baseProperties.getProperty(key));
                }
            }
        } else {
            logger.info("Loading darkMagic passwords from " + darkFileName);
            props.load(new BufferedInputStream(new FileInputStream(darkFileName)));
        }
        return props;
    }

    private byte[] propBytes;

    public static final int ID = 3948;

    private static final String DARK_MAGIC_PASSWORD_FILE = "darkMagic.passwordFile";

    private static final Logger logger = Logger.getLogger(Alohomora.class);
}
