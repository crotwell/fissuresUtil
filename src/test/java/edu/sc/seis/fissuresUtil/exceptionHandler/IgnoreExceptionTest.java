package edu.sc.seis.fissuresUtil.exceptionHandler;

import java.io.EOFException;
import java.util.List;

import org.junit.Test;
import static org.junit.Assert.*;


public class IgnoreExceptionTest {
    
    @Test
    public void ignoreEOF() {
        GlobalExceptionHandler.add(new ClassExceptionInterceptor(EOFException.class));
        GlobalExceptionHandler.add(new ExceptionReporter() {

            public void report(String message, Throwable e, List sections) throws Exception {
                didReport = true;
            }
            
        });
        didReport = false;
        GlobalExceptionHandler.handle(new EOFException());
        assertFalse(didReport);
        didReport = false;
        GlobalExceptionHandler.handle("test message", new EOFException());
        assertFalse(didReport);
        didReport = false;
        GlobalExceptionHandler.handle("test message", new Exception());
        assertTrue(didReport);
    }
    
    boolean didReport = false;
}
