package edu.sc.seis.fissuresUtil;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;

import org.omg.CORBA.LocalObject;
import org.omg.IOP.ServiceContext;
import org.omg.PortableInterceptor.ClientRequestInfo;
import org.omg.PortableInterceptor.ClientRequestInterceptor;
import org.omg.PortableInterceptor.ForwardRequest;
import org.omg.PortableInterceptor.ORBInitInfo;
import org.omg.PortableInterceptor.ORBInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Robert Casey, IRIS DMC
 * @version 5/12/2011
 * Derivative of Philip Crotwell's Ahohomora class.  Extends client interceptor
 * compatibility with IRIS DMC's two-part authentication scheme.
 */
public class Dissendium extends LocalObject implements ClientRequestInterceptor, ORBInitializer {

    public Dissendium() throws IOException {
        baseProperties = System.getProperties();  // get the system properties

        // get the USC properties first
        // format: darkMagic.<NetYear>.<password>
        // e.g.    darkMagic.XA2005=myBigPassWord
        // or use  darkMagic.passwordFile     to point to a separate file listing the same properties
        //
        Properties props = getPasswordProps("darkMagic");
        // if we've got autentication props, then take advantage of the output stream store to get byte array
        // uscPropBytes gets passed to a ServiceContext for the interceptor
        if (props.size() != 0) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            props.store(out, "darkMagic passwords");
            uscPropBytes = out.toByteArray();
            out.close();
        } else {
            uscPropBytes = new byte[0];
        }

        // get the IRIS properties next
        // format: irisDmc.<Net>.<StartYr>.<EndYr>=<password>:<email_addr>
        // e.g.    irisDmc.XX.2002.2005=myPasswd:me@myaccount.isp.com
	// or use  irisDmc.passwordFile which points to a separate file listing
        //              		authentication properties in VASE/JWEED format:
        // 				e.g. XX 2002 2005 myPasswd me@myaccount.isp.com
        //
        props = getPasswordProps("irisDmc");
        if (props.size() != 0) {
            StringBuffer propStrBuf = new StringBuffer();  // we're creating a big string
            Iterator it = props.keySet().iterator();  // iterate through the filtered property list
            while(it.hasNext()) {
                String key = (String)it.next();
                // check to see if the property points to a separate password file
                if (key.equals(IRIS_PASSWORD_FILE)) {
	   	    String passFileName = props.getProperty(key);
		    if (passFileName != null) {
                        logger.info("reading from IRIS password file: " + key);
                        try {
                            BufferedReader br = new BufferedReader(new FileReader(passFileName));
                            String inStr;
                            while ((inStr = br.readLine()) != null) {
                                String[] parts = inStr.split(" ");
                                if (parts.length < 5) {
                                    logger.debug("password file error at line: " + inStr);
                                    logger.debug("Skipping ...");
                                    continue;
                                }
                                if (inStr.trim().length() > 0) {
                                    propStrBuf.append(inStr).append(";");
                                }
                            }
                            br.close();
                        } catch (Exception e) {
			    logger.error("Exception thrown while reading IRIS password file",e);
                        }
                    } else {  // no password filename in property
                        logger.debug("IRIS password file property with empty entry...skipping");
                    }
                } else {
                    // IRIS properties need to be reformatted for server compatibility
                    StringBuffer sb = new StringBuffer(32);
                    String[] keyBits = key.split("\\.");  // split on the periods in the key
                    if (keyBits.length < 4) {
                        logger.info("improperly formatted key (rejecting): " + key);
                        continue;
                    }
                    for (int i = 1; i < keyBits.length; i++) { // skip element 0, containing 'irisDmc'
                        sb.append(keyBits[i]);
                        sb.append(" ");  // white space separated
                    }
                    // that should have taken care of net.startyr.endyr
                    //
                    // now append password:email with space separation
                    String authVal = props.getProperty(key);
                    if (authVal == null) {
                        logger.info("empty auth property (rejecting)");
                        continue;
                    }
                    String[] authBits = authVal.split("\\:");
                    if (authBits.length < 2) {
                        logger.info("improperly formatted auth values (rejecting): " + authVal);
                        continue;
                    }
                    sb.append(authBits[0]);
                    sb.append(" ");
                    sb.append(authBits[1]);

		    // finally, add to the master buffer
                    propStrBuf.append(sb.toString()).append(";");
                }
            }  // next property from iterator

            // finally, convert the contents of the entire master buffer to a byte array
            irisPropBytes = propStrBuf.toString().getBytes();
        } else {
            irisPropBytes = new byte[0];
        }

    }



    /** ORBInitializer Impl */
    public void post_init(ORBInitInfo info) {}

    public void pre_init(ORBInitInfo info) {
        try {
            //logger.debug("Dissendium pre_init");
            // if any authentication properties were pulled out...
            if ( (uscPropBytes != null && uscPropBytes.length != 0) || (irisPropBytes != null && irisPropBytes.length != 0)  ) {
                info.add_client_request_interceptor(this);     // add ourselves as a client request interceptor instance
                logger.info("Dissendium registered with orb");
            } else {
                logger.info("Dissendium NOT registered with orb as no passwords specified.");
            }
        } catch(Throwable t) {
            logger.error("Exception adding Dissendium to orb", t);
        }
    }



    /** ClientRequestInterceptor Impl */

    // stack up the USC and IRIS authentication contexts to the interceptor instance
    public void send_request(ClientRequestInfo info) throws ForwardRequest {

        // set up the USC auth context
        ServiceContext context = new ServiceContext(uscID, uscPropBytes);
        info.add_request_service_context(context, true); // pipe the properties directly in

        // set up the IRIS auth context
        context = new ServiceContext(irisID, irisPropBytes);
        info.add_request_service_context(context, true);

    }



    public void send_poll(ClientRequestInfo arg0) {}
    public void receive_reply(ClientRequestInfo arg0) {}
    public void receive_exception(ClientRequestInfo arg0) throws ForwardRequest {}
    public void receive_other(ClientRequestInfo arg0) throws ForwardRequest {}
    public String name() {
        return "Dissendium";
    }
    public void destroy() {}

    // get properties for the key prefix, which is currently 'darkMagic' or 'irisDmc'
    public Properties getPasswordProps(String keyPrefix)
            throws FileNotFoundException, IOException {

        Properties returnProps = new Properties();
        if (keyPrefix.equals("darkMagic")) {

            // check to see whether there is an alternate dark magic password file
            String darkFileName = baseProperties.getProperty(DARK_MAGIC_PASSWORD_FILE);
            Properties props;
            if (darkFileName != null) {
                // pulling passwords from the alternate password file
                logger.info("Loading darkMagic passwords from " + darkFileName);
                props = new Properties();
                props.load(new BufferedInputStream(new FileInputStream(darkFileName)));
            } else {
                logger.info("Loading darkMagic passwords from system properties");
                props = baseProperties;  // use the default system properties instead
            }
            Iterator it = props.keySet().iterator();
            while(it.hasNext()) {
                String key = (String)it.next();
                if(key.startsWith("darkMagic.") && !key.equals(DARK_MAGIC_PASSWORD_FILE)) {
                    logger.info("Adding password for " + key);
                    returnProps.put(key, props.getProperty(key));
                }
            }

        } else if (keyPrefix.equals("irisDmc")) {

	    // let's just pull back all of the irisDmc-related properties, to be reformatted later
            logger.info("Loading IRIS passwords from system properties");
            Iterator it = baseProperties.keySet().iterator();
            while(it.hasNext()) {
                String key = (String)it.next();
                if (key.startsWith("irisDmc.")){
                    logger.info("Found IRIS property " + key);
                    returnProps.put(key, baseProperties.getProperty(key));
                }
            }

        }
        return returnProps;  // return the appropriate set of properties, or a zero-size set
    }
  
 
    public static void insertOrbProp(Properties props) {
        if ( ! props.containsKey(DISSENDIUM_ORB_PROP_NAME)) {
            props.put(DISSENDIUM_ORB_PROP_NAME, DISSENDIUM_ORB_PROP_VALUE);
        }
    }


    private byte[] uscPropBytes;
    private byte[] irisPropBytes;
    private Properties baseProperties;
    public static final int uscID = 3948;      // USC-darkMagic ServiceContext ID
    public static final int irisID = 9463002;  // IRIS DMC ServiceContext ID
    private static final String DARK_MAGIC_PASSWORD_FILE = "darkMagic.passwordFile";
    private static final String IRIS_PASSWORD_FILE = "irisDmc.passwordFile";
    public static final String DISSENDIUM_ORB_PROP_NAME = "org.omg.PortableInterceptor.ORBInitializerClass.edu.sc.seis.fissuresUtil.Dissendium";
    public static final String DISSENDIUM_ORB_PROP_VALUE = "edu.sc.seis.fissuresUtil.Dissendium";
    private static final Logger logger = LoggerFactory.getLogger(Dissendium.class);

}
