package edu.sc.seis.fissuresUtil.map.tools;

import java.awt.event.MouseEvent;
import javax.swing.event.EventListenerList;
import com.bbn.openmap.LatLonPoint;
import com.bbn.openmap.Layer;
import com.bbn.openmap.MapBean;
import com.bbn.openmap.event.CenterEvent;
import com.bbn.openmap.event.ZoomEvent;
import com.bbn.openmap.event.ZoomListener;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.Origin;
import edu.sc.seis.fissuresUtil.cache.EventUtil;
import edu.sc.seis.fissuresUtil.map.OpenMap;
import edu.sc.seis.fissuresUtil.map.layers.ChannelChooserLayer;

public class ZoomTool extends OpenMapTool {

	private float zoomFactor;

	private OpenMap map;

	private float mapScale;

	private MapBean mapBean;

	private EventListenerList listenerList = new EventListenerList();

	private String id = "Zoom";

	public ZoomTool(OpenMap om, float factor, String id) {
		this(om, factor, OpenMap.DEFAULT_SCALE, id);
	}

	public ZoomTool(OpenMap om, float factor, float scale, String id) {
		zoomFactor = factor;
		map = om;
		mapScale = scale;
		mapBean = map.getMapBean();
		if (id != null) {
			this.id = id;
		}
	}

	public void addZoomListener(ZoomListener zl) {
		listenerList.add(ZoomListener.class, zl);
	}

	public void removeZoomListener(ZoomListener zl) {
		listenerList.remove(ZoomListener.class, zl);
	}

	private void fireZoomChanged(ZoomEvent ze) {
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == ZoomListener.class) {
				// Lazily create the event:
				if (ze != null) {
					((ZoomListener) listeners[i + 1]).zoom(ze);
				}
			}
		}
	}

	public void mouseClicked(MouseEvent e) {
		if (isActive()) {
			LatLonPoint llp = mapBean.getCoordinates(e);
			mapBean.center(new CenterEvent(this, llp.getLatitude(), llp
					.getLongitude()));
			zoom();
		}
	}

	public void zoom() {
		fireZoomChanged(new ZoomEvent(this, ZoomEvent.ABSOLUTE, mapBean
				.getScale()
				/ zoomFactor));
	}

	public void reset() {
		LatLonPoint center = mapBean.getCenter(); //center on mapBean's current
		// center point
		//however, if there is an event (or events) selected, center on the
		// first selected event
		if (map.getEventLayer() != null
				&& map.getEventLayer().getSelectedEvents().length > 0) {
			EventAccessOperations event = map.getEventLayer()
					.getSelectedEvents()[0];
			Origin orig = EventUtil.extractOrigin(event);
			center.setLatitude(0f);
			center.setLongitude((float)orig.my_location.longitude);
		}
		mapBean.setCenter(center.getLatitude(), center.getLongitude());
		fireZoomChanged(new ZoomEvent(this, ZoomEvent.ABSOLUTE, mapScale));
		Layer[] layers = map.getLayers();
		for (int i = 0; i < layers.length; i++) {
			if (layers[i] instanceof ChannelChooserLayer) {
				((ChannelChooserLayer) layers[i]).getChannelChooser()
						.recheckNetworkAvailability();
				break;
			}
		}
	}

	public void setActive(boolean active) {
		super.setActive(active);
	}

	/**
	 * Returns the id (MapMouseMode name). This name should be unique for each
	 * MapMouseMode.
	 * 
	 * @return String ID
	 */
	public String getID() {
		return id;
	}

	public String getPrettyName() {
		return id;
	}

}