package edu.sc.seis.fissuresUtil.map.layers;
import edu.sc.seis.fissuresUtil.map.*;


/**
 * EventLayer.java
 *
 * @author Created by Charlie Groves
 */

import edu.sc.seis.fissuresUtil.chooser.*;

import com.bbn.openmap.event.ProjectionEvent;
import com.bbn.openmap.event.SelectMouseMode;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMPoly;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.NoPreferredOrigin;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.IfNetwork.Station;
import edu.sc.seis.TauP.SphericalCoords;
import edu.sc.seis.fissuresUtil.display.EQSelectionEvent;
import edu.sc.seis.fissuresUtil.display.EQSelectionListener;
import edu.sc.seis.fissuresUtil.display.EventDataListener;
import java.awt.Color;
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
import edu.sc.seis.fissuresUtil.display.EQDataEvent;

public class StationLayer extends MouseAdapterLayer implements StationDataListener,
    StationSelectionListener, AvailableStationDataListener, EQSelectionListener, EventDataListener{
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
        c.addAvailableStationDataListener(this);
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


    /**
     * Method stationAvailabiltyChanged
     *
     * @param    e                   an AvailableStationDataEvent
     *
     */
    public void stationAvailabiltyChanged(AvailableStationDataEvent e) {
        Station station = e.getStation();
        boolean isUp = e.stationIsUp();

        Iterator it = omgraphics.iterator();
        boolean found = false;
        while (it.hasNext() && !found){
            OMStation current = (OMStation)it.next();
            if (current.getStation() == station){
                current.setIsUp(isUp);
            }
        }
        repaint();
    }


    /**
     * Method eqSelectionChanged
     *
     * @param    eqSelectionEvent    an EQSelectionEvent
     *
     */
    public void eqSelectionChanged(EQSelectionEvent eqSelectionEvent) {
        currentEvent = eqSelectionEvent.getEvents()[0];
    }


    /**
     * Method eventDataChanged
     *
     * @param    eqDataEvent         an EQDataEvent
     *
     */
    public void eventDataChanged(EQDataEvent eqDataEvent) {}

    /**
     * Method eventDataCleared
     *
     */
    public void eventDataCleared() {
        currentEvent = null;
    }

    private class OMStation extends OMPoly{
        public OMStation(Station stat){
            super(stat.my_location.latitude,
                  stat.my_location.longitude, xPoints, yPoints,
                  OMPoly.COORDMODE_ORIGIN);
            station = stat;
            setDefaultColor(DOWN_STATION);
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

        public void setIsUp(boolean up){
            isUp = up;
            if(up){
                setDefaultColor(STATION);
            }
            else{
                setDefaultColor(DOWN_STATION);
            }
        }

        public boolean isUp(){
            return isUp;
        }

        public void deselect(){
            setFillPaint(defaultColor);
            selected = false;
        }

        public void setDefaultColor(Color c){
            defaultColor = c;
            setFillPaint(defaultColor);
        }

        private boolean selected = false;

        private boolean isUp = true;

        private Station station;

        private Color defaultColor;
    }


    private static int[] xPoints = {-5, 0, 5};

    private static int[] yPoints = {5, -5, 5};

    /**
     *  A list of graphics to be painted on the map.
     */
    private OMGraphicList omgraphics;

    private static String[] modeList = { SelectMouseMode.modeID } ;

    private ChannelChooser chooser;

    private JPopupMenu currentPopup;

    private EventAccessOperations currentEvent;

    public static final Color STATION = new Color(43, 33, 243);

    public static final Color DOWN_STATION = new Color(183, 183, 183);

    public String[] getMouseModeServiceList() {
        return modeList;
    }

    public boolean mouseClicked(MouseEvent e){
        if (currentPopup != null){
            currentPopup.setVisible(false);
            currentPopup = null;
        }

        Iterator it = omgraphics.iterator();
        List stationsUnderMouse = new ArrayList();
        while(it.hasNext()){
            OMStation current = (OMStation)it.next();
            if(current.contains(e.getX(), e.getY())){
                stationsUnderMouse.add(current.getStation());
            }
        }
        if (stationsUnderMouse.size() > 0){
            if (stationsUnderMouse.size() == 1){
                chooser.toggleStationSelected((Station)stationsUnderMouse.get(0));
            }
            else{
                final JPopupMenu popup = new JPopupMenu();
                it = stationsUnderMouse.iterator();
                while (it.hasNext()){
                    final Station current = (Station)it.next();
                    final JMenuItem menuItem = new JMenuItem(getStationInfo(current, currentEvent));
                    menuItem.addActionListener(new ActionListener(){
                                public void actionPerformed(ActionEvent e) {
                                    chooser.toggleStationSelected(current);
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

        return false;
    }

    public boolean mouseMoved(MouseEvent e){
        synchronized(omgraphics){
            Iterator it = omgraphics.iterator();
            while(it.hasNext()){
                OMStation current = (OMStation)it.next();
                if(current.contains(e.getX(), e.getY())){
                    Station station = current.getStation();
                    fireRequestInfoLine(getStationInfo(station, currentEvent));
                    return true;
                }
            }
            fireRequestInfoLine(" ");
            return false;
        }
    }

    public static String getStationInfo(Station station, EventAccessOperations event){
        StringBuffer buf = new StringBuffer();
        buf.append("Station: ");
        buf.append(station.get_code() + "-");
        buf.append(station.name);
        if (event != null){
            try{
                double dist = StationLayer.calcDistStationToEvent(station, event);
                buf.append(" | Distance to Event: ");
                buf.append(dist);
                buf.append(" deg");
            }
            catch(NoPreferredOrigin e){}
        }
        return buf.toString();
    }

    public static double calcDistStationToEvent(Station station, EventAccessOperations event)
        throws NoPreferredOrigin{

        Origin origin = event.get_preferred_origin();
        double dist = SphericalCoords.distance(origin.my_location.latitude,
                                        origin.my_location.longitude,
                                        station.my_location.latitude,
                                        station.my_location.longitude);

        dist = (int)(dist*100);
        dist = dist/100;

        return dist;
    }
}
