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
        synchronized(distCircles){
            //System.out.println("making distance circles: radius " + radiusDegrees);
            OMCircle circle = new DistCircle(llp, radiusDegrees);
            distCircles.add(circle);
            if (radiusDegrees != 90.0){
                OMCircle oppCircle = new DistCircle(llp, 180.0 - radiusDegrees);
                distCircles.add(oppCircle);
            }

            LatLonPoint[] labelPoints = new LatLonPoint[4];
            labelPoints[0] = makeTextLabelLatLon(llp, radiusDegrees, 0);
            labelPoints[1] = makeTextLabelLatLon(llp, -radiusDegrees, 0);
            labelPoints[2] = new LatLonPoint(-labelPoints[0].getLatitude(), labelPoints[0].getLongitude() + 180);
            labelPoints[3] = new LatLonPoint(-labelPoints[1].getLatitude(), labelPoints[1].getLongitude() + 180);

            int labelInt = (int)radiusDegrees;
            for (int i = 0; i < labelPoints.length; i++) {
                if (i > 1){
                    labelInt = 180 - (int)radiusDegrees;
                }
                //System.out.println("adding label: " + labelInt);
                distCircles.add(new TextLabel(adjustLatLonByPixels(labelPoints[i], 0, 6),
                                              Integer.toString(labelInt)));
            }
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
        else fireRequestInfoLine(" ");
        return false;
    }

    private LatLonPoint makeTextLabelLatLon(LatLonPoint center, double degreesNorth, double degreesEast){
        LatLonPoint textLLP;
        textLLP = new LatLonPoint(center.getLatitude() + degreesNorth, center.getLongitude() + degreesEast);
        return textLLP;
    }

    private LatLonPoint adjustLatLonByPixels(LatLonPoint latLon, int pixelsLeft, int pixelsUp){
        Projection proj = getProjection();

        Point XYPoint = proj.forward(latLon);
        XYPoint.setLocation(XYPoint.getX() + pixelsLeft, XYPoint.getY() - pixelsUp);
        LatLonPoint llp = proj.inverse(XYPoint);

        return llp;
    }

    private class DistCircle extends OMCircle{

        public DistCircle(LatLonPoint llp, double radiusDegrees){
            super(llp.getLatitude(), llp.getLongitude(), (float)radiusDegrees, Length.DECIMAL_DEGREE);
            this.setLinePaint(Color.magenta);
            //this.setStroke(DisplayUtils.ONE_PIXEL_STROKE);
            this.setStroke(DisplayUtils.TWO_PIXEL_STROKE);
            generate(getProjection());
        }

    }

    private class TextLabel extends OMText{
        public TextLabel(LatLonPoint position, String text){
            super(position.getLatitude(), position.getLongitude(), text, OMText.JUSTIFY_CENTER);
            setLinePaint(Color.WHITE);
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

