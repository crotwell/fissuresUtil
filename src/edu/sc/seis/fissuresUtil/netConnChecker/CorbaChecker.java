package edu.sc.seis.fissuresUtil.netConnChecker;

//import edu.sc.seis.sac.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.text.*;
import javax.swing.text.html.*;
import org.apache.log4j.*;
import org.omg.CORBA.*;
import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextPackage.*;



/**
 * Description: This implementation receives a ConnCheckerConfig object,
 * that contains 4 fields: name, Corba object, finished and succesful.
 *
 * CorbaChecker.java
 *
 *
 * @Created Sept 17, 2001 
 *
 * @author Georgina Coleman
 * @version 0
 */

public class CorbaChecker implements ConnChecker  {

   /** The constructor receives a ConnCheckerConfig object
    * 
    * @param conncheckerobject a ConnCheckerConfig object
    * @return 
    */ 
    public CorbaChecker(CORBAConfig conncheckerobjectgiven){
        conncheckerobject = conncheckerobjectgiven;
      
    }

   /** Pre: A Runnable thread calls run()
    *  Post: Attempt to make a Corba connection  
    */
   public void run()  {

       long begintime = System.currentTimeMillis();	
       logger.debug("***** Testing Corba Connection:*****");	  
       
        try {
       
            if((conncheckerobject.getrootObj())._non_existent() == true){
	       logger.warn("No connection to Corba.");
               conncheckerobject.setSuccessful(false);
	    }else {
               conncheckerobject.setSuccessful(true);
	    }
            long endtime = System.currentTimeMillis();
            long duration = endtime-begintime;
            conncheckerobject.setTime(duration);
            logger.debug("Corba connection: "+ conncheckerobject.getName()+" "+conncheckerobject.getTime()+" milliseconds"); 
         } catch(COMM_FAILURE cf){
               logger.warn("!!!!! CORBA CONNECTION FAILURE.");
               conncheckerobject.setSuccessful(false);
	 }
       conncheckerobject.setFinished(true);
  
     }// close run

    CORBAConfig conncheckerobject;
    org.omg.CORBA.Object CorbaChecker_rootObj;
    static Category logger = Category.getInstance(CorbaChecker.class);

}// CorbaChecker class

/************************************************************/

