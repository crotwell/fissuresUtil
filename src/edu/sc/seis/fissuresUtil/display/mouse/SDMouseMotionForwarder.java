package edu.sc.seis.fissuresUtil.display.mouse;

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import javax.swing.event.EventListenerList;

public class SDMouseMotionForwarder implements MouseMotionListener {
    public void add(SDMouseMotionListener m) {
        listenerList.add(SDMouseMotionListener.class, m);
    }

    public void remove(SDMouseMotionListener m) {
        listenerList.remove(SDMouseMotionListener.class, m);
    }

    public void mouseDragged(MouseEvent e) {
        SDMouseEvent sdE = SDMouseEvent.wrap(e);
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length-1; i>=0; i-=1) {
            if (listeners[i]==SDMouseMotionListener.class) {
                ((SDMouseMotionListener)listeners[i+1]).mouseDragged(sdE);
            }
        }
    }

    public void mouseMoved(MouseEvent e) {
        SDMouseEvent sdE = SDMouseEvent.wrap(e);
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length-1; i>=0; i-=1) {
            if (listeners[i]==SDMouseMotionListener.class) {
                ((SDMouseMotionListener)listeners[i+1]).mouseMoved(sdE);
            }
        }
    }

    private EventListenerList listenerList = new EventListenerList();
} // MouseMotionForwarder
