package edu.sc.seis.fissuresUtil.display;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

/**
 * MouseForwarder.java
 *
 *
 * Created: Sat Apr  8 17:20:49 2000
 *
 * @author Philip Crotwell
 * @version
 */

public class MouseForwarder implements MouseListener {
    
    public MouseForwarder() {
	
    }

    public void addMouseListener(MouseListener m) {
	listenerList.add(MouseListener.class, m);
    }

    public void removeMouseListener(MouseListener m) {
	listenerList.remove(MouseListener.class, m);
    }

    public void mouseClicked(MouseEvent e) {
	Object[] listeners = listenerList.getListenerList();
	// Process the listeners last to first, notifying
	// those that are interested in this event
	for (int i = listeners.length-2; i>=0; i-=2) {
	    if (listeners[i]==MouseListener.class) {
		((MouseListener)listeners[i+1]).mouseClicked(e);
	    }              
	}
    }

    public void mousePressed(MouseEvent e) {
	Object[] listeners = listenerList.getListenerList();
	// Process the listeners last to first, notifying
	// those that are interested in this event
	for (int i = listeners.length-2; i>=0; i-=2) {
	    if (listeners[i]==MouseListener.class) {
		((MouseListener)listeners[i+1]).mousePressed(e);
	    }              
	}
     }
    
    public void mouseReleased(MouseEvent e) {
	Object[] listeners = listenerList.getListenerList();
	// Process the listeners last to first, notifying
	// those that are interested in this event
	for (int i = listeners.length-2; i>=0; i-=2) {
	    if (listeners[i]==MouseListener.class) {
		((MouseListener)listeners[i+1]).mouseReleased(e);
	    }              
	}
     }
    
    public void mouseEntered(MouseEvent e) {
	Object[] listeners = listenerList.getListenerList();
	// Process the listeners last to first, notifying
	// those that are interested in this event
	for (int i = listeners.length-2; i>=0; i-=2) {
	    if (listeners[i]==MouseListener.class) {
		((MouseListener)listeners[i+1]).mouseEntered(e);
	    }              
	}
     }
    
    public void mouseExited(MouseEvent e) {
	Object[] listeners = listenerList.getListenerList();
	// Process the listeners last to first, notifying
	// those that are interested in this event
	for (int i = listeners.length-2; i>=0; i-=2) {
	    if (listeners[i]==MouseListener.class) {
		((MouseListener)listeners[i+1]).mouseExited(e);
	    }              
	}
     }
    
    protected EventListenerList listenerList = new EventListenerList();

} // MouseForwarder
