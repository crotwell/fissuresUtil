package edu.sc.seis.fissuresUtil.eventHandlerGUI;

import java.lang.*;

/**
 * TestEventHandler.java
 *
 *
 * Created: Thu Jan 31 17:03:09 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class TestEventHandler {
    public TestEventHandler (){
	
    }
    public static void main(String args[]) {
	String num = "abcd";
	
	try {
	    
	    int no = Integer.parseInt(num);
	} catch(Exception e) {

	    EventHandlerGUI.handleException(e);
	    System.out.println("Number Format Exception");
	}
    }
}// TestEventHandler
