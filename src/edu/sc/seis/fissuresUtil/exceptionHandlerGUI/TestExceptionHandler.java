package edu.sc.seis.fissuresUtil.exceptionHandlerGUI;

import java.lang.*;

/**
 * TestExceptionHandler.java
 *
 *
 * Created: Thu Jan 31 17:03:09 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class TestExceptionHandler {
    public TestExceptionHandler (){
    
    }
    public static void main(String args[]) {
    String num = "abcd";
    
    try {
        
        int no = Integer.parseInt(num);
    } catch(Exception e) {

        ExceptionHandlerGUI.handleException("Number Format Exception", e);
    }
    }
}// TestExceptionHandler
