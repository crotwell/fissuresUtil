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
 * Description: The Checker class receives a Collection of ConnCheckerConfig
 * objects with the name, type of connection to perform and its associated 
 * information, such as a website address in the case of an HTML connection or
 * a Corba object in the case of a Corba connection.
 * Each attempt to a connection runs in independent threads, the HTTPChecker 
 * and CorbaChecker make the connection and update the fields in the
 * ConnCheckerConfig object of this collection.
 *
 * Checker.java
 *
 *
 * @Created Sept 11, 2001 
 *
 * @author Georgina Coleman
 * @version 0
 *
 *
 */

public class Checker {


    /**  Constructor receives a collection of ConnCheckerConfig objects
     * @param  connCheckerCollectionReceived A Collection of ConncheckerConfig objects
     * @returns
     *    
     */
    public Checker(Collection connCheckerCollectionReceived) {

        ConnCheckerCollection = connCheckerCollectionReceived;
	try{
 	    makeChecksFromConfig();
	}catch(IOException e){
	    logger.warn("Could not perform makeChecksFromConfig");
	}
    }

     /**  An iterator returns the ConnCheckerConfig objects from the collection 
     * and calls the apropriate ConnChecker to try to connect to it. 
     * @param  
     * @returns
     *    
     */
   public void makeChecksFromConfig() throws IOException {

     
       String type, name, destination;  
       int sizeofCollection = ConnCheckerCollection.size();

       logger.debug("\nSize of collection is: "+sizeofCollection);

       ConnChecker site=null;
   
       ConnCheckerConfig objfromcollection;
       Iterator collectionExe = ConnCheckerCollection.iterator();

       while(collectionExe.hasNext()){

	    /* Below:Iterator returns the ConncheckerConfig object */
            objfromcollection =  (ConnCheckerConfig)collectionExe.next();
          
           
            if(objfromcollection instanceof HTTPConfig){
               site = new HTTPChecker((HTTPConfig)objfromcollection);
            } else

            if(objfromcollection instanceof CORBAConfig){             
               site = new CorbaChecker((CORBAConfig)objfromcollection);              
            }else {
		logger.debug("skipping");
	    }

            Thread th = new Thread(site);
            th.start();          
	        
       }     
    
    } // close makeChecks

    /** Returns how many connections were finished
     * at the moment the call was made to this method.
     */   
    public int checkStatus(){

       ConnCheckerConfig objfromcollection;
       Iterator collectionExe = ConnCheckerCollection.iterator();
       int sizeofCollection = ConnCheckerCollection.size();
       int totalfinished = 0;

       while(collectionExe.hasNext()){
          objfromcollection = (ConnCheckerConfig) collectionExe.next();
	  if(objfromcollection.getFinished() == true){
	      totalfinished+=1;
          }
       }

       logger.debug("\nTotal finished connections are: "+totalfinished+" from "+sizeofCollection);
       return totalfinished;            

    }

    /** Returns a Collection of a list of successful connections. 
     *    
     */
  public Collection getSuccessfulConnections(){

       ConnCheckerConfig objfromcollection;      
       Iterator collectionExe = ConnCheckerCollection.iterator();
       boolean conn = false;
       Collection collectionofSuccessful=new LinkedList();   

       logger.debug("\nThe succesful connections are: ");

       while(collectionExe.hasNext()){

          objfromcollection = (ConnCheckerConfig) collectionExe.next();
	  conn = objfromcollection.getSuccessful();

	  if(conn == true){
	      collectionofSuccessful.add(objfromcollection);
	      logger.debug("Successful: " + objfromcollection.getName());
	  }       

       }//close while
       return collectionofSuccessful;
    
    }

    /**  Returns a Collection of a list of finished connections. 
     *    
     */
 public Collection getFinishedConnections(){

       ConnCheckerConfig objfromcollection;
       Iterator collectionExe = ConnCheckerCollection.iterator();
       boolean conn = false;
       Collection collectionofFinished=new LinkedList();   
       logger.debug("\nThe finished connections are: ");
       while(collectionExe.hasNext()){
          objfromcollection = (ConnCheckerConfig) collectionExe.next();
	  conn = objfromcollection.getFinished();
	  if(conn == true){
             collectionofFinished.add(objfromcollection);
	     logger.debug("Finished: " + objfromcollection.getName());
	  }       
       }//close while
       return collectionofFinished;

    }

    /** Returns a Collection of a list of unsuccessful connections. 
     *    
     */
 public Collection  getUnsuccessfulConnections(){

       ConnCheckerConfig objfromcollection;
       Iterator collectionExe = ConnCheckerCollection.iterator();
       boolean conn = false;
       Collection collectionofFailed=new LinkedList(); 
       logger.debug("\nThe failed connections are: ");
       while(collectionExe.hasNext()){
          objfromcollection = (ConnCheckerConfig) collectionExe.next();
	  conn = objfromcollection.getSuccessful();
	  if(conn == false){
              collectionofFailed.add(objfromcollection);
	      logger.debug("Failed: " + objfromcollection.getName());
          }
       }//close while
       return collectionofFailed;

    }

    /**  Returns a Collection of a list of unfinished connections. 
     *    
     */
 public Collection getUnfinishedConnections(){

       ConnCheckerConfig objfromcollection;
       Iterator collectionExe = ConnCheckerCollection.iterator();
       boolean conn = false;
       Collection collectionofUnfinished=new LinkedList();   
       logger.debug("\nThe unfinished connections are: ");
       while(collectionExe.hasNext()){
          objfromcollection = (ConnCheckerConfig) collectionExe.next();
	  conn = objfromcollection.getFinished();
	  if(conn == false){
              collectionofUnfinished.add(objfromcollection);
              logger.debug("Unfinished: " + objfromcollection.getName());
	  }       
       }//close while
       return collectionofUnfinished;
    }

    private Collection ConnCheckerCollection;/*LinkedList collection*/
    static Category logger = Category.getInstance(Checker.class);
  

}// Checker class

/************************************************************/
