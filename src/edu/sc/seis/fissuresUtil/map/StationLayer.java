package edu.sc.seis.fissuresUtil.map;

/**
 * EventLayer.java
 *
 * @author Created by Charlie Groves
 */

import com.bbn.openmap.event.MapMouseListener;
import com.bbn.openmap.event.NavMouseMode;
import com.bbn.openmap.event.ProjectionEvent;
import com.bbn.openmap.event.SelectMouseMode;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMPoly;
import edu.iris.Fissures.IfNetwork.Station;
import edu.sc.seis.fissuresUtil.chooser.ChannelChooser;
import edu.sc.seis.fissuresUtil.chooser.StationDataEvent;
import edu.sc.seis.fissuresUtil.chooser.StationDataListener;
import edu.sc.seis.fissuresUtil.chooser.StationSelectionEvent;
import edu.sc.seis.fissuresUtil.chooser.StationSelectionListener;
import java.awt.Color;
import java.awt.event.MouseEvent;
import java.util.Iterator;

public class StationLayer extends MouseAdapterLayer implements StationDataListener,
    StationSelectionListener{
    /**
     * Adds this layer as a listener on station data arriving and station
     * selection occuring on the channel chooser being passed in.
     * If station data is passed, a blue triangle is drawn on the map.
     * If a station is selected, the triangle turns red.
     */
    public StationLayer(ChannelChooser c) {
        omgraphics = new OMGraphicList();
        //add the necessary data listeners to the channel chooser, provided it exists
        c.addStationDataListener(this);
        c.addStationSelectionListener(this);
        chooser = c;
    }

    public void paint(java.awt.Graphics g) {
        omgraphics.render(g);
    }

    public void projectionChanged(ProjectionEvent e) {
        LayerProjectionUpdater.update(e, omgraphics, this);
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
            setLinePaint(Color.BLACK);
            generate(getProjection());
        }

        public Station getStation(){
            return station;
        }

        public void select(){
            setFillPaint(Color.RED);
            selected = true;
        }

        public boolean toggleSelection(){
            if(!selected){
                select();
            }else{
                deselect();
            }
            return selected;
        }

        public void deselect(){
            setFillPaint(Color.BLUE);
            selected = false;
        }

        private boolean selected = false;

        private Station station;
    }
    private static int[] xPoints = {-5, 0, 5};

    private static int[] yPoints = {5, -5, 5};

    /**
     *  A list of graphics to be painted on the map.
     */
    private OMGraphicList omgraphics;

    private static String[] modeList = { SelectMouseMode.modeID } ;

    private ChannelChooser chooser;

    public MapMouseListener getMapMouseListener(){
        return this;
    }

    public String[] getMouseModeServiceList() {
        return modeList;
    }

    public boolean mouseClicked(MouseEvent e){
        Iterator it = omgraphics.iterator();
        while(it.hasNext()){
            OMStation current = (OMStation)it.next();
            if(current.contains(e.getX(), e.getY())){
                chooser.toggleStationSelected(current.getStation());
                //current.toggleSelection();
                //chooser.setStationSelected(current.getStation());
                //repaint();
            }
        }
        return true;
    }
}
