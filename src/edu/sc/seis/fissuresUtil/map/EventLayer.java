package edu.sc.seis.fissuresUtil.map;

/**
 * EventLayer.java
 *
 * @author Created by Charlie Groves
 */


import edu.sc.seis.fissuresUtil.display.*;

import com.bbn.openmap.MapBean;
import com.bbn.openmap.event.CenterEvent;
import com.bbn.openmap.event.InfoDisplayEvent;
import com.bbn.openmap.event.ProjectionEvent;
import com.bbn.openmap.event.SelectMouseMode;
import com.bbn.openmap.omGraphics.OMCircle;
import com.bbn.openmap.omGraphics.OMGraphicList;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.NoPreferredOrigin;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.cache.EventBackgroundLoaderPool;
import edu.sc.seis.fissuresUtil.cache.EventLoadedListener;
import java.awt.Color;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.apache.log4j.Logger;

public class EventLayer extends MouseAdapterLayer implements EventDataListener, EventLoadedListener, EQSelectionListener{
    public EventLayer(EventTableModel tableModel, ListSelectionModel lsm, MapBean mapBean){
		this.tableModel = tableModel;
		selectionModel = lsm;
		tableModel.addEventDataListener(this);
		eventDataChanged(new EQDataEvent(this, tableModel.getAllEvents()));

		selectionModel.addListSelectionListener(new ListSelectionListener(){
					public void valueChanged(ListSelectionEvent e) {
						List selectedEvents = new ArrayList();
						EventAccessOperations[] allEvents = getTableModel().getAllEvents();
						for (int i = 0; i < allEvents.length; i++) {
							if (selectionModel.isSelectedIndex(i)){
								selectedEvents.add(allEvents[i]);
							}
						}
						eqSelectionChanged(new EQSelectionEvent(this, new EventAccessOperations[]{(EventAccessOperations)selectedEvents.get(0)}));
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
					if (current.getEvent().get_preferred_origin().equals(eqSelectionEvent.getEvents()[0].get_preferred_origin())){
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
		System.out.println("mouseClicked");
		synchronized(circles){
			Iterator it = circles.iterator();
			while(it.hasNext()){
				OMEvent current = (OMEvent)it.next();
				if(current.getBigCircle().contains(e.getX(), e.getY())){
					System.out.println("Selection of event " + current.getEvent());
					int rowToSelect = tableModel.getRowForEvent(current.getEvent());
					if (rowToSelect != -1){
						selectionModel.setSelectionInterval(rowToSelect, rowToSelect);
					}
					return true;
				}
			}
		}
		return false;
    }

	public boolean mouseMoved(MouseEvent e){
		synchronized(circles){
			Iterator it = circles.iterator();
			while(it.hasNext()){
				OMEvent current = (OMEvent)it.next();
				try{
					if(current.getBigCircle().contains(e.getX(), e.getY())){
						EventAccessOperations event = current.getEvent();
						StringBuffer buf = new StringBuffer();
						buf.append(event.get_preferred_origin().origin_time.date_time);
						fireRequestToolTip(e, buf.toString());
						return true;
					}
				}
				catch(Exception ex){}
			}
		}
		fireHideToolTip(e);
		return false;
	}

    private class OMEvent extends OMGraphicList{
		public OMEvent(EventAccessOperations eao) throws NoPreferredOrigin{
			super(2);
			Origin prefOrigin = eao.get_preferred_origin();
			float lat = prefOrigin.my_location.latitude;
			float lon = prefOrigin.my_location.longitude;
			float mag = prefOrigin.magnitudes[0].value;

			double scale = 1.8;
			int lilDiameter = (int)Math.pow(scale, 3.0);
			OMCircle lilCircle = new OMCircle(lat, lon, lilDiameter, lilDiameter);
			if (mag <= 3.0){
				bigCircle = new OMCircle(lat, lon, lilDiameter, lilDiameter);
			}
			else{
				mag = (float)(Math.pow(scale, (double)mag));
				bigCircle = new OMCircle(lat, lon, (int)Math.floor(mag), (int)Math.floor(mag));
			}
			event = new CacheEvent(eao);

			Color color = getDepthColor((QuantityImpl)prefOrigin.my_location.depth);
			lilCircle.setLinePaint(Color.BLACK);
			lilCircle.setFillPaint(color);
			bigCircle.setStroke(DisplayUtils.THREE_PIXEL_STROKE);
			bigCircle.setLinePaint(color);
			add(bigCircle);
			add(lilCircle);
			generate(getProjection());
		}

		public CacheEvent getEvent(){
			return event;
		}

		public void select() {
			bigCircle.setFillPaint(new Color(0, 0, 0, 64));
			try{
				mapBean.center(new CenterEvent(this,
											   event.get_preferred_origin().my_location.latitude,
											   event.get_preferred_origin().my_location.longitude));
			}catch(NoPreferredOrigin e){}
		}

		public void deselect(){
			bigCircle.setFillPaint(OMGraphicList.clear);
		}

		public OMCircle getBigCircle(){ return bigCircle; }

		private Color getDepthColor(QuantityImpl depth){
			double depthKM = depth.convertTo(UnitImpl.KILOMETER).value;
			Color color = MEDIUM_DEPTH_EVENT;
			if (depthKM <= 40.0){
				color = SHALLOW_DEPTH_EVENT;
			}
			if (depthKM >= 150.0){
				color = DEEP_DEPTH_EVENT;
			}
			return color;
		}

		private CacheEvent event;

		private OMCircle bigCircle;
    }

	private EventTableModel getTableModel(){
		return tableModel;
	}

    private OMGraphicList circles = new OMGraphicList();

    private static Logger logger = Logger.getLogger(EventLayer.class);

    private EventTableModel tableModel;

    private ListSelectionModel selectionModel;

    private MapBean mapBean;

	public static final Color SHALLOW_DEPTH_EVENT = new Color(243, 33, 78);

	public static final Color MEDIUM_DEPTH_EVENT = new Color(246, 185, 42);

	public static final Color DEEP_DEPTH_EVENT = new Color(245, 249, 27);

}




