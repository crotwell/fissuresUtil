package edu.sc.seis.fissuresUtil.netConnChecker;

import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.text.*;
import javax.swing.text.html.*;
import org.apache.log4j.*;


/**
 * Description: class for a http connection object.
 *
 * HTTPConfig.java
 *
 * @created Sept 18, 2001 
 *
 * @author Georgina Coleman
 * @version 0
 */

public class HTTPConfig extends ConnCheckerConfig {


    /** Constructor receives name and URL to instantiate
     * a HTTPConfig object with the required values. It passes
     * the name to the super class.
     * @param nameofconn a reference name for the connection
     * @param destination an URL for the connection
     */
    public HTTPConfig(String nameofconn,URL destination, Checker checker )    {
	super(nameofconn, checker);
        dest_url=destination ;           
        
    }

    /** Pre: Public, receives no arguments
     *  Post: Returns the type of connection.
     */
    public String getType(){
	return "HTTP";
    }
  
    /** Pre: Public, receives no arguments
     *  Post: Returns URL of connection.
     *  @return String       destination of connection
     */   
    public URL getURL(){
	return dest_url;
    }
 

    /*  Variables */
 
    protected URL dest_url;

    static Category logger = Category.getInstance(HTTPConfig.class);


}//HTTPConfig class

/**********************************************************/

