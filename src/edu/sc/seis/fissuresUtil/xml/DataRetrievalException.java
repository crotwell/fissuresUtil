package edu.sc.seis.fissuresUtil.xml;

import edu.sc.seis.fissuresUtil.exceptionHandlerGUI.WrappedException;

/**
 * Exception to be thrown if there is an error in retrieving data.
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
        super(s, e);
    } // DataRetrievalException constructor

    public Throwable getCausalException() {
        return getCause();
    }

} // DataRetrievalException
