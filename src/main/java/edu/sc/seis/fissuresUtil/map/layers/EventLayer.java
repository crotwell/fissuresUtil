package edu.sc.seis.fissuresUtil.map.layers;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.apache.log4j.Logger;

import com.bbn.openmap.event.ProjectionEvent;
import com.bbn.openmap.event.SelectMouseMode;
import com.bbn.openmap.omGraphics.OMGraphicList;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.cache.EventLoadedListener;
import edu.sc.seis.fissuresUtil.cache.EventLoader;
import edu.sc.seis.fissuresUtil.cache.EventUtil;
import edu.sc.seis.fissuresUtil.cache.ProxyEventAccessOperations;
import edu.sc.seis.fissuresUtil.cache.WorkerThreadPool;
import edu.sc.seis.fissuresUtil.display.EQDataEvent;
import edu.sc.seis.fissuresUtil.display.EQSelectionEvent;
import edu.sc.seis.fissuresUtil.display.EQSelectionListener;
import edu.sc.seis.fissuresUtil.display.EventDataListener;
import edu.sc.seis.fissuresUtil.map.LayerProjectionUpdater;
import edu.sc.seis.fissuresUtil.map.OpenMap;
import edu.sc.seis.fissuresUtil.map.colorizer.event.EventColorizer;
import edu.sc.seis.fissuresUtil.map.graphics.OMEvent;

public class EventLayer extends MouseAdapterLayer implements EventDataListener,
        EventLoadedListener, EQSelectionListener {

    public EventLayer(OpenMap map, EventColorizer colorizer) {
        this.map = map;
        this.colorizer = colorizer;
        setName("Event Layer");
        circles.setTraverseMode(OMGraphicList.LAST_ADDED_ON_TOP);
    }

    public void paint(java.awt.Graphics g) {
        synchronized(circles) {
            circles.render(g);
        }
    }

    public void projectionChanged(ProjectionEvent e) {
        LayerProjectionUpdater.update(e, circles, this);
    }

    public void eventDataAppended(EQDataEvent eqDataEvent) {
        loadEvents(eqDataEvent);
    }

    public void eventDataChanged(EQDataEvent eqDataEvent) {
        loadEvents(eqDataEvent);
    }

    protected void loadEvents(EQDataEvent eqDataEvent) {
        Iterator it = eqDataEvent.getEvents().iterator();
        while(it.hasNext()) {
            EventLoader loader = new EventLoader((CacheEvent)it.next(), this);
            WorkerThreadPool.getDefaultPool().invokeLater(loader);
        }
    }

    public void eventLoaded(ProxyEventAccessOperations event) {
        synchronized(circles) {
            if(events.add(event)) {
                circles.add(new OMEvent(event, this, map));
                colorizer.colorize(circles);
                repaint();
            }
        }
    }

    public void eventDataCleared() {
        synchronized(circles) {
            circles.clear();
            events.clear();
        }
        repaint();
    }

    public void addEQSelectionListener(EQSelectionListener listener) {
        listenerList.add(EQSelectionListener.class, listener);
        EventAccessOperations[] selectedEvents = getSelectedEvents();
        if(selectedEvents.length > 0) {
            listener.eqSelectionChanged(new EQSelectionEvent(this,
                                                             getSelectedEvents()));
        }
    }

    public void removeEQSelectionListener(EQSelectionListener listener) {
        listenerList.remove(EQSelectionListener.class, listener);
    }

    public void fireEQSelectionChanged(EQSelectionEvent e) {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for(int i = listeners.length - 2; i >= 0; i -= 2) {
            if(listeners[i] == EQSelectionListener.class) {
                // Lazily create the event:
                ((EQSelectionListener)listeners[i + 1]).eqSelectionChanged(e);
            }
        }
    }

    // FIXME: make this work for more than one selected event at a time
    public void eqSelectionChanged(EQSelectionEvent eqSelectionEvent) {
        OMEvent selected = null;
        List deselected = new ArrayList();
        synchronized(circles) {
            Iterator it = circles.iterator();
            while(it.hasNext()) {
                OMEvent current = (OMEvent)it.next();
                if(current.getEvent().equals(eqSelectionEvent.getEvents()[0])) {
                    selected = current;
                } else {
                    deselected.add(current);
                }
            }
        }
        if(selected != null) {
            selected.select();
            synchronized(circles) {
                circles.moveIndexedToTop(circles.indexOf(selected));
            }
            Iterator it = deselected.iterator();
            while(it.hasNext()) {
                ((OMEvent)it.next()).deselect();
            }
        }
    }

    private static String[] modeList = {SelectMouseMode.modeID};

    public String[] getMouseModeServiceList() {
        return modeList;
    }

    public boolean mouseClicked(MouseEvent e) {
        maybeKillCurrentPopup();
        synchronized(circles) {
            Iterator it = circles.iterator();
            List eventsUnderMouse = new ArrayList();
            while(it.hasNext()) {
                OMEvent current = (OMEvent)it.next();
                if(current.getBigCircle().contains(e.getX(), e.getY())) {
                    eventsUnderMouse.add(current.getEvent());
                }
            }
            if(eventsUnderMouse.size() > 0) {
                if(eventsUnderMouse.size() == 1) {
                    selectEvent((EventAccessOperations)eventsUnderMouse.get(0));
                } else {
                    final JPopupMenu popup = new JPopupMenu();
                    popup.setInvoker(this);
                    it = eventsUnderMouse.iterator();
                    while(it.hasNext()) {
                        final EventAccessOperations current = (EventAccessOperations)it.next();
                        final JMenuItem menuItem = new JMenuItem(EventUtil.getEventInfo(current));
                        menuItem.addActionListener(new ActionListener() {

                            public void actionPerformed(ActionEvent e) {
                                selectEvent(current);
                                popup.setVisible(false);
                            }
                        });
                        menuItem.addMouseListener(new MouseAdapter() {

                            public void mouseEntered(MouseEvent e) {
                                menuItem.setArmed(true);
                            }

                            public void mouseExited(MouseEvent e) {
                                menuItem.setArmed(false);
                            }
                        });
                        popup.add(menuItem);
                    }
                    Point compLocation = e.getComponent().getLocationOnScreen();
                    double[] popupLoc = {compLocation.getX(),
                                         compLocation.getY()};
                    popup.setLocation((int)popupLoc[0] + e.getX(),
                                      (int)popupLoc[1] + e.getY());
                    popup.setVisible(true);
                    currentPopup = popup;
                }
                return true;
            }
        }
        return false;
    }

    // extend this method if you want this selection to hook up
    // with an external event browser (ie. an EventTableModel)
    public void selectEvent(EventAccessOperations evo) {
    // noImpl
    }

    public boolean mouseMoved(MouseEvent e) {
        maybeKillCurrentPopup();
        synchronized(circles) {
            Iterator it = circles.iterator();
            while(it.hasNext()) {
                OMEvent current = (OMEvent)it.next();
                try {
                    if(current.getBigCircle().contains(e.getX(), e.getY())) {
                        EventAccessOperations event = current.getEvent();
                        fireRequestInfoLine(EventUtil.getEventInfo(event));
                        return true;
                    }
                } catch(Exception ex) {}
            }
        }
        return false;
    }

    // implement this method to get the selected events from
    // your source (ie. the EventTableModel)
    public EventAccessOperations[] getSelectedEvents() {
        return new EventAccessOperations[0];
    }

    public void printCircleLocs() {
        Iterator it = circles.iterator();
        int i = 0;
        while(it.hasNext()) {
            OMEvent cur = (OMEvent)it.next();
            int x = cur.getBigCircle().getX();
            int y = cur.getBigCircle().getY();
            int rad = (int)cur.getBigCircle().getHeight() / 2;
            System.out.println("<area href=\"" + i++
                    + "\" shape=\"circle\" coords=\"" + x + "," + y + "," + rad
                    + "\"/>");
        }
    }

    private OMGraphicList circles = new OMGraphicList();

    private Set events = new HashSet();

    private static Logger logger = Logger.getLogger(EventLayer.class);

    private OpenMap map;

    private EventColorizer colorizer;

    public void removeEQSelectionListener(StationLayer stl) {
    // TODO - replace autogenerated method body
    }
}
