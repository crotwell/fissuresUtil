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
import edu.iris.Fissures.IfEvent.Origin;
import edu.sc.seis.fissuresUtil.display.DisplayUtils;
import edu.sc.seis.fissuresUtil.display.EQSelectionEvent;
import edu.sc.seis.fissuresUtil.display.EQSelectionListener;
import java.awt.Color;
import edu.sc.seis.fissuresUtil.display.EventDataListener;
import edu.sc.seis.fissuresUtil.display.EQDataEvent;

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

    private void makeDistCircles(LatLonPoint llp){
        OMCircle lilCircle = new DistCircle(llp, (float)Math.PI*(float)(1.0/6.0));
        distCircles.add(lilCircle);

        OMCircle biggerCircle = new DistCircle(llp, (float)Math.PI*(float)(1.0/3.0));
        distCircles.add(biggerCircle);

        OMCircle biggestCircle = new DistCircle(llp, (float)Math.PI*(float)(1.0/2.0));
        distCircles.add(biggestCircle);
    }

    public void paint(java.awt.Graphics g){
        distCircles.render(g);
    }

    private class DistCircle extends OMCircle{

        public DistCircle(LatLonPoint llp, float radiusRadians){
            super(llp.getLatitude(), llp.getLongitude(), radiusRadians, Length.RADIAN);
            this.setLineColor(Color.magenta);
            this.setStroke(DisplayUtils.ONE_PIXEL_STROKE);
            generate(getProjection());
        }

    }

    private class TextLabel extends OMText{
        public TextLabel(LatLonPoint llp, float radiusRadians){

        }
    }
}

