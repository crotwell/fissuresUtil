/**
 * GlobalExceptionHandler.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.exceptionHandlerGUI;

import javax.swing.JFrame;
import org.apache.log4j.Logger;

public class GlobalExceptionHandler {
    
    public void handle(Throwable thrown) {
        handle("An uncaught exception has occured. Please report this to geebugs@seis.sc.edu", thrown);
    }
    
    public void handle(String message, Throwable thrown) {
        logger.error(message, thrown);
        // try to not open a new frame if the existin one is visible
        if (frame == null || ! frame.isVisible()) {
            ExceptionHandlerGUI exceptH =
                ExceptionHandlerGUI.getExceptionHandlerGUI(message, thrown);
            frame = exceptH.display();
            //frame.pack();
            frame.show();
        }
    }
    
    public static void handleStatic(Throwable thrown) {
        GlobalExceptionHandler g = new GlobalExceptionHandler();
        g.handle(thrown);
    }
    
    public static void handleStatic(String message, Throwable thrown) {
        GlobalExceptionHandler g = new GlobalExceptionHandler();
        g.handle(message, thrown);
    }
    
    Logger logger = Logger.getLogger(GlobalExceptionHandler.class);
    
    static JFrame frame = null;
}

