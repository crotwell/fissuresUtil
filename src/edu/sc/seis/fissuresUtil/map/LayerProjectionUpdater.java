package edu.sc.seis.fissuresUtil.map;

/**
 * LayerProjectionUpdater.java
 * 
 * @author Created by Charlie Groves
 */
import com.bbn.openmap.Layer;
import com.bbn.openmap.event.ProjectionEvent;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.proj.Projection;

public class LayerProjectionUpdater{
    public static void update(ProjectionEvent e,
                              OMGraphicList graphics,
                              Layer layer) {
        Projection projection = e.getProjection();
        layer.setProjection(projection);
        if(graphics != null) {
            synchronized(graphics) {
                graphics.regenerate(projection);
            }
        }
        layer.repaint();
    }
}