package edu.sc.seis.fissuresUtil.map.layers;
import edu.sc.seis.fissuresUtil.chooser.*;
import java.util.*;

import com.bbn.openmap.event.ProjectionEvent;
import com.bbn.openmap.event.SelectMouseMode;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMPoly;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.NoPreferredOrigin;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.IfNetwork.Station;
import edu.sc.seis.TauP.SphericalCoords;
import edu.sc.seis.fissuresUtil.display.DisplayUtils;
import edu.sc.seis.fissuresUtil.display.EQDataEvent;
import edu.sc.seis.fissuresUtil.display.EQSelectionEvent;
import edu.sc.seis.fissuresUtil.display.EQSelectionListener;
import edu.sc.seis.fissuresUtil.display.EventDataListener;
import edu.sc.seis.fissuresUtil.map.LayerProjectionUpdater;
import java.awt.Color;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

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
        synchronized(omgraphics){
            omgraphics.render(g);
        }
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
            if (!stationMap.containsKey(stations[i].name)){
                stationNames.add(stations[i].name);
                synchronized(omgraphics){
                    omgraphics.add(new OMStation(stations[i]));
                }
            }
            List stationList = (List)stationMap.get(stations[i].name);
            if (stationList == null){
                stationList = new LinkedList();
                stationMap.put(stations[i].name, stationList);
            }
            stationList.add(stations[i]);
        }
        repaint();
    }

    public void stationDataCleared() {
        synchronized(omgraphics){
            stationMap.clear();
            omgraphics.clear();
            repaint();
        }
    }

    /*takes all of the selected stations in this list, and if they're in the
     *StationLayer changes their line color to RED.
     */
    public void stationSelectionChanged(StationSelectionEvent s) {
        synchronized(omgraphics){
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

        synchronized(omgraphics){
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

    public ChannelChooser getChannelChooser(){
        return chooser;
    }

    private class OMStation extends OMPoly{
        public OMStation(Station stat){
            super(stat.my_location.latitude,
                  stat.my_location.longitude, xPoints, yPoints,
                  OMPoly.COORDMODE_ORIGIN);
            station = stat;
            setDefaultColor(DOWN_STATION);
            setStroke(DisplayUtils.ONE_PIXEL_STROKE);
            setLinePaint(Color.BLACK);
            generate(getProjection());
        }

        public Station getStation(){
            return station;
        }

        public void select(){
            //setFillPaint(Color.RED);
            setStroke(DisplayUtils.TWO_PIXEL_STROKE);
            setLinePaint(Color.WHITE);
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
            //setFillPaint(defaultColor);
            setStroke(DisplayUtils.ONE_PIXEL_STROKE);
            setLinePaint(Color.BLACK);
            selected = false;
        }

        public void setDefaultColor(Color c){
            defaultColor = c;
            setFillPaint(defaultColor);
        }

        private boolean selected = false;

        private boolean isUp = false;

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

    private Map stationMap = new HashMap();

    private List stationNames = new ArrayList();

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

        List stationsUnderMouse = new ArrayList();
        synchronized(omgraphics){
            Iterator it = omgraphics.iterator();
            while(it.hasNext()){
                OMStation current = (OMStation)it.next();
                if(current.contains(e.getX(), e.getY())){
                    stationsUnderMouse.add(current.getStation());
                }
            }
        }
        if (stationsUnderMouse.size() > 0){
            if (stationsUnderMouse.size() > 1){
                final JPopupMenu popup = new JPopupMenu();
                Iterator it = stationsUnderMouse.iterator();
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
            else{
                chooser.toggleStationSelected((Station)stationsUnderMouse.get(0));
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
            //fireRequestInfoLine(" ");
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
                double dist = StationLayer.calcDistEventFromLocation(station.my_location.latitude,
                                                                     station.my_location.longitude,
                                                                     event);
                buf.append(" | Distance from Event: ");
                buf.append(dist);
                buf.append(" deg");
            }
            catch(NoPreferredOrigin e){}
        }
        return buf.toString();
    }

    public static double calcDistEventFromLocation(double latitude, double longitude, EventAccessOperations event)
        throws NoPreferredOrigin{

        Origin origin = event.get_preferred_origin();
        double dist = SphericalCoords.distance(origin.my_location.latitude,
                                               origin.my_location.longitude,
                                               latitude,
                                               longitude);

        dist = (int)(dist*100);
        dist = dist/100;

        return dist;
    }
}


