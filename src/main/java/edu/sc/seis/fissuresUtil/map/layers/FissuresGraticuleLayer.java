package edu.sc.seis.fissuresUtil.map.layers;

import com.bbn.openmap.event.ProjectionEvent;
import com.bbn.openmap.layer.GraticuleLayer;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.proj.Projection;

/**
 * @author oliverpa Created on Aug 19, 2004
 */
public class FissuresGraticuleLayer extends GraticuleLayer implements
        OverriddenOMLayer {

    public void setOverrideProjectionChanged(boolean override) {
        overrideProjectionChanged = override;
    }

    public void projectionChanged(ProjectionEvent e) {
        if(overrideProjectionChanged) {
            //this is a hack to make the layer render
            //properly when output to png
            doPrepare();
            repaint();
        } else {
            super.projectionChanged(e);
        }
    }

    public synchronized OMGraphicList prepare() {
        //this is a hack to keep the nullpointers
        //away that were created by the above hack
        Projection projection = getProjection();
        if(projection == null) { return new OMGraphicList(); }
        return super.prepare();
    }

    private boolean overrideProjectionChanged = false;
}