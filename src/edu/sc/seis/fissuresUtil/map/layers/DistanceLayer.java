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
import java.awt.Font;
import java.awt.Paint;
import java.awt.Point;
import java.awt.event.MouseEvent;

public class DistanceLayer extends MouseAdapterLayer implements EQSelectionListener, EventDataListener{

    private static String[] modeList = { SelectMouseMode.modeID } ;

    private OMGraphicList distCircles = new OMGraphicList();
    private MapBean mapBean;
    private EventAccessOperations[] events;

    public DistanceLayer(MapBean mapBean){
        this.mapBean = mapBean;
        setName("Distance Information Layer");
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
        synchronized(distCircles){
            distCircles.clear();
        }
        events = null;
    }

    private void makeDistCircle(LatLonPoint llp, double radiusDegrees, Paint p){
        synchronized(distCircles){
            OMCircle circle = new DistCircle(llp, radiusDegrees, p);
            distCircles.add(circle);

            LatLonPoint[] labelPoints = new LatLonPoint[4];
            labelPoints[0] = makeTextLabelLatLon(llp, radiusDegrees, 0);
            labelPoints[1] = makeTextLabelLatLon(llp, -radiusDegrees, 0);
            labelPoints[2] = new WrapAroundLatLonPoint(-labelPoints[0].getLatitude(), labelPoints[0].getLongitude() + 180);
            labelPoints[3] = new WrapAroundLatLonPoint(-labelPoints[1].getLatitude(), labelPoints[1].getLongitude() + 180);

            int labelInt = (int)radiusDegrees;
            for (int i = 0; i < labelPoints.length; i++) {
                if (i > 1){
                    labelInt = 180 - (int)radiusDegrees;
                }
                TextLabel label = new TextLabel(adjustLatLonByPixels(labelPoints[i], 0, 6),
                                                Integer.toString(labelInt));
                //label.setRenderType(OMGraphic.RENDERTYPE_OFFSET);

                distCircles.add(label);
            }
        }
    }

    private void makeDistCircles(LatLonPoint llp){
        makeDistCircle(llp, 30.0, new Color(255, 255, 255));
        makeDistCircle(llp, 60.0, new Color(255, 191, 255));
        makeDistCircle(llp, 90.0, new Color(255, 127, 255));
        makeDistCircle(llp, 120.0, new Color(255, 64, 255));
        makeDistCircle(llp, 150.0, new Color(255, 0, 255));
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
        textLLP = new WrapAroundLatLonPoint(center.getLatitude() + degreesNorth, center.getLongitude() + degreesEast);
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

        public DistCircle(LatLonPoint llp, double radiusDegrees, Paint p){
            super(llp.getLatitude(), llp.getLongitude(), (float)radiusDegrees, Length.DECIMAL_DEGREE);
            this.setLinePaint(p);
            //this.setStroke(DisplayUtils.ONE_PIXEL_STROKE);
            this.setStroke(DisplayUtils.TWO_PIXEL_STROKE);
            generate(getProjection());
        }

    }

    private class TextLabel extends OMText{
        public TextLabel(LatLonPoint position, String text){
            super(position.getLatitude(), position.getLongitude(), text, OMText.JUSTIFY_CENTER);
            setFont(new Font("Serif", Font.BOLD, 14));
            setLinePaint(Color.WHITE);
        }
    }

    private class WrapAroundLatLonPoint extends LatLonPoint{
        public WrapAroundLatLonPoint(double lat, double lon){
            super(0,0);
            if (lat > 90){
                lat = 180 - lat;
                if (lon > 0){
                    lon = lon - 180;
                }
                else{
                    lon = lon + 180;
                }
            }
            LatLonPoint latLon = new LatLonPoint(lat, lon);
            super.setLatLon(latLon);
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

