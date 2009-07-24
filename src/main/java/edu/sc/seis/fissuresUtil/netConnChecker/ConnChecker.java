package edu.sc.seis.fissuresUtil.netConnChecker;

//import edu.sc.seis.sac.*;


/**
 * Interface for the Connection Checker:
 * HTTPChecker and CorbaChecker
 *
 * ConnChecker.java
 *
 *
 * @Created Sept 11, 2001
 *
 * @author Georgina Coleman
 * @version 0
 *
 */

public interface ConnChecker  extends Runnable {
    public ConnStatusResult getStatus();
    public String getDescription();
    public void addConnStatusChangedListener(ConnStatusChangedListener listener);
    public void removeConnStatusChangedListener(ConnStatusChangedListener listener);
    public void fireStatusChanged(String urlStr, ConnStatus connectionStatus);
}// ConnChecker interface
