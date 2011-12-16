package edu.sc.seis.fissuresUtil.map.layers;

import java.awt.Graphics;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import com.bbn.openmap.event.ProjectionEvent;
import com.bbn.openmap.omGraphics.OMGraphic;

import edu.sc.seis.fissuresUtil.cache.ProxyEventAccessOperations;
import edu.sc.seis.fissuresUtil.map.OpenMap;
import edu.sc.seis.fissuresUtil.map.graphics.OMEvent;

public class EventListLayer extends MouseAdapterLayer {

    private OpenMap map;

    public EventListLayer(OpenMap map) {
        this.map = map;
    }

    public void projectionChanged(ProjectionEvent e) {
        for(Iterator iter = list.iterator(); iter.hasNext();) {
            getGraphic(iter.next()).generate(e.getProjection());
        }
    }

    private OMGraphic getGraphic(Object object) {
        OMGraphic g = (OMGraphic)listItemToOMGraphic.get(object);
        if(g == null) {
            g = createGraphic(object);
            listItemToOMGraphic.put(object, g);
        }
        return g;
    }

    private OMGraphic createGraphic(Object object) {
        return new OMEvent((ProxyEventAccessOperations)object, this, map);
    }

    public void paint(Graphics g) {
        for(Iterator iter = list.iterator(); iter.hasNext();) {
            getGraphic(iter.next()).render(g);
        }
    }

    public String[] getMouseModeServiceList() {
        // TODO Auto-generated method stub
        return null;
    }

    private List list;

    private Map listItemToOMGraphic = new WeakHashMap();
}
