package edu.sc.seis.fissuresUtil;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;
import org.omg.IOP.ServiceContext;
import org.omg.PortableInterceptor.ClientRequestInfo;
import org.omg.PortableInterceptor.ClientRequestInterceptor;
import org.omg.PortableInterceptor.ForwardRequest;

/**
 * @author crotwell Created on Feb 10, 2005
 */
public class Alohomora extends org.omg.CORBA.LocalObject implements
        ClientRequestInterceptor {

    private static final String DARK_MAGIC_PASSWORD_FILE = "darkMagic.passwordFile";

    public Alohomora() throws IOException {
        Properties props = getPasswordProps();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        props.store(out, "darkMagic passwords");
        propBytes = out.toByteArray();
    }

    public static Properties getPasswordProps() throws FileNotFoundException,
            IOException {
        return getPasswordProps(System.getProperties());
    }

    public static Properties getPasswordProps(Properties baseProperties)
            throws FileNotFoundException, IOException {
        String darkFileName = baseProperties.getProperty(DARK_MAGIC_PASSWORD_FILE);
        new Properties();
        Properties props = new Properties();
        if(darkFileName == null) {
            logger.info("Loading darkMagic passwords from system properties");
            Iterator it = baseProperties.keySet().iterator();
            while(it.hasNext()) {
                String key = (String)it.next();
                if(key.startsWith("darkMagic.")
                        && !key.equals(DARK_MAGIC_PASSWORD_FILE)) {
                    props.put(key, baseProperties.getProperty(key));
                }
            }
        } else {
            logger.info("Loading darkMagic passwords from " + darkFileName);
            props.load(new BufferedInputStream(new FileInputStream(darkFileName)));
        }
        return props;
    }

    public void send_request(ClientRequestInfo info) throws ForwardRequest {
        ServiceContext context = new ServiceContext(id, propBytes);
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

    int id;

    byte[] propBytes;

    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(Alohomora.class);
}
