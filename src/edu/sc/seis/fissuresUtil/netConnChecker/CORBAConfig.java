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
 * Description: class for a Corba connection object.
 *
 * CORBAConfig.java
 *
 * @created Sept 18, 2001 
 *
 * @author Georgina Coleman
 * @version 0
 */

public class CORBAConfig extends ConnCheckerConfig{

    /** Constructor receives name and a CORBA Object
     *  It passes the name to the super class.
     *  @param rootObjgiven a non null Corba object.
     */     
    public CORBAConfig (String namegiven, org.omg.CORBA.Object rootObjgiven, Checker checker) {
	super(namegiven, checker);
        ConnCheckerConfigrootObj = rootObjgiven;	   

    }// constructor
 
    /** Pre: Public, receives no arguments
     *  Post: Returns the type of connection.
     */
    public String getType(){
	return "CORBA";
    }

    /** Pre: Public, receives no arguments
     *  Post: Returns the org.omg.CORBA Object
     *  @return ConnCheckerConfigrootObj
     */
    public org.omg.CORBA.Object getrootObj(){
        return ConnCheckerConfigrootObj;
    }   


    protected org.omg.CORBA.Object ConnCheckerConfigrootObj; 

    static Category logger = Category.getInstance(CORBAConfig.class);


}//  CORBAConfig class

/***********************************************************/

