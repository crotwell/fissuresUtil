/**
 * DistanceLayer.java
 *
 * @author Created by Philip Oliver-Paull
 */

package edu.sc.seis.fissuresUtil.map;

import com.bbn.openmap.LatLonPoint;
import com.bbn.openmap.Layer;
import com.bbn.openmap.event.ProjectionEvent;
import com.bbn.openmap.omGraphics.OMCircle;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMText;
import com.bbn.openmap.proj.Length;
import com.bbn.openmap.proj.Projection;
import edu.iris.Fissures.IfEvent.Origin;
import edu.sc.seis.fissuresUtil.display.DisplayUtils;
import edu.sc.seis.fissuresUtil.display.EQDataEvent;
import edu.sc.seis.fissuresUtil.display.EQSelectionEvent;
import edu.sc.seis.fissuresUtil.display.EQSelectionListener;
import edu.sc.seis.fissuresUtil.display.EventDataListener;
import java.awt.Color;
import java.awt.Point;

public class DistanceLayer extends Layer implements EQSelectionListener, EventDataListener{

    private OMGraphicList distCircles = new OMGraphicList();

    public DistanceLayer(){
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
        distCircles.clear();
        try{
            Origin origin = eqSelectionEvent.getEvents()[0].get_preferred_origin();
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
        distCircles.clear();
    }

    private void makeDistCircle(LatLonPoint llp, double radiusDegrees){
        Projection proj = getProjection();

        OMCircle circle = new DistCircle(llp, radiusDegrees);
        LatLonPoint labelPointUp = makeTextLabelLatLon(llp, radiusDegrees, proj, true);
        if (proj.isPlotable(labelPointUp)){
            distCircles.add(new TextLabel(labelPointUp, Integer.toString((int)radiusDegrees)));
        }
        LatLonPoint labelPointDown = makeTextLabelLatLon(llp, radiusDegrees, proj, false);
        if (proj.isPlotable(labelPointDown)){
            distCircles.add(new TextLabel(labelPointDown, Integer.toString((int)radiusDegrees)));
        }
        distCircles.add(circle);
    }

    private void makeDistCircles(LatLonPoint llp){
        makeDistCircle(llp, 30.0);
        makeDistCircle(llp, 60.0);
        makeDistCircle(llp, 90.0);
    }

    public void paint(java.awt.Graphics g){
        distCircles.render(g);
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
}

