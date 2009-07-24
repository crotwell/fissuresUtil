package edu.sc.seis.fissuresUtil.exceptionHandler;


public interface PostProcess {
    
    public void process(String message, Throwable thrown);
    
}
