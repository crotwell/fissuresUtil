package edu.sc.seis.fissuresUtil.exceptionHandler;


public class MissingPropertyException extends Exception {
    
    public MissingPropertyException() {}
    
    public MissingPropertyException(String reason) {
        super(reason);
    }
    
    public MissingPropertyException(String reason, Throwable cause) {
        super(reason, cause);
    }
}
