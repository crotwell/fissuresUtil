package edu.sc.seis.fissuresUtil.map.layers;

import java.awt.Graphics;
import org.apache.log4j.Logger;
import com.bbn.openmap.event.ProjectionEvent;
import com.bbn.openmap.layer.shape.ShapeLayer;
import com.bbn.openmap.proj.Projection;
import edu.sc.seis.fissuresUtil.exceptionHandler.ExceptionReporterUtils;


/**
 * @author oliverpa
 * Created on Aug 19, 2004
 */
public class FissuresShapeLayer extends ShapeLayer implements OverriddenOMLayer{
    
    public void renderDataForProjection(Projection p, Graphics g) {
        logger.debug(ExceptionReporterUtils.getMemoryUsage()
                + " InformativeShapeLayer: rendering shape layer");
        super.renderDataForProjection(p, g);
    }
    
    public void setOverrideProjectionChanged(boolean override) {
        overrideProjectionChanged = override;
    }
    
    public void projectionChanged(ProjectionEvent e){
        if(overrideProjectionChanged) {
            //this is a hack to make the layer render
            //properly when output to png
            doPrepare();
            repaint();
        } else {
            super.projectionChanged(e);
        }
    }
    
    private boolean overrideProjectionChanged = false;
    private static Logger logger = Logger.getLogger(FissuresShapeLayer.class);    
}
