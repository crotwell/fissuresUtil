
package edu.sc.seis.fissuresUtil.netConnChecker;

//import edu.sc.seis.sac.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.text.*;
import javax.swing.text.html.*;
import org.apache.log4j.*;



public class HTTPChecker extends ConcreteConnChecker  {
 
   
   public HTTPChecker(String description, String url){
       super(description);
        this.url = url;
   }// constructor

  
    public void run ()  {
 
	System.out.println("running the HTTP Checker for "+getDescription());
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
	   System.out.println("Successful");
	   fireStatusChanged(getDescription(), ConnStatus.SUCCESSFUL);
       } catch (MalformedURLException urle) {
	   setTrying(false);
	   setFinished(true);
           setSuccessful(false);
	   setUnknown(true);
	   System.out.println("Unknown");
	   fireStatusChanged(getDescription(), ConnStatus.UNKNOWN);
       } catch (IOException ioe) {
	   setTrying(false);
	   setFinished(true);
	   setSuccessful(false);  
	   System.out.println("failed");
	   //System.out.println(conncheckerobject.getName() + " Not connected");
	   fireStatusChanged(getDescription(), ConnStatus.FAILED);
       } catch(Exception e) {
	   e.printStackTrace();
       }
       /*
       if(conncheckerobject.getSuccessful() != true){
	   checker.fireStatusChanged(conncheckerobject.getName(), ConnStatus.FAILED);
           //logger.debug
	   System.out.println(conncheckerobject.getName() + " Not connected");
       }else {
	   checker.fireStatusChanged(conncheckerobject.getName(), ConnStatus.SUCCESSFUL);
           //logger.debug
	   System.out.println(conncheckerobject.getName()+" is connected." );
       }

       conncheckerobject.setFinished(true);
       */
   } // run
    
    private String url;
   static Category logger = Category.getInstance(HTTPChecker.class);

} // HTTPChecker class

/************************************************************/

