package edu.sc.seis.fissuresUtil.netConnChecker;

/**
 * ConnectionStatus.java
 *
 *
 * Created: Wed Jan 30 10:57:19 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class ConnStatus {
    String status;
    
    private  ConnStatus (String status){
	this.status = status;
    }
    
    /**
     * returns the string representation of the ConnStatus.
     *
     * @return a <code>String</code> value
     */
    public String toString() {

	return status;
    }
    
    /**
     * constant <code>SUCCESSFUL</code>.
     *
     */
    public static final ConnStatus SUCCESSFUL = new ConnStatus("SUCCESSFUL");
    
    /**
     * constant <code>UNFINISHED</code>.
     *
     */
    public static final ConnStatus UNFINISHED = new ConnStatus("UNFINISHED");

    /**
     * constant <code>FAILED</code>.
     *
     */
    public static final ConnStatus FAILED = new ConnStatus("FAILED");

    /**
     * constant <code>UNKNOWN</code>.
     *
     */
    public static final ConnStatus UNKNOWN = new ConnStatus("UNKNOWN");

    /**
     * constant <code>TRYING</code>.
     *
     */
    public static final ConnStatus TRYING = new
    ConnStatus("TRYING");

    
}// ConnectionStatus
