package edu.sc.seis.fissuresUtil.map;

/**
 * EventLayer.java
 *
 * @author Created by Charlie Groves
 */


import com.bbn.openmap.MapBean;
import com.bbn.openmap.event.CenterEvent;
import com.bbn.openmap.event.ProjectionEvent;
import com.bbn.openmap.event.SelectMouseMode;
import com.bbn.openmap.omGraphics.OMCircle;
import com.bbn.openmap.omGraphics.OMGraphicConstants;
import com.bbn.openmap.omGraphics.OMGraphicList;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.NoPreferredOrigin;
import edu.iris.Fissures.IfEvent.Origin;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.cache.EventBackgroundLoaderPool;
import edu.sc.seis.fissuresUtil.cache.EventLoadedListener;
import edu.sc.seis.fissuresUtil.display.EQDataEvent;
import edu.sc.seis.fissuresUtil.display.EQSelectionEvent;
import edu.sc.seis.fissuresUtil.display.EQSelectionListener;
import edu.sc.seis.fissuresUtil.display.EventDataListener;
import edu.sc.seis.fissuresUtil.display.EventTableModel;
import java.awt.Color;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;
import java.util.List;
import org.apache.log4j.Logger;
import javax.swing.event.ListSelectionEvent;
import com.bbn.openmap.Layer;

public class EventLayer extends Layer implements EventDataListener, EventLoadedListener, EQSelectionListener{
    public EventLayer(EventTableModel tableModel, ListSelectionModel lsm, MapBean mapBean){
		this.tableModel = tableModel;
		selectionModel = lsm;
		tableModel.addEventDataListener(this);
		eventDataChanged(new EQDataEvent(this, tableModel.getAllEvents()));

		//this list selection stuff does not work yet.
		selectionModel.addListSelectionListener(new ListSelectionListener(){
					public void valueChanged(ListSelectionEvent e) {
						EventAccessOperations[] events = new EventAccessOperations[0];
						//EventAccessOperations[] events = tableModel.getSelectedEvents();
						for (int i = 0; i < events.length; i++) {
							Iterator it = circles.iterator();
							while (it.hasNext()){
								OMEvent current = (OMEvent)it.next();
								try{

									if (current.getEvent().get_preferred_origin().equals(events[i].get_preferred_origin())){
										current.select();
										return;
									}
								}
								catch(NoPreferredOrigin ex){}
							}
						}
					}

				});

		//temporary fix for selection for now...will be removed as soon
		//as the stuff above starts working
		tableModel.addEQSelectionListener(this);

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
            OMEvent omEvent = new OMEvent(event);
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

    public void eqSelectionChanged(EQSelectionEvent eqSelectionEvent) {
        OMEvent selected = null;
        List deselected = new ArrayList();
        synchronized(circles){
            Iterator it = circles.iterator();
            while (it.hasNext()){
                OMEvent current = (OMEvent)it.next();
                try{
                    if (current.getEvent().get_preferred_origin().equals(eqSelectionEvent.getEvent().get_preferred_origin())){
                        selected = current;
                    }else{
                        deselected.add(current);
                    }
                }
                catch(NoPreferredOrigin e){}
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
        synchronized(circles){
            Iterator it = circles.iterator();
            while(it.hasNext()){
                OMEvent current = (OMEvent)it.next();
                if(current.getBigCircle().contains(e.getX(), e.getY())){
                    circles.deselect();
                    current.select();
                    return true;
                }
            }
        }
        return false;
    }

    private class OMEvent extends OMGraphicList{
        public OMEvent(EventAccessOperations eao) throws NoPreferredOrigin{
            super(2);
            Origin prefOrigin = eao.get_preferred_origin();
            float lat = prefOrigin.my_location.latitude;
            float lon = prefOrigin.my_location.longitude;
            float mag = prefOrigin.magnitudes[0].value;
            mag *= mag;
            bigCircle = new OMCircle(lat, lon, (int)Math.floor(mag), (int)Math.floor(mag));
            OMCircle lilCircle = new OMCircle(lat, lon, 5, 5);
            event = new CacheEvent(eao);
            setLinePaint(Color.BLUE);
            lilCircle.setFillPaint(Color.RED);
            setSelectPaint(Color.MAGENTA);
            add(bigCircle);
            add(lilCircle);
            generate(getProjection());
        }

        public CacheEvent getEvent(){
            return event;
        }

        public void select() {
            bigCircle.setFillPaint(Color.RED);
            try{
                mapBean.center(new CenterEvent(this,
                                               event.get_preferred_origin().my_location.latitude,
                                               event.get_preferred_origin().my_location.longitude));
            }catch(NoPreferredOrigin e){}
            super.select();
        }

        public void deselect(){
            bigCircle.setFillPaint(OMGraphicConstants.clear);
            super.deselect();
        }

        public OMCircle getBigCircle(){ return bigCircle; }

        private CacheEvent event;

        private OMCircle bigCircle;
    }

    private OMGraphicList circles = new OMGraphicList();

    private static Logger logger = Logger.getLogger(EventLayer.class);

    private EventTableModel tableModel;

	private ListSelectionModel selectionModel;

    private MapBean mapBean;
}


