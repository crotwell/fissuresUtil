package edu.sc.seis.fissuresUtil.netConnChecker;

//import edu.sc.seis.sac.*;
import edu.sc.seis.fissuresUtil.namingService.*;
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
 */

public class CorbaChecker extends ConcreteConnChecker  {

   /** The constructor receives a ConnCheckerConfig object
    * 
    * @param conncheckerobject a ConnCheckerConfig object
    * @return 
    */ 
    public CorbaChecker(org.omg.CORBA.Object obj, String description){
	super(description);
	this.obj = obj;
    }

    public CorbaChecker(String dns, Class interfaceName, String objectName, String description, FissuresNamingService fissuresNamingService) {
	super(description);
	this.dns = dns;
	this.interfaceClass = interfaceName;
	this.objectName = objectName;
	this.fissuresNamingServiceImpl = (FissuresNamingServiceImpl)fissuresNamingService;
	this.obj = null;

    }

   /** Pre: A Runnable thread calls run()
    *  Post: Attempt to make a Corba connection  
    */
   public void run()  {
       long begintime = System.currentTimeMillis();	
	       
       System.out.println("running the corbaChecker for "+getDescription());
       
        try {
	    if(obj == null) {
		try {
		    this.obj = fissuresNamingServiceImpl.resolve(dns, getInterfaceName(), objectName);
		} catch(Exception e) {
		    this.obj = null;
		    setFinished(true);
		    setTrying(false);
		    setUnknown(true);
		    System.out.println("CORBA UnKnown");
		    fireStatusChanged(getDescription(), ConnStatus.UNKNOWN);
		    return;
		}    
	    }

            if(obj._non_existent() == true){
		setFinished(true);
		setTrying(false);
		setSuccessful(false);
		System.out.println("Failed");
		fireStatusChanged(getDescription(), ConnStatus.FAILED);
	    }else {
		setFinished(true);
		setTrying(false);
		setSuccessful(true);
		System.out.println("successful");
		fireStatusChanged(getDescription(), ConnStatus.SUCCESSFUL);
	       
	    }
            long endtime = System.currentTimeMillis();
            long duration = endtime-begintime;
            //conncheckerobject.setTime(duration);
            //logger.debug
	    //System.out.println("Corba connection: "+ conncheckerobject.getName()+" "+conncheckerobject.getTime()+" milliseconds"); 
		return;
         } catch(COMM_FAILURE cf){
	     //logger.warn
	     setFinished(true);
	     setTrying(false);
	     setSuccessful(false);
	     System.out.println("!!!!! CORBA CONNECTION FAILURE.");
	     fireStatusChanged(getDescription(), ConnStatus.FAILED);
             
	 } catch(Exception e) {
	     setFinished(true);
	     setTrying(false);
	     setSuccessful(false);
	     e.printStackTrace();
	     fireStatusChanged(getDescription(), ConnStatus.FAILED);
	 }
	//conncheckerobject.setFinished(true);
    }// close run

  
    private String getInterfaceName() {

	Class[] interfacenames = interfaceClass.getInterfaces();
	String temp = "";
        for(int counter = 0; counter < interfacenames.length; counter++) {
      
            if(interfacenames[counter].getName().startsWith("edu.iris.Fissures")) {
                temp = interfacenames[counter].getName();
                break;
            }
        }

        StringTokenizer tokenizer = new StringTokenizer(temp, ".");
        String rtnValue = new String();
        while( tokenizer.hasMoreElements() ) {

            rtnValue = (String) tokenizer.nextElement();
        }
	if(rtnValue.indexOf("Operations") == -1) {
	    return rtnValue.substring(0, rtnValue.length());
	} else {
	    return rtnValue.substring(0, rtnValue.length() - "Operations".length());
	}

    }
    org.omg.CORBA.Object obj;
    static Category logger = Category.getInstance(CorbaChecker.class);
    private FissuresNamingServiceImpl fissuresNamingServiceImpl;
    private String dns;
    private String objectName;
    private Class interfaceClass;
    
   
}// CorbaChecker class

/************************************************************/

