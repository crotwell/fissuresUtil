package edu.sc.seis.fissuresUtil.bag;

/**
 * IncompatibleSeismograms.java
 *
 *
 * Created: Sat Oct 19 11:38:49 2002
 *
 * @author <a href="mailto:www@seis.sc.edu">Philip Crotwell</a>
 * @version
 */

public class IncompatibleSeismograms extends Exception {
    public IncompatibleSeismograms (String reason){
	super(reason);
    }
    
}// IncompatibleSeismograms
