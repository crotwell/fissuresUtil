/**
 * DistanceLayer.java
 *
 * @author Created by Philip Oliver-Paull
 */

package edu.sc.seis.fissuresUtil.map.layers;
import com.bbn.openmap.LatLonPoint;
import com.bbn.openmap.MapBean;
import com.bbn.openmap.event.ProjectionEvent;
import com.bbn.openmap.event.SelectMouseMode;
import com.bbn.openmap.omGraphics.OMCircle;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMText;
import com.bbn.openmap.proj.Length;
import com.bbn.openmap.proj.Projection;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.NoPreferredOrigin;
import edu.iris.Fissures.IfEvent.Origin;
import edu.sc.seis.fissuresUtil.display.DisplayUtils;
import edu.sc.seis.fissuresUtil.display.EQDataEvent;
import edu.sc.seis.fissuresUtil.display.EQSelectionEvent;
import edu.sc.seis.fissuresUtil.display.EQSelectionListener;
import edu.sc.seis.fissuresUtil.display.EventDataListener;
import edu.sc.seis.fissuresUtil.map.LayerProjectionUpdater;
import edu.sc.seis.fissuresUtil.map.layers.MouseAdapterLayer;
import java.awt.Color;
import java.awt.Point;
import java.awt.event.MouseEvent;

public class DistanceLayer extends MouseAdapterLayer implements EQSelectionListener, EventDataListener{

    private static String[] modeList = { SelectMouseMode.modeID } ;

    private OMGraphicList distCircles = new OMGraphicList();
    private MapBean mapBean;
    private EventAccessOperations[] events;

    public DistanceLayer(MapBean mapBean){
        this.mapBean = mapBean;
    }

    /**
     * Invoked when there has been a fundamental change to the Map.
     * <p>
     * Layers are expected to recompute their graphics (if this makes sense),
     * and then <code>repaint()</code> themselves.
     * @param e ProjectionEvent
     */
    public void projectionChanged(ProjectionEvent e) {
        LayerProjectionUpdater.update(e, distCircles, this);
    }


    /**
     * Method eqSelectionChanged
     *
     * @param    eqSelectionEvent    an EQSelectionEvent
     *
     * FIXME: make this work for more than one event
     */
    public void eqSelectionChanged(EQSelectionEvent eqSelectionEvent) {
        synchronized(distCircles){
            distCircles.clear();
        }
        try{
            events = eqSelectionEvent.getEvents();
            Origin origin = events[0].get_preferred_origin();
            LatLonPoint llp = new LatLonPoint(origin.my_location.latitude, origin.my_location.longitude);
            makeDistCircles(llp);
            repaint();
        }
        catch(Exception e){

        }
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
        synchronized(distCircles){
            distCircles.clear();
        }
    }

    private void makeDistCircle(LatLonPoint llp, double radiusDegrees){
        Projection proj = getProjection();

        OMCircle circle = new DistCircle(llp, radiusDegrees);
        LatLonPoint labelPointUp = makeTextLabelLatLon(llp, radiusDegrees, proj, true);
        if (proj.isPlotable(labelPointUp)){
            synchronized(distCircles){
                distCircles.add(new TextLabel(labelPointUp, Integer.toString((int)radiusDegrees)));
            }
        }
        LatLonPoint labelPointDown = makeTextLabelLatLon(llp, radiusDegrees, proj, false);
        if (proj.isPlotable(labelPointDown)){
            synchronized(distCircles){
                distCircles.add(new TextLabel(labelPointDown, Integer.toString((int)radiusDegrees)));
            }
        }
        synchronized(distCircles){
            distCircles.add(circle);
        }
    }

    private void makeDistCircles(LatLonPoint llp){
        makeDistCircle(llp, 30.0);
        makeDistCircle(llp, 60.0);
        makeDistCircle(llp, 90.0);
        makeDistCircle(llp, 120.0);
        makeDistCircle(llp, 150.0);
    }

    public void paint(java.awt.Graphics g){
        synchronized(distCircles){
            distCircles.render(g);
        }
    }

    public boolean mouseMoved(MouseEvent e){
        if (events != null && events[0] != null){
            try{
                LatLonPoint llp = mapBean.getCoordinates(e);
                double dist = StationLayer.calcDistEventFromLocation(llp.getLatitude(),
                                                                     llp.getLongitude(),
                                                                     events[0]);
                String message = "Distance from Event: " + dist + " deg";
                fireRequestInfoLine(message);
            }
            catch(NoPreferredOrigin npo){}
        }
        return false;
    }

    private static LatLonPoint makeTextLabelLatLon(LatLonPoint center, double radiusDegrees, Projection prj, boolean isUpper){
        LatLonPoint textLLP;

        if(isUpper){
            textLLP = new LatLonPoint(center.getLatitude() + radiusDegrees,
                                      center.getLongitude());
        }
        else{
            textLLP = new LatLonPoint(center.getLatitude() - radiusDegrees,
                                      center.getLongitude());
        }

        Point textXYPoint = prj.forward(textLLP);
        textXYPoint.setLocation(textXYPoint.getX(), textXYPoint.getY() - 5);
        textLLP = prj.inverse(textXYPoint);

        return textLLP;
    }

    private class DistCircle extends OMCircle{

        public DistCircle(LatLonPoint llp, double radiusDegrees){
            super(llp.getLatitude(), llp.getLongitude(), (float)radiusDegrees, Length.DECIMAL_DEGREE);
            this.setLinePaint(Color.magenta);
            this.setStroke(DisplayUtils.ONE_PIXEL_STROKE);
            generate(getProjection());
        }

    }

    private class TextLabel extends OMText{
        public TextLabel(LatLonPoint position, String text){
            super(position.getLatitude(), position.getLongitude(), text, OMText.JUSTIFY_CENTER);
        }
    }

    /**
     * Return a list of the modes that are interesting to the
     * MapMouseListener.  The source MouseEvents will only get sent to
     * the MapMouseListener if the mode is set to one that the
     * listener is interested in.
     * Layers interested in receiving events should register for
     * receiving events in "select" mode:
     * <code>
     * <pre>
     *  return new String[] {
     *      SelectMouseMode.modeID
     *  };
     * </pre>
     * <code>
     * @return String[] of modeID's
     * @see NavMouseMode#modeID
     * @see SelectMouseMode#modeID
     * @see NullMouseMode#modeID
     */
    public String[] getMouseModeServiceList() {
        return modeList;
    }
}

