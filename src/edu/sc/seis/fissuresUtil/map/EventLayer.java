package edu.sc.seis.fissuresUtil.map;

/**
 * EventLayer.java
 *
 * @author Created by Charlie Groves
 */


import com.bbn.openmap.Layer;
import com.bbn.openmap.event.ProjectionEvent;
import com.bbn.openmap.omGraphics.OMCircle;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.NoPreferredOrigin;
import edu.iris.Fissures.IfEvent.Origin;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.display.EQDataEvent;
import edu.sc.seis.fissuresUtil.display.EventDataListener;
import edu.sc.seis.fissuresUtil.display.EventTableModel;
import java.awt.Color;
import org.apache.log4j.Logger;

public class EventLayer extends Layer implements EventDataListener{
    public EventLayer(EventTableModel tableModel){
        tableModel.addEventDataListener(this);
    }

    public void paint(java.awt.Graphics g) {
        circles.render(g);
    }

    public void projectionChanged(ProjectionEvent e) {
        LayerProjectionUpdater.update(e, circles, this);
    }

    public void eventDataChanged(EQDataEvent eqDataEvent) {
        EventAccessOperations[] eao = eqDataEvent.getEvents();
        for (int i = 0; i < eao.length; i++){
            try{
                circles.add(new OMEvent(eao[i]));
            }catch(NoPreferredOrigin e){
                logger.debug("No origin for an event");
            }
        }
    }

    public void eventDataCleared() {
        circles.clear();
        repaint();
    }

    private class OMEvent extends OMGraphicList{
        public OMEvent(EventAccessOperations eao) throws NoPreferredOrigin{
            super(2);
            Origin prefOrigin = eao.get_preferred_origin();
            float lat = prefOrigin.my_location.latitude;
            float lon = prefOrigin.my_location.longitude;
            float mag = (prefOrigin.magnitudes[0].value * 10);
            OMCircle bigCircle = new OMCircle(lat, lon, (int)Math.floor(mag), (int)Math.floor(mag));
            OMCircle lilCircle = new OMCircle(lat, lon, 1, 1);
            event = new CacheEvent(eao);
            setLinePaint(Color.BLUE);
            lilCircle.setFillPaint(Color.RED);
            setSelectPaint(Color.RED);
            add(bigCircle);
            add(lilCircle);
            generate(getProjection());
        }

        private CacheEvent event;
    }

    private OMGraphicList circles = new OMGraphicList();

    private Logger logger = Logger.getLogger(EventLayer.class);
}
