package edu.sc.seis.fissuresUtil.netConnChecker;


import java.util.*;

/**
 * ConnectionStatusChangedListener.java
 *
 *
 * Created: Wed Jan 30 11:02:56 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public interface  ConnStatusChangedListener extends EventListener {

    
    public void statusChanged(StatusChangedEvent e);
    
    
}// ConnectionStatusChangedListener
