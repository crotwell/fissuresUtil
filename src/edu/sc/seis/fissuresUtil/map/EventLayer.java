package edu.sc.seis.fissuresUtil.map;

/**
 * EventLayer.java
 *
 * @author Created by Charlie Groves
 */


import com.bbn.openmap.Layer;
import com.bbn.openmap.event.ProjectionEvent;
import com.bbn.openmap.omGraphics.OMCircle;
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
import java.util.Iterator;
import org.apache.log4j.Logger;

public class EventLayer extends Layer implements EventDataListener, EventLoadedListener, EQSelectionListener{
    public EventLayer(EventTableModel tableModel){
		this.tableModel = tableModel;
		tableModel.addEventDataListener(this);
		eventDataChanged(new EQDataEvent(this, tableModel.getAllEvents()));
		tableModel.addEQSelectionListener(this);
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
			logger.debug("event loaded");
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

	/**
	 * Method eqSelectionChanged
	 *
	 * @param    eqSelectionEvent    an EQSelectionEvent
	 *
	 */
	public void eqSelectionChanged(EQSelectionEvent eqSelectionEvent) {
		Iterator it = circles.iterator();
		while (it.hasNext()){
			OMEvent current = (OMEvent)it.next();
			try{
				if (current.getEvent().get_preferred_origin().equals(eqSelectionEvent.getEvent().get_preferred_origin())){
					current.select();
					return;
				}
			}
			catch(NoPreferredOrigin e){}
		}
	}

    private class OMEvent extends OMGraphicList{
		public OMEvent(EventAccessOperations eao) throws NoPreferredOrigin{
			super(2);
			Origin prefOrigin = eao.get_preferred_origin();
			float lat = prefOrigin.my_location.latitude;
			float lon = prefOrigin.my_location.longitude;
			float mag = (prefOrigin.magnitudes[0].value * 10);
			OMCircle bigCircle = new OMCircle(lat, lon, (int)Math.floor(mag), (int)Math.floor(mag));
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
			System.out.println("Event selected...should render on map.");
			super.select();
		}

		private CacheEvent event;
    }

    private OMGraphicList circles = new OMGraphicList();

    private static Logger logger = Logger.getLogger(EventLayer.class);

    private EventTableModel tableModel;
}
