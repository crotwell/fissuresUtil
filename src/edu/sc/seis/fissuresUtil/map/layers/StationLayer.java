package edu.sc.seis.fissuresUtil.map.layers;
import edu.sc.seis.fissuresUtil.chooser.*;
import java.util.*;

import com.bbn.openmap.event.ProjectionEvent;
import com.bbn.openmap.event.SelectMouseMode;
import com.bbn.openmap.omGraphics.OMGraphicList;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.NoPreferredOrigin;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.IfNetwork.Station;
import edu.sc.seis.TauP.SphericalCoords;
import edu.sc.seis.fissuresUtil.display.EQDataEvent;
import edu.sc.seis.fissuresUtil.display.EQSelectionEvent;
import edu.sc.seis.fissuresUtil.display.EQSelectionListener;
import edu.sc.seis.fissuresUtil.display.EventDataListener;
import edu.sc.seis.fissuresUtil.map.LayerProjectionUpdater;
import edu.sc.seis.fissuresUtil.map.graphics.OMStation;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.apache.log4j.Logger;

public class StationLayer extends MouseAdapterLayer implements StationDataListener,
    StationSelectionListener, AvailableStationDataListener, EQSelectionListener, EventDataListener{


    /**
     * Adds this layer as a listener on station data arriving and station
     * selection occuring on the channel chooser being passed in.
     * If station data is passed, a blue triangle is drawn on the map.
     * If a station is selected, the triangle turns red.
     */
    public StationLayer() {
        setName("Seismogram Station Layer");
    }

    public void paint(java.awt.Graphics g) {
        synchronized(omgraphics){
            omgraphics.render(g);
        }
    }

    public void projectionChanged(ProjectionEvent e) {
        LayerProjectionUpdater.update(e, omgraphics, this);
    }

    public void honorRepaint(boolean honor){ honorRepaint = honor; }

    public void repaint(){ if(honorRepaint) super.repaint(); }

    private boolean honorRepaint = true;

    /*This adds each of these stations to the layer
     *
     */
    public void stationDataChanged(StationDataEvent s) {
        Station[] stations = s.getStations();
        for (int i = 0; i < stations.length; i++){
            if (!stationMap.containsKey(stations[i].name)){
                stationNames.add(stations[i].name);
                synchronized(omgraphics){
                    omgraphics.add(new OMStation(stations[i], this));
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
                if (current.getStation().name.equals(station.name)) {
                    // if (StationIdUtil.areEqual(current.getStation().get_id(), station.get_id())){
                    current.setIsUp(isUp);
                    if(isUp){
                        omgraphics.moveIndexedToTop(omgraphics.indexOf(current));
                    }
                    else{
                        omgraphics.moveIndexedToBottom(omgraphics.indexOf(current));
                    }
                    found = true;
                }
            }
            if ( ! found ) {
                logger.debug("no match for available data update, "+station.get_code()+" "+isUp);
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
        Iterator it = omgraphics.iterator();
        while (it.hasNext()){
            OMStation current = (OMStation)it.next();
            current.resetIsUp();
        }
        repaint();
        currentEvent = eqSelectionEvent.getEvents()[0];
    }

    /** No impl here, only the eventDataCleared() method is needed*/
    public void eventDataAppended(EQDataEvent eqDataEvent) {
    }



    /** No impl here, only the eventDataCleared() method is needed*/
    public void eventDataChanged(EQDataEvent eqDataEvent) {}

    /**
     * Method eventDataCleared
     *
     */
    public void eventDataCleared() {
        currentEvent = null;
    }

    private static int[] xPoints = {-5, 0, 5};

    private static int[] yPoints = {5, -5, 5};

    /**
     *  A list of graphics to be painted on the map.
     */
    private OMGraphicList omgraphics = new OMGraphicList();

    private static String[] modeList = { SelectMouseMode.modeID } ;

    private EventAccessOperations currentEvent;

    private Map stationMap = new HashMap();

    private List stationNames = new ArrayList();

    public String[] getMouseModeServiceList() {
        return modeList;
    }

    public boolean mouseClicked(MouseEvent e){
		maybeKillCurrentPopup();

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
				popup.setInvoker(this);
                Iterator it = stationsUnderMouse.iterator();
                while (it.hasNext()){
                    final Station current = (Station)it.next();
                    final JMenuItem menuItem = new JMenuItem(getStationInfo(current, currentEvent));
                    menuItem.addActionListener(new ActionListener(){
                                public void actionPerformed(ActionEvent e) {
                                    toggleStationSelection(current);
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
                toggleStationSelection((Station)stationsUnderMouse.get(0));
            }
            return true;
        }

        return false;
    }


    //extend this method if you want this method to do something
    //other than change the selection color of the station
    public void toggleStationSelection(Station station){
        //noImpl
    }

    public boolean mouseMoved(MouseEvent e){
		maybeKillCurrentPopup();
		
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

    private static Logger logger = Logger.getLogger(StationLayer.class);
}


