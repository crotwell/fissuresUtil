package edu.sc.seis.fissuresUtil.map;

/**
 * LayerProjectionUpdater.java
 *
 * @author Created by Charlie Groves
 */
import com.bbn.openmap.Layer;
import com.bbn.openmap.event.ProjectionEvent;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import edu.sc.seis.fissuresUtil.cache.WorkerThreadPool;

public class LayerProjectionUpdater implements Runnable{
    public static void update(ProjectionEvent e, OMGraphicList graphics,
                              Layer layer){
        threadPool.invokeLater(new LayerProjectionUpdater(e, graphics, layer));
    }

    private LayerProjectionUpdater(ProjectionEvent e,
                                   OMGraphicList graphics,
                                   Layer layer){
        event = e;
        this.graphics = graphics;
        this.layer = layer;
    }

    public void run(){
        layer.setProjection(event.getProjection());
        synchronized(graphics){
            graphics.regenerate(event.getProjection());
        }
        layer.repaint();
    }

    private ProjectionEvent event;
    private OMGraphicList graphics;
    private Layer layer;

    private static WorkerThreadPool threadPool = new WorkerThreadPool("Map Layer Updater", 1);
}
