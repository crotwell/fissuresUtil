/**
 * EventTableLayer.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.map.layers;

import com.bbn.openmap.MapBean;
import com.bbn.openmap.event.SelectMouseMode;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.sc.seis.fissuresUtil.cache.EventBackgroundLoaderPool;
import edu.sc.seis.fissuresUtil.display.EQDataEvent;
import edu.sc.seis.fissuresUtil.display.EQSelectionEvent;
import edu.sc.seis.fissuresUtil.display.EventTableModel;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class EventTableLayer extends EventLayer{

    private EventTableModel tableModel;
    private ListSelectionModel selectionModel;

    public EventTableLayer(EventTableModel tableModel, ListSelectionModel lsm, MapBean mapBean){
        super(mapBean);
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
    }

    public void selectEvent(EventAccessOperations evo){
        int rowToSelect = tableModel.getRowForEvent(evo);
        if (rowToSelect != -1){
            selectionModel.setSelectionInterval(rowToSelect, rowToSelect);
        }
    }

    public EventTableModel getTableModel(){
        return tableModel;
    }

   public EventBackgroundLoaderPool getLoader(){
        return tableModel.getLoader();
    }

    public EventAccessOperations[] getSelectedEvents(){
        List selectedEvents = new ArrayList();
        EventAccessOperations[] allEvents = tableModel.getAllEvents();
        for (int i = 0; i < allEvents.length; i++) {
            if (selectionModel.isSelectedIndex(i)){
                selectedEvents.add(allEvents[i]);
            }
        }
        return (EventAccessOperations[])selectedEvents.toArray(new EventAccessOperations[0]);
    }
}

