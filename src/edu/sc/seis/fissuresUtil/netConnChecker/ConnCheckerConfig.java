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
 * Description: Abstract class for a connection object.
 *
 *
 * @Created Sept 18, 2001 
 *
 * @author Georgina Coleman
 * @version 0
 */

public abstract class ConnCheckerConfig  {
 
    /** Receives the name of the connection and instantiate a
     *  ConnCheckerConfig object 
     */
    public ConnCheckerConfig(String namegiven, Checker checker) {
	this.checker = checker;
        name = namegiven;
	finished = false;
	successful=false;

    }

     public ConnCheckerConfig(String namegiven) {
  name = namegiven;
	finished = false;
	successful=false;

    }

    /** Pre: Public, receives no arguments
     *  Post: Returns the name of connection.
     */
    public String getName(){
	return name;
    }

    /** Pre: Public, receives true or false
     *  Post: Sets true if object has succesfully connected.
     *  @param status       true if  connection was successful.
     */
    public synchronized void setSuccessful(boolean status){
 	successful = status;
    }

    /** Pre: Public, receives no arguments
     *  Post: Returns true if object has successful connection.
     *  @return true       if successful connection
     */
    public synchronized boolean getSuccessful(){
	return successful;
    }      

    /** Pre: Public, receives true or false
     *  Post: Sets true if object has finished connection.
     *  @param status       true if finished connection
     */        
    public synchronized void setFinished(boolean status){
 	finished = status;
    }

    /** Pre: Public, receives no arguments
     *  Post: Returns true if object has finished connection.
     *  @return true       if finished connection
     */
    public synchronized boolean getFinished(){
	return finished;
    }

    /** Pre: Public, receives time elapsed to connect in millis
     *  Post: Set time taken to connect.
     *  @param time_elapsed_in_millis time taken to connect
     */   
    public synchronized void setTime(long time_elapsed_in_millis){
 	timetoconnect = time_elapsed_in_millis;
    }

    /** 
     *  Post: Return time taken to connect.
     *  @return timetoconnect  time taken to connect
     */   
    public synchronized  long getTime(){
	return timetoconnect;
    }

    public void setChecker(Checker checker) {
  
	this.checker = checker;

    }

    protected String name;
    protected boolean finished;
    protected boolean successful;
    protected long timetoconnect;

    protected Checker checker;

}//  ConnCheckerConfig class

/*************************************************************/

