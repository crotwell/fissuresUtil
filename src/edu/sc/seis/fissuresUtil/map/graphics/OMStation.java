/**
 * OMStation.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.map.graphics;

import com.bbn.openmap.Layer;
import com.bbn.openmap.omGraphics.OMPoly;
import edu.iris.Fissures.IfNetwork.Station;
import edu.sc.seis.fissuresUtil.display.DisplayUtils;
import java.awt.Color;

public class OMStation extends OMPoly implements FissuresGraphic{
    public OMStation(Station stat, Layer stationLayer){
        super(stat.my_location.latitude,
              stat.my_location.longitude, xPoints, yPoints,
              OMPoly.COORDMODE_ORIGIN);
        station = stat;
        setDefaultColor(DisplayUtils.NO_STATUS_STATION);
        setStroke(DisplayUtils.ONE_PIXEL_STROKE);
        setLinePaint(Color.BLACK);
        generate(stationLayer.getProjection());
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

    public void resetIsUp() {
        setDefaultColor(DisplayUtils.NO_STATUS_STATION);
    }

    public void setIsUp(boolean up){
        isUp = up;
        if(up){
            setDefaultColor(DisplayUtils.STATION);
        }
        else{
            setDefaultColor(DisplayUtils.DOWN_STATION);
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

    private static int[] xPoints = {-5, 0, 5};

    private static int[] yPoints = {5, -5, 5};
}

