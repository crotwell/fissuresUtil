package edu.sc.seis.fissuresUtil.exceptionHandlerGUI;

/**
 * WrappedException.java
 *
 *
 * Created: Mon Feb 11 14:08:25 2002
 *
 * @author <a href="mailto:crotwell@pooh">Philip Crotwell</a>
 * @version
 */

public interface WrappedException {
    
    public Exception getCausalException();

}// WrappedException
