/**
 * OMEvent.java
 *
 * @author Created by Philip Oliver-Paull
 */

package edu.sc.seis.fissuresUtil.map.graphics;

import com.bbn.openmap.Layer;
import com.bbn.openmap.MapBean;
import com.bbn.openmap.event.CenterEvent;
import com.bbn.openmap.omGraphics.OMCircle;
import com.bbn.openmap.omGraphics.OMGraphicList;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.NoPreferredOrigin;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.display.DisplayUtils;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import java.awt.Color;

public class OMEvent extends OMGraphicList{
    public OMEvent(EventAccessOperations eao, Layer eventLayer, MapBean mapBean) throws NoPreferredOrigin{
        super(2);
        this.mapBean = mapBean;

        Origin prefOrigin = eao.get_preferred_origin();
        float lat = prefOrigin.my_location.latitude;
        float lon = prefOrigin.my_location.longitude;
        float mag = prefOrigin.magnitudes[0].value;

        double scale = 1.8;
        int lilDiameter = (int)Math.pow(scale, 3.0);
        OMCircle lilCircle = new OMCircle(lat, lon, lilDiameter, lilDiameter);
        if (mag <= 3.0){
            bigCircle = new OMCircle(lat, lon, lilDiameter, lilDiameter);
        }
        else{
            mag = (float)(Math.pow(scale, (double)mag));
            bigCircle = new OMCircle(lat, lon, (int)Math.floor(mag), (int)Math.floor(mag));
        }
        event = new CacheEvent(eao);

        Color color = getDepthColor((QuantityImpl)prefOrigin.my_location.depth);
        lilCircle.setLinePaint(Color.BLACK);
        lilCircle.setFillPaint(color);
        bigCircle.setStroke(DisplayUtils.THREE_PIXEL_STROKE);
        bigCircle.setLinePaint(color);
        bigCircle.setSelectPaint(color);
        add(bigCircle);
        add(lilCircle);
        generate(eventLayer.getProjection());
    }

    public CacheEvent getEvent(){
        return event;
    }

    public void select() {
        super.select();
        bigCircle.setFillPaint(new Color(0, 0, 0, 64));
        try{
            mapBean.center(new CenterEvent(this,
                                           0.0f,
                                           event.get_preferred_origin().my_location.longitude));
        }catch(NoPreferredOrigin e){
            GlobalExceptionHandler.handle("For some reason, a NoPreferredOrigin has been called.", e);
        }
    }

    public void deselect(){
        super.deselect();
        bigCircle.setFillPaint(OMGraphicList.clear);
    }

    public OMCircle getBigCircle(){ return bigCircle; }

    private Color getDepthColor(QuantityImpl depth){
        double depthKM = depth.convertTo(UnitImpl.KILOMETER).value;
        Color color = DisplayUtils.MEDIUM_DEPTH_EVENT;
        if (depthKM <= 40.0){
            color = DisplayUtils.SHALLOW_DEPTH_EVENT;
        }
        if (depthKM >= 150.0){
            color = DisplayUtils.DEEP_DEPTH_EVENT;
        }
        return color;
    }

    private CacheEvent event;
    private MapBean mapBean;
    private OMCircle bigCircle;
}

