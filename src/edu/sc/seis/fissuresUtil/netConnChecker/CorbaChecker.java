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
    public CorbaChecker(CORBAConfig conncheckerobjectgiven, Checker checker){
        conncheckerobject = conncheckerobjectgiven;

	this.checker = checker;
      
    }

   /** Pre: A Runnable thread calls run()
    *  Post: Attempt to make a Corba connection  
    */
   public void run()  {
       
        long begintime = System.currentTimeMillis();	
       logger.debug("***** Testing Corba Connection:*****");	  
       
        try {
	    Thread.sleep(2000);
	     
       
            if((conncheckerobject.getrootObj())._non_existent() == true){
	       logger.warn("No connection to Corba.");
               conncheckerobject.setSuccessful(false);
	       conncheckerobject.checker.fireStatusChanged(conncheckerobject.getName(), ConnStatus.FAILED);
	    }else {
               conncheckerobject.setSuccessful(true);
	       conncheckerobject.checker.fireStatusChanged(conncheckerobject.getName(), ConnStatus.SUCCESSFUL);
	       
	    }
            long endtime = System.currentTimeMillis();
            long duration = endtime-begintime;
            conncheckerobject.setTime(duration);
            //logger.debug
		System.out.println("Corba connection: "+ conncheckerobject.getName()+" "+conncheckerobject.getTime()+" milliseconds"); 
		return;
         } catch(COMM_FAILURE cf){
	     //logger.warn
	     conncheckerobject.setSuccessful(false);
	     System.out.println("!!!!! CORBA CONNECTION FAILURE.");
	       conncheckerobject.checker.fireStatusChanged(conncheckerobject.getName(), ConnStatus.FAILED);
             
	 } catch(Exception e) {
	     conncheckerobject.setSuccessful(false);
	     e.printStackTrace();
	       conncheckerobject.checker.fireStatusChanged(conncheckerobject.getName(), ConnStatus.FAILED);
	 }
       conncheckerobject.setFinished(true);
   }// close run

    CORBAConfig conncheckerobject;
    Checker checker;
    org.omg.CORBA.Object CorbaChecker_rootObj;
    static Category logger = Category.getInstance(CorbaChecker.class);
    
   
}// CorbaChecker class

/************************************************************/

