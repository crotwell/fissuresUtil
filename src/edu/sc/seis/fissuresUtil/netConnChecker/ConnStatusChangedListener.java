package edu.sc.seis.fissuresUtil.netConnChecker;


import java.util.EventListener;

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

    
    /**
     * All the ConnStatusChangedListeners must implement this method.
     *
     * @param e a <code>StatusChangedEvent</code> value
     */
    public void statusChanged(StatusChangedEvent e);
    
    
}// ConnectionStatusChangedListener
