package edu.sc.seis.fissuresUtil.display;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

/**
 * MouseMotionForwarder.java
 *
 *
 * Created: Sat Apr  8 17:25:06 2000
 *
 * @author Philip Crotwell
 * @version
 */

public class MouseMotionForwarder implements MouseMotionListener {
    
    public MouseMotionForwarder() {
	
    }

    public void addMouseMotionListener(MouseMotionListener m) {
	listenerList.add(MouseMotionListener.class, m);
    }

    public void removeMouseMotionListener(MouseMotionListener m) {
	listenerList.remove(MouseMotionListener.class, m);
    }

    public void mouseDragged(MouseEvent e) {
	Object[] listeners = listenerList.getListenerList();
	// Process the listeners last to first, notifying
	// those that are interested in this event
	for (int i = listeners.length-2; i>=0; i-=2) {
	    if (listeners[i]==MouseMotionListener.class) {
		((MouseMotionListener)listeners[i+1]).mouseDragged(e);
	    }              
	}
     }

    public void mouseMoved(MouseEvent e) {
	Object[] listeners = listenerList.getListenerList();
	// Process the listeners last to first, notifying
	// those that are interested in this event
	for (int i = listeners.length-2; i>=0; i-=2) {
	    if (listeners[i]==MouseMotionListener.class) {
		((MouseMotionListener)listeners[i+1]).mouseMoved(e);
	    }              
	}
     }
       
    protected EventListenerList listenerList = new EventListenerList();

} // MouseMotionForwarder
