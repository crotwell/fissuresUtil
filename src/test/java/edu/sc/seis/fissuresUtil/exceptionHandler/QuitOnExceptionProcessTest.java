package edu.sc.seis.fissuresUtil.exceptionHandler;

import junit.framework.TestCase;


public class QuitOnExceptionProcessTest extends TestCase {
  
    // careful about these tests as they quit the jvm!
    
    public void testQuit() {
//        GlobalExceptionHandler.handle("just a test", new OutOfMemoryError());
//        fail("after out of memory");
    }
    
    public void testWrappedQuit() {
//        GlobalExceptionHandler.handle("just a test", new RuntimeException(new OutOfMemoryError()));
//        fail("after wrapped out of memory");
    }
}
