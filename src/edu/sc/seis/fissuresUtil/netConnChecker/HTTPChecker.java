
package edu.sc.seis.fissuresUtil.netConnChecker;

import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.text.*;
import javax.swing.text.html.*;
import org.apache.log4j.*;



/**
 * Description: This class checks for HTTP connections. An HTTPChecker can be instantiated by 
 * passing the parameters description and url
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version 1.0
 */
public class HTTPChecker extends ConcreteConnChecker  {
 
   
    /**
     * Creates a new <code>HTTPChecker</code> instance.
     *
     * @param description a <code>String</code> value
     * @param url a <code>String</code> value
     */
    public HTTPChecker(String description, String url){
       super(description);
        this.url = url;
   }// constructor

  
    /**
     * starts the execution of HTTPChecker Thread.
     *
     */
    public void run ()  {
 
	long begintime;
	long endtime; 

     
       try{
	    begintime = System.currentTimeMillis();         
	   URL seis = new URL(this.url);
           URLConnection seisConnection = seis.openConnection();
           InputStreamReader buffer = new InputStreamReader(seisConnection.getInputStream());
           BufferedReader bufferread = new BufferedReader(buffer);	  
	   endtime = System.currentTimeMillis();
           long duration = endtime-begintime;
	   setTrying(false);
	   setFinished(true);
	   setSuccessful(true);
	   fireStatusChanged(getDescription(), ConnStatus.SUCCESSFUL);
       } catch (MalformedURLException urle) {
	   setTrying(false);
	   setFinished(true);
           setSuccessful(false);
	   setUnknown(true);
	   fireStatusChanged(getDescription(), ConnStatus.UNKNOWN);
       } catch (IOException ioe) {
	   setTrying(false);
	   setFinished(true);
	   setSuccessful(false);  
	   fireStatusChanged(getDescription(), ConnStatus.FAILED);
       } catch(Exception e) {
	   e.printStackTrace();
       }
    } // run
    
    private String url;
   static Category logger = Category.getInstance(HTTPChecker.class);

} // HTTPChecker class

/************************************************************/

