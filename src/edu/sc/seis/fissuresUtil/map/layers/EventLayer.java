package edu.sc.seis.fissuresUtil.map.layers;
import edu.sc.seis.fissuresUtil.display.*;

import com.bbn.openmap.MapBean;
import com.bbn.openmap.event.ProjectionEvent;
import com.bbn.openmap.event.SelectMouseMode;
import com.bbn.openmap.omGraphics.OMGraphicList;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.NoPreferredOrigin;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.cache.EventBackgroundLoaderPool;
import edu.sc.seis.fissuresUtil.cache.EventLoadedListener;
import edu.sc.seis.fissuresUtil.map.LayerProjectionUpdater;
import edu.sc.seis.fissuresUtil.map.graphics.OMEvent;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.apache.log4j.Logger;

public class EventLayer extends MouseAdapterLayer implements EventDataListener, EventLoadedListener, EQSelectionListener{
    public EventLayer(EventTableModel tableModel, ListSelectionModel lsm, MapBean mapBean){
        this.tableModel = tableModel;
        selectionModel = lsm;
        addEQSelectionListener(this);
        tableModel.addEventDataListener(this);
        eventDataChanged(new EQDataEvent(this, tableModel.getAllEvents()));

        selectionModel.addListSelectionListener(new ListSelectionListener(){
                    public void valueChanged(ListSelectionEvent e) {
                        EventAccessOperations[] selectedEvents = getSelectedEvents();
                        if(selectedEvents.length > 0){
                            fireEQSelectionChanged(new EQSelectionEvent(this, selectedEvents));
                        }
                    }

                });

        this.mapBean = mapBean;
    }

    public void paint(java.awt.Graphics g) {
        synchronized(circles){
            circles.render(g);
        }
    }

    public void projectionChanged(ProjectionEvent e) {
        LayerProjectionUpdater.update(e, circles, this);
    }

    public void eventDataChanged(EQDataEvent eqDataEvent) {
        EventAccessOperations[] events = eqDataEvent.getEvents();
        EventBackgroundLoaderPool loader = tableModel.getLoader();
        for (int i = 0; i < events.length; i++) {
            loader.getEvent(events[i], (CacheEvent)events[i], this);
        }
    }

    public void eventLoaded(CacheEvent event) {
        try{
            OMEvent omEvent = new OMEvent(event, this, mapBean);
            synchronized(circles){
                circles.add(omEvent);
            }
            repaint();
        }catch(NoPreferredOrigin e){
            logger.debug("No origin for an event");
        }
    }

    public void eventDataCleared() {
        synchronized(circles){circles.clear();}
        repaint();
    }

    public void addEQSelectionListener(EQSelectionListener listener){
        listenerList.add(EQSelectionListener.class, listener);
        EventAccessOperations[] selectedEvents = getSelectedEvents();
        if (selectedEvents.length > 0){
            listener.eqSelectionChanged(new EQSelectionEvent(this, getSelectedEvents()));
        }
    }

    public void fireEQSelectionChanged(EQSelectionEvent e){
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==EQSelectionListener.class) {
                // Lazily create the event:
                ((EQSelectionListener)listeners[i+1]).eqSelectionChanged(e);
            }
        }
    }

    //FIXME: make this work for more than one selected event at a time
    public void eqSelectionChanged(EQSelectionEvent eqSelectionEvent) {
        OMEvent selected = null;
        List deselected = new ArrayList();
        synchronized(circles){
            Iterator it = circles.iterator();
            while (it.hasNext()){
                OMEvent current = (OMEvent)it.next();
                try{
                    //if (current.getEvent().get_preferred_origin().equals(eqSelectionEvent.getEvents()[0].get_preferred_origin())){
                    if (DisplayUtils.originIsEqual(current.getEvent(), eqSelectionEvent.getEvents()[0])){
                        selected = current;
                    }else{
                        deselected.add(current);
                    }
                }
                catch(NoPreferredOrigin e){
                    e.printStackTrace();
                }
            }
        }
        if(selected != null){
            selected.select();
            synchronized(circles){
                circles.moveIndexedToTop(circles.indexOf(selected));
            }
        }
        Iterator it = deselected.iterator();
        while(it.hasNext()){
            ((OMEvent)it.next()).deselect();
        }

    }

    private static String[] modeList = { SelectMouseMode.modeID } ;

    public String[] getMouseModeServiceList() {
        return modeList;
    }

    public boolean mouseClicked(MouseEvent e){
        if (currentPopup != null){
            currentPopup.setVisible(false);
            currentPopup = null;
        }

        synchronized(circles){
            Iterator it = circles.iterator();
            List eventsUnderMouse = new ArrayList();
            while(it.hasNext()){
                OMEvent current = (OMEvent)it.next();
                if(current.getBigCircle().contains(e.getX(), e.getY())){
                    eventsUnderMouse.add(current.getEvent());
                }
            }
            if (eventsUnderMouse.size() > 0){
                if (eventsUnderMouse.size() == 1){
                    int rowToSelect = tableModel.getRowForEvent((EventAccessOperations)eventsUnderMouse.get(0));
                    if (rowToSelect != -1){
                        selectionModel.setSelectionInterval(rowToSelect, rowToSelect);
                    }
                }
                else{
                    final JPopupMenu popup = new JPopupMenu();
                    it = eventsUnderMouse.iterator();
                    while (it.hasNext()){
                        final EventAccessOperations current = (EventAccessOperations)it.next();
                        final JMenuItem menuItem = new JMenuItem(CacheEvent.getEventInfo(current));
                        menuItem.addActionListener(new ActionListener(){
                                    public void actionPerformed(ActionEvent e) {
                                        int rowToSelect = tableModel.getRowForEvent(current);
                                        if (rowToSelect != -1){
                                            selectionModel.setSelectionInterval(rowToSelect, rowToSelect);
                                        }
                                        popup.setVisible(false);
                                    }
                                });
                        menuItem.addMouseListener(new MouseAdapter(){

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
                    double[] popupLoc = {compLocation.getX(), compLocation.getY()};
                    popup.setLocation((int)popupLoc[0] + e.getX(), (int)popupLoc[1] + e.getY());
                    popup.setVisible(true);
                    currentPopup = popup;
                }
                return true;
            }
        }

        return false;
    }

    public boolean mouseMoved(MouseEvent e){
        //System.out.println("Something is happening: EventLayer");
        synchronized(circles){
            Iterator it = circles.iterator();
            while(it.hasNext()){
                OMEvent current = (OMEvent)it.next();
                try{
                    if(current.getBigCircle().contains(e.getX(), e.getY())){
                        EventAccessOperations event = current.getEvent();
                        fireRequestInfoLine(CacheEvent.getEventInfo(event));
                        return true;
                    }
                }
                catch(Exception ex){}
            }
        }
        //fireRequestInfoLine(" ");
        return false;
    }

    private EventTableModel getTableModel(){
        return tableModel;
    }

    private EventAccessOperations[] getSelectedEvents(){
        List selectedEvents = new ArrayList();
        EventAccessOperations[] allEvents = getTableModel().getAllEvents();
        for (int i = 0; i < allEvents.length; i++) {
            if (selectionModel.isSelectedIndex(i)){
                selectedEvents.add(allEvents[i]);
            }
        }
        return (EventAccessOperations[])selectedEvents.toArray(new EventAccessOperations[0]);
    }

    private OMGraphicList circles = new OMGraphicList();

    private static Logger logger = Logger.getLogger(EventLayer.class);

    private EventTableModel tableModel;

    private ListSelectionModel selectionModel;

    private MapBean mapBean;

    private JPopupMenu currentPopup;
}





