/**
 * GlobalExceptionHandler.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.exceptionHandlerGUI;

import javax.swing.JFrame;

public class GlobalExceptionHandler
{

    public void handle(Throwable thrown) {
        ExceptionHandlerGUI exceptH =
            ExceptionHandlerGUI.getExceptionHandlerGUI("An uncaught exception has occured. Please report this to geebugs@seis.sc.edu", thrown);
        JFrame frame = exceptH.display();
        //frame.pack();
        frame.show();
    }

    public static void handleStatic(Throwable thrown) {
        GlobalExceptionHandler g = new GlobalExceptionHandler();
        g.handle(thrown);
    }
}

