package edu.sc.seis.fissuresUtil.xml;

import edu.sc.seis.fissuresUtil.exceptionHandlerGUI.WrappedException;

/**
 * DataRetrievalException.java
 *
 *
 * Created: Fri Apr 11 14:57:41 2003
 *
 * @author <a href="mailto:crotwell@owl.seis.sc.edu">Philip Crotwell</a>
 * @version 1.0
 */
public class DataRetrievalException extends Exception implements WrappedException {
    public DataRetrievalException(String s) {
        super(s);
    } // DataRetrievalException constructor

    public DataRetrievalException(String s, Exception e) {
        super(s);
        this.causalException = e;
    } // DataRetrievalException constructor

    public Exception getCausalException() {
        return causalException;
    }

    Exception causalException;

} // DataRetrievalException
