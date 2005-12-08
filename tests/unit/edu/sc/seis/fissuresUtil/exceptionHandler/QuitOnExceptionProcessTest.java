package edu.sc.seis.fissuresUtil.exceptionHandler;

import junit.framework.TestCase;


public class QuitOnExceptionProcessTest extends TestCase {
    
    public void testQuit() {
        GlobalExceptionHandler.handle("just a test", new OutOfMemoryError());
        assertFalse("after out of memory", true);
        GlobalExceptionHandler.handle("just a test", new RuntimeException(new OutOfMemoryError()));
        assertFalse("after wrapped out of memory", true);
    }
}
