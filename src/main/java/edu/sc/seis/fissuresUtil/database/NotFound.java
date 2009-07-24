
package edu.sc.seis.fissuresUtil.database;

/**
 * Indicates that a request to the database did not generate any result.
 *
 *
 * Created: Wed Dec  6 11:07:20 2000
 *
 * @author Philip Crotwell
 * @version
 */

public class NotFound extends Exception {
    
    public NotFound() {
        
    }

    public NotFound(String mesg) {
        super(mesg);
    }

    public NotFound(String mesg, Throwable t) {
        super(mesg, t);
    }
    
}
