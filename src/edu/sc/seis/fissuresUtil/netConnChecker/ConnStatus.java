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
    
    public String toString() {

	return status;
    }
    
    public static final ConnStatus SUCCESSFUL = new ConnStatus("SUCCESSFUL");
    
    public static final ConnStatus UNFINISHED = new ConnStatus("UNFINISHED");

    public static final ConnStatus FAILED = new ConnStatus("FAILED");

    public static final ConnStatus UNKNOWN = new
    ConnStatus("UNKNOWN");

    
}// ConnectionStatus
