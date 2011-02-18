/**
 * OMEvent.java
 * 
 * @author Created by Philip Oliver-Paull
 */
package edu.sc.seis.fissuresUtil.map.graphics;

import java.awt.Color;
import java.awt.Paint;

import com.bbn.openmap.Layer;
import com.bbn.openmap.event.CenterEvent;
import com.bbn.openmap.omGraphics.OMCircle;
import com.bbn.openmap.omGraphics.OMGraphicList;

import edu.iris.Fissures.IfEvent.NoPreferredOrigin;
import edu.sc.seis.fissuresUtil.cache.ProxyEventAccessOperations;
import edu.sc.seis.fissuresUtil.display.DisplayUtils;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.fissuresUtil.flow.extractor.event.MagnitudeValueExtractor;
import edu.sc.seis.fissuresUtil.flow.extractor.model.LocationExtractor;
import edu.sc.seis.fissuresUtil.map.OpenMap;
import edu.sc.seis.fissuresUtil.map.colorizer.event.DefaultEventColorizer;

public class OMEvent extends OMGraphicList implements FissuresGraphic {

    public OMEvent(ProxyEventAccessOperations eao, Layer eventLayer, OpenMap map) {
        super(2);
        this.map = map;
        LocationExtractor le = new LocationExtractor();
        float lat = le.extract(eao).latitude;
        float lon = le.extract(eao).longitude;
        float mag = new MagnitudeValueExtractor().extract(eao);
        double scale = 1.8;
        int lilDiameter = (int)Math.pow(scale, 3.0);
        lilCircle = new OMCircle(lat, lon, lilDiameter, lilDiameter);
        if(mag <= 3.0) {
            bigCircle = new OMCircle(lat, lon, lilDiameter, lilDiameter);
        } else {
            mag = (float)(Math.pow(scale, mag));
            bigCircle = new OMCircle(lat,
                                     lon,
                                     (int)Math.floor(mag),
                                     (int)Math.floor(mag));
        }
        event = eao;
        lilCircle.setLinePaint(Color.BLACK);
        bigCircle.setStroke(DisplayUtils.THREE_PIXEL_STROKE);
        setPaint(DefaultEventColorizer.DEFAULT_EVENT);
        add(bigCircle);
        add(lilCircle);
        generate(eventLayer.getProjection());
    }

    public ProxyEventAccessOperations getEvent() {
        return event;
    }

    public void select() {
        super.select();
        bigCircle.setFillPaint(new Color(0, 0, 0, 64));
        if(map.getWidthDegrees() > 300f) {
            try {
                map.getMapBean()
                        .center(new CenterEvent(this,
                                                0.0f,
                                                event.get_preferred_origin().getLocation().longitude));
            } catch(NoPreferredOrigin e) {
                GlobalExceptionHandler.handle("For some reason, a NoPreferredOrigin has been called.",
                                              e);
            }
        }
    }

    public void deselect() {
        super.deselect();
        bigCircle.setFillPaint(OMGraphicList.clear);
    }

    public OMCircle getBigCircle() {
        return bigCircle;
    }

    public void setPaint(Paint paint) {
        lilCircle.setFillPaint(paint);
        bigCircle.setLinePaint(paint);
        bigCircle.setSelectPaint(paint);
    }

    private ProxyEventAccessOperations event;

    private OpenMap map;

    private OMCircle lilCircle;

    private OMCircle bigCircle;
}
