
package edu.sc.seis.fissuresUtil.netConnChecker;

//import edu.sc.seis.sac.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.text.*;
import javax.swing.text.html.*;
import org.apache.log4j.*;

/**
 * Description: This implementation receives a ConnCheckerConfig object,
 * that contains 4 fields: name, URL,  finished and succesful.
 * HTTPChecker.java
 *
 * @Created Sept 10, 2001 
 *
 * @author Georgina Coleman
 * @version 0
 *
 */

public class HTTPChecker implements ConnChecker  {
 
   /** The constructor receives a ConnCheckerConfig object
    * @param conncheckerobject a ConnCheckerConfig object
    */ 
   public HTTPChecker(HTTPConfig conncheckerobjectgiven, Checker checker){

       conncheckerobject = conncheckerobjectgiven;
       this.checker = checker;

   }// constructor

        
   /** Pre:  A Runnable thread calls run()
    *  Post: Attempt to connect to a http site  
    *  and set the ConnCheckerConfig finished
    *  and successful to be true or false.
    */ 
   
    public void run ()  {
 
       long begintime;
       long endtime; 

     
       try{
	   Thread.sleep(2000);
           begintime = System.currentTimeMillis();         
	   URL seis = conncheckerobject.getURL();
           URLConnection seisConnection = seis.openConnection();
           InputStreamReader buffer = new InputStreamReader(seisConnection.getInputStream());
           BufferedReader bufferread = new BufferedReader(buffer);	  
           
           logger.debug(bufferread.readLine());

           endtime = System.currentTimeMillis();
           long duration = endtime-begintime;

	   conncheckerobject.setTime(duration);
           conncheckerobject.setSuccessful(true);
	   checker.fireStatusChanged(conncheckerobject.getName(), ConnStatus.SUCCESSFUL);
	   System.out.println(conncheckerobject.getName()+" is connected." );

           logger.debug("Connection to "+ conncheckerobject.getName()+" "+conncheckerobject.getTime()+" milliseconds"); 

       } catch (MalformedURLException urle) {
           conncheckerobject.setSuccessful(false);
	   checker.fireStatusChanged(conncheckerobject.getName(), ConnStatus.FAILED);
	   System.out.println(conncheckerobject.getName() + " Not connected");
       } catch (IOException ioe) {
           conncheckerobject.setSuccessful(false);  
	   System.out.println(conncheckerobject.getName() + " Not connected");
	   checker.fireStatusChanged(conncheckerobject.getName(), ConnStatus.FAILED);
       } catch(Exception e) {

	   e.printStackTrace();

       }
       
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
   
   } // run
    

    
  
   HTTPConfig conncheckerobject;
    Checker checker;
   static Category logger = Category.getInstance(HTTPChecker.class);

} // HTTPChecker class

/************************************************************/

