/**
 * GlobalExceptionHandler.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.exceptionHandlerGUI;

public class GlobalExceptionHandler
{

    public void handle(Throwable thrown) {
        ExceptionHandlerGUI.getExceptionHandlerGUI("An uncaught exception has occured. Please report this to geebugs@seis.sc.edu", thrown);
    }

}

