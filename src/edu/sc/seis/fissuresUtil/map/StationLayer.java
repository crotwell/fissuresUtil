package edu.sc.seis.fissuresUtil.map;

/**
 * EventLayer.java
 *
 * @author Created by Charlie Groves
 */

import com.bbn.openmap.LatLonPoint;
import com.bbn.openmap.Layer;
import com.bbn.openmap.MapBean;
import com.bbn.openmap.event.CenterEvent;
import com.bbn.openmap.event.ProjectionEvent;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMPoly;
import edu.iris.Fissures.IfNetwork.Station;
import edu.sc.seis.fissuresUtil.chooser.ChannelChooser;
import edu.sc.seis.fissuresUtil.chooser.StationDataEvent;
import edu.sc.seis.fissuresUtil.chooser.StationDataListener;
import edu.sc.seis.fissuresUtil.chooser.StationSelectionEvent;
import edu.sc.seis.fissuresUtil.chooser.StationSelectionListener;
import java.awt.Color;
import java.util.Iterator;

public class StationLayer extends Layer implements StationDataListener,
    StationSelectionListener{
    /**
     * Adds this layer as a listener on station data arriving and station
     * selection occuring on the channel chooser being passed in.
     * If station data is passed, a blue triangle is drawn on the map.
     * If a station is selected, the triangle turns red.
     */
    public StationLayer(ChannelChooser c, MapBean mapBean) {
        omgraphics = new OMGraphicList();
        //add the necessary data listeners to the channel chooser, provided it exists
        c.addStationDataListener(this);
        c.addStationSelectionListener(this);
        this.mapBean = mapBean;
    }

    public void paint(java.awt.Graphics g) {
        omgraphics.render(g);
    }

    public void projectionChanged(ProjectionEvent e) {
        omgraphics.project(e.getProjection(), true);
        repaint();
    }

    /*This adds each of these stations to the layer
     *
     */
    public void stationDataChanged(StationDataEvent s) {
        Station[] stations = s.getStations();
        for (int i = 0; i < stations.length; i++){
            omgraphics.add(new OMStation(stations[i]));
        }
        repaint();
        LatLonPoint center = mapBean.getCenter();
        mapBean.center(new CenterEvent(this, center.getLatitude(),
                                       center.getLongitude()));

    }

    public void stationDataCleared() {
        omgraphics.clear();
        repaint();
    }

    /*takes all of the selected stations in this list, and if they're in the
     *StationLayer changes their line color to RED.
     */
    public void stationSelectionChanged(StationSelectionEvent s) {
        Station[] stations = s.getSelectedStations();
        Iterator it = omgraphics.iterator();
        while(it.hasNext()){
            OMStation current = (OMStation)it.next();
            boolean selected = false;
            for (int i = 0; i < stations.length && !selected; i++){
                if(current.getStation().equals(stations[i])){
                    current.select();
                    selected = true;
                }
            }
            if(!selected){
                current.deselect();
            }
        }
        repaint();
    }

    private class OMStation extends OMPoly{
        public OMStation(Station stat){
            super(stat.my_location.latitude,
                  stat.my_location.longitude, xPoints, yPoints,
                  OMPoly.COORDMODE_ORIGIN);
            station = stat;
            setFillPaint(Color.BLUE);
            setSelectPaint(Color.RED);
        }

        public Station getStation(){
            return station;
        }

        private Station station;
    }
    private static int[] xPoints = {-3, 0, 3};

    private static int[] yPoints = {3, -3, 3};

    /**
     *  A list of graphics to be painted on the map.
     */
    private OMGraphicList omgraphics;

    private MapBean mapBean;
}
