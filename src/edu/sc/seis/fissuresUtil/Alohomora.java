package edu.sc.seis.fissuresUtil;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
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
        String darkFileName = System.getProperty(DARK_MAGIC_PASSWORD_FILE);
        Properties props = new Properties();
        if(darkFileName == null) {
            logger.info("Loading Alohomora passwords from system properties");
            Properties sys = System.getProperties();
            Iterator it = sys.keySet().iterator();
            while(it.hasNext()) {
                String key = (String)it.next();
                if(key.startsWith("darkMagic.")
                        && !key.equals(DARK_MAGIC_PASSWORD_FILE)) {
                    props.put(key, sys.getProperty(key));
                }
            }
        } else {
            logger.info("Loading Alohomora passwords from " + darkFileName);
            props.load(new BufferedInputStream(new FileInputStream(darkFileName)));
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        props.store(out, "darkMagic passwords");
        propBytes = out.toByteArray();
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
