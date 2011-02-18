package edu.sc.seis.fissuresUtil.display;


import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.event.EventListenerList;
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
    public void setMouseListener(MouseListener m){
        if(current != null){
            listenerList.remove(MouseListener.class, current);
        }
        current = m;
        listenerList.add(MouseListener.class, m);
    }

    public void addMouseListener(MouseListener m) {
        if(current != null)
            listenerList.remove(MouseListener.class, current);
        current = m;
        listenerList.add(MouseListener.class, m);
    }

    public void addPermMouseListener(MouseListener m){
        listenerList.add(MouseListener.class, m);
    }

    public void removeMouseListener(MouseListener m) {
        current = null;
        listenerList.remove(MouseListener.class, m);
    }

    public void removePermMouseListener(MouseListener m){
        listenerList.remove(MouseListener.class, m);
    }

    public void removeMouseListener(){
        if(current != null){
            listenerList.remove(MouseListener.class, current);
            current = null;
        }
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

    protected MouseListener current;

} // MouseForwarder
