package edu.sc.seis.fissuresUtil.display.mouse;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.event.EventListenerList;

public class SDMouseForwarder implements MouseListener {
    public void add(SDMouseListener m) {
        listenerList.add(SDMouseListener.class, m);
    }

    public void remove(SDMouseListener m) {
        listenerList.remove(SDMouseListener.class, m);
    }

    public void mouseClicked(MouseEvent e) {
        SDMouseEvent sdE = SDMouseEvent.wrap(e);
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==SDMouseListener.class) {
                ((SDMouseListener)listeners[i+1]).mouseClicked(sdE);
            }
        }
    }

    public void mousePressed(MouseEvent e) {
        SDMouseEvent sdE = SDMouseEvent.wrap(e);
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==SDMouseListener.class) {
                ((SDMouseListener)listeners[i+1]).mousePressed(sdE);
            }
        }
    }

    public void mouseReleased(MouseEvent e) {
        SDMouseEvent sdE = SDMouseEvent.wrap(e);
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==SDMouseListener.class) {
                ((SDMouseListener)listeners[i+1]).mouseReleased(sdE);
            }
        }
    }

    public void mouseEntered(MouseEvent e) {
        SDMouseEvent sdE = SDMouseEvent.wrap(e);
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==SDMouseListener.class) {
                ((SDMouseListener)listeners[i+1]).mouseEntered(sdE);
            }
        }
    }

    public void mouseExited(MouseEvent e) {
        SDMouseEvent sdE = SDMouseEvent.wrap(e);
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==SDMouseListener.class) {
                ((SDMouseListener)listeners[i+1]).mouseExited(sdE);
            }
        }
    }

    private EventListenerList listenerList = new EventListenerList();
} // MouseForwarder
