/**
 * OMEvent.java
 *
 * @author Created by Philip Oliver-Paull
 */

package edu.sc.seis.fissuresUtil.map.graphics;

import java.awt.Color;
import java.awt.Paint;
import org.apache.log4j.Logger;
import com.bbn.openmap.Layer;
import com.bbn.openmap.event.CenterEvent;
import com.bbn.openmap.omGraphics.OMCircle;
import com.bbn.openmap.omGraphics.OMGraphicList;
import edu.iris.Fissures.IfEvent.NoPreferredOrigin;
import edu.iris.Fissures.IfEvent.Origin;
import edu.sc.seis.fissuresUtil.cache.ProxyEventAccessOperations;
import edu.sc.seis.fissuresUtil.display.DisplayUtils;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.fissuresUtil.map.OpenMap;
import edu.sc.seis.fissuresUtil.map.colorizer.event.DefaultEventColorizer;

public class OMEvent extends OMGraphicList implements FissuresGraphic{

    public OMEvent(ProxyEventAccessOperations eao, Layer eventLayer, OpenMap map) throws NoPreferredOrigin{
        super(2);
        this.map = map;

        Origin prefOrigin = eao.get_preferred_origin();
        float lat = prefOrigin.my_location.latitude;
        float lon = prefOrigin.my_location.longitude;
        float mag = prefOrigin.magnitudes[0].value;

        double scale = 1.8;
        int lilDiameter = (int)Math.pow(scale, 3.0);
        lilCircle = new OMCircle(lat, lon, lilDiameter, lilDiameter);
        if (mag <= 3.0){
            bigCircle = new OMCircle(lat, lon, lilDiameter, lilDiameter);
        }
        else{
            mag = (float)(Math.pow(scale, (double)mag));
            bigCircle = new OMCircle(lat, lon, (int)Math.floor(mag), (int)Math.floor(mag));
        }
        event = eao;

        lilCircle.setLinePaint(Color.BLACK);
        bigCircle.setStroke(DisplayUtils.THREE_PIXEL_STROKE);
        setPaint(DefaultEventColorizer.DEFAULT_EVENT);
        add(bigCircle);
        add(lilCircle);
        generate(eventLayer.getProjection());
    }

    public ProxyEventAccessOperations getEvent(){
        return event;
    }

    public void select() {
        super.select();
        bigCircle.setFillPaint(new Color(0, 0, 0, 64));
        if (map.getWidthDegrees() > 300f){
            try{
                map.getMapBean().center(new CenterEvent(this,
                                               0.0f,
                                               event.get_preferred_origin().my_location.longitude));
            }catch(NoPreferredOrigin e){
                GlobalExceptionHandler.handle("For some reason, a NoPreferredOrigin has been called.", e);
            }
        }
    }

    public void deselect(){
        super.deselect();
        bigCircle.setFillPaint(OMGraphicList.clear);
    }

    public OMCircle getBigCircle(){ return bigCircle; }

    public void setPaint(Paint paint){
        lilCircle.setFillPaint(paint);
        bigCircle.setLinePaint(paint);
        bigCircle.setSelectPaint(paint);
    }

    private float originalScale;
    private ProxyEventAccessOperations event;
    private OpenMap map;
    private OMCircle lilCircle;
    private OMCircle bigCircle;
    private static Logger logger = Logger.getLogger(OMEvent.class);
}

