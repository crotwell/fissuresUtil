package edu.sc.seis.fissuresUtil.netConnChecker;

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
 * Description: Stand-alone testing application to check connections.
 * It reads a file in a standard format, makes the apropriate ConnCheckerConfig 
 * object, puts the object in a Collection then sends it to Checker, that 
 * will perform the apropriate connection check and you can call several methods that
 * will return a Collection of successful, finished, unsuccessful, or unfinished
 * ConnCheckerConfig objects.
 *
 *
 * TestChecker.java
 *
 *
 * @Created Sept 12, 2001 
 *
 * @author Georgina Coleman
 * @version 0
 */

public class TestChecker {

  
    /**  Makes a file with a list of connections to be checked.
     *    
     */
    static public void makeFile() {

	String filetocheck = new String();

	try{
	FileWriter out = new FileWriter("ConnectionList.txt");
        out.write("seis\n");
        out.write("HTTP\n");
	out.write("http://www.seis.sc.edu\n");
        out.write("corba connection\n");
	out.write("CORBA\n");
	out.write("\n");
        out.close();   
	}catch(FileNotFoundException fe){
	}catch(IOException ioe){
	}
        filetocheck="ConnectionList.txt";
    }

   /**   Reads a file with a list of checks to perform.
         Each line should have the name, type of check, and 
         the location to connect. Example of a valid file:
         seis
         HTTP
         http://www.seis.sc.edu
         corbaconnection
         CORBA
 
         google    
         HTTP
         http://www.google.com
    */
      
    public static void main (String []args) {

 
         /*================================================================*/
            /* Below:Tests the Checker constructor that receive a Collection */
                    
	          
        try {

            Collection connCheckerCollection = new LinkedList();
            ConnCheckerConfig configobj;

            File inputFile = new File("listofConnections.txt");
            FileReader fr = new FileReader(inputFile);
            BufferedReader bufferread = new BufferedReader(fr);
 
            String namefromfile=bufferread.readLine();
            String typefromfile=bufferread.readLine();
            String website = bufferread.readLine();  

            ConnChecker site=null;

            Properties props = System.getProperties();
	    props.put("org.omg.CORBA.ORBClass", "com.ooc.CORBA.ORB");
	    props.put("org.omg.CORBA.ORBSingletonClass",
                      "com.ooc.CORBA.ORBSingleton");

            // get some defaults
            String propFilename="CorbaChecker.prop";
            String defaultsFilename= "edu/sc/seis/NetConnChecker"+propFilename;

            FileInputStream in = new FileInputStream(propFilename);
            props.load(in);          	
            in.close();

       	   PropertyConfigurator.configure(props);

           // get a reference to the Naming Service root_context
	   org.omg.CORBA_2_3.ORB orb = (org.omg.CORBA_2_3.ORB)ORB.init(args,props);	  
           org.omg.CORBA.Object rootObj = orb.resolve_initial_references("NameService");

           if (rootObj == null) {
               logger.warn("Name service object is null!");
	    }  
  
           while(typefromfile!=null){
               if(typefromfile.equals("HTTP")){
		   URL urltocheck = new URL(website);
                   configobj = new HTTPConfig(namefromfile, urltocheck);
                   connCheckerCollection.add(configobj);               
               } else

               if(typefromfile.equals("CORBA")){
                   if (rootObj == null) {
                       logger.warn("Corba object is null!");
	           } else { 
                       configobj = new CORBAConfig(namefromfile, rootObj);
                       connCheckerCollection.add(configobj);
                   }                           
               }
               namefromfile=bufferread.readLine();
               typefromfile = bufferread.readLine();
               website = bufferread.readLine();          
           }
     
            collectiontocheck = new Checker(connCheckerCollection);
          	  	   	              
   	 } catch (FileNotFoundException f) {
                logger.warn(" file missing "+f+" using defaults");
         } catch (IOException f) {
                logger.warn(f.toString()+" using defaults");
         } catch(COMM_FAILURE cf){
                logger.warn("!!!!! CORBA CONNECTION FAILURE.");
         } catch(org.omg.CORBA.ORBPackage.InvalidName inv){
                logger.warn("!!!!! CORBA InvalidName Exception.");
	 }    

        try{    
	       Thread th = new Thread();
	       th.sleep(1000);
	}catch(InterruptedException e) {
	       logger.debug(e);
	}

	/* Below: Just checking the collection returned by Checker */
       Collection obtainedCollection = collectiontocheck.getSuccessfulConnections(); 
       ConnCheckerConfig objfromcollection;
       Iterator collectionExe = obtainedCollection.iterator();
     
       while(collectionExe.hasNext()){
          objfromcollection = (ConnCheckerConfig) collectionExe.next();
	  logger.debug("Successful_from_collection: " + objfromcollection.getName());       
       }//close while  
        
        collectiontocheck.getFinishedConnections();
        collectiontocheck.getUnsuccessfulConnections();
        collectiontocheck.getUnfinishedConnections();
        collectiontocheck.checkStatus();

     }// main

      static Checker collectiontocheck;  
      static Category logger = Category.getInstance(TestChecker.class);

}// TestChecker class

/************************************************************/
