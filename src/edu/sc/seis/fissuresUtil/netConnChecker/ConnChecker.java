package edu.sc.seis.fissuresUtil.netConnChecker;

//import edu.sc.seis.sac.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.text.*;
import javax.swing.text.html.*;
import org.apache.log4j.*;


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
    public boolean isFinished();
    public boolean isTrying();
    public boolean isUnknown();
    public boolean isSuccessful();
    public ConnStatus getStatus();
    public String getDescription();
    public void addConnStatusChangedListener(ConnStatusChangedListener listener);
    public void removeConnStatusChangedListener(ConnStatusChangedListener listener);
    public void fireStatusChanged(String urlStr, ConnStatus connectionStatus);
}// ConnChecker interface

/************************************************************/

