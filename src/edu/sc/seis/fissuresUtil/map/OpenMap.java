package edu.sc.seis.fissuresUtil.map;
import edu.sc.seis.fissuresUtil.map.layers.*;

/**
 * Map.java
 *
 * @author Created by Charlie Groves
 */

import com.bbn.openmap.*;

import com.bbn.openmap.event.MapMouseMode;
import com.bbn.openmap.layer.GraticuleLayer;
import com.bbn.openmap.layer.shape.ShapeLayer;
import com.bbn.openmap.proj.Proj;
import edu.sc.seis.fissuresUtil.chooser.ChannelChooser;
import edu.sc.seis.fissuresUtil.display.EventTableModel;
import edu.sc.seis.fissuresUtil.map.tools.ZoomTool;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.swing.ListSelectionModel;

public class OpenMap extends OpenMapComponent{

    public static final Color WATER = new Color(54, 179, 221);
    public static final float DEFAULT_SCALE = 200000000f;

    private MapHandler mapHandler;
    private LayerHandler lh;
    private EventLayer el;
    private MapBean mapBean;
    private MouseDelegator mouseDelegator;
    private List tools = new ArrayList();
    private EventTableModel tableModel;

    /**Creates a new openmap.  Both the channel chooser and the event table
     * model can be null.  If so, channels and events just won't get drawn
     */
    public OpenMap(EventTableModel etm, ListSelectionModel lsm, String shapefile){
        try{
            mapHandler = new MapHandler();
            mapHandler.add(this);
            // Create a MapBean
            mapBean = getMapBean();
            tableModel = etm;

            //get the projection and set its background color and center point
//            Proj proj = new Orthographic(new LatLonPoint(mapBean.DEFAULT_CENTER_LAT, mapBean.DEFAULT_CENTER_LON),
//                                         DEFAULT_SCALE,
//                                         mapBean.DEFAULT_WIDTH,
//                                         mapBean.DEFAULT_HEIGHT);
            Proj proj = (Proj)mapBean.getProjection();

            proj.setBackgroundColor(WATER);
            //mapBean.setProjection(proj);
            mapBean.setCenter(20, 200);

            mapHandler.add(mapBean);

            // Create and add a LayerHandler to the MapHandler.  The
            // LayerHandler manages Layers, whether they are part of the
            // map or not.  layer.setVisible(true) will add it to the map.
            // The LayerHandler has methods to do this, too.  The
            // LayerHandler will find the MapBean in the MapHandler.
            lh = new LayerHandler();
            mapHandler.add(lh);

            if(etm != null){
                el = new EventLayer(etm, lsm, mapBean);
                mapHandler.add(el);
                lh.addLayer(el, 1);

                DistanceLayer dl = new DistanceLayer(mapBean);
                el.addEQSelectionListener(dl);
                mapHandler.add(dl);
                lh.addLayer(dl);
                etm.addEventDataListener(dl);
            }

            GraticuleLayer gl = new GraticuleLayer();
            Properties graticuleLayerProps = gl.getProperties(new Properties());
            graticuleLayerProps.setProperty("10DegreeColor", "FF888888");
            gl.setProperties(graticuleLayerProps);

            gl.setShowRuler(true);

            mapHandler.add(gl);
            lh.addLayer(gl);

            // Create a ShapeLayer to show world political boundaries.
            ShapeLayer shapeLayer = new ShapeLayer();

            //Create shape layer properties
            Properties shapeLayerProps = new Properties();
            shapeLayerProps.put("prettyName", "Political Solid");
            shapeLayerProps.put("lineColor", "000000");
            shapeLayerProps.put("fillColor", "39DA87");
            shapeLayerProps.put("shapeFile", shapefile + ".shp");
            shapeLayerProps.put("spatialIndex", shapefile + ".ssx");
            shapeLayer.setProperties(shapeLayerProps);
            shapeLayer.setVisible(true);
            mapHandler.add(shapeLayer);

            // Create the directional and zoom control tool
            //OMToolSet omts = new OMToolSet();
            // Create an OpenMap toolbar
            //ToolPanel toolBar = new ToolPanel();
            //Create an OpenMap Info Line
            InformationDelegator infoDel = new InformationDelegator();
            infoDel.setShowLights(false);
            //Create an OpenMap Mouse Delegator
            mouseDelegator = new MouseDelegator();

            // Add the ToolPanel and the OMToolSet to the MapHandler.  The
            // OpenMapFrame will find the ToolPanel and attach it to the
            // top part of its content pane, and the ToolPanel will find
            // the OMToolSet and add it to itself.
            //mapHandler.add(omts);
            //mapHandler.add(toolBar);

            mapHandler.add(mouseDelegator);
            mapHandler.add(infoDel);
        } catch (MultipleSoloMapComponentException msmce) {
            // The MapHandler is only allowed to have one of certain
            // items.  These items implement the SoloMapComponent
            // interface.  The MapHandler can have a policy that
            // determines what to do when duplicate instances of the
            // same type of object are added - replace or ignore.

            // In this class, this will never happen, since we are
            // controlling that one MapBean, LayerHandler,
            // MouseDelegator, etc is being added to the MapHandler.
        }
    }

    public void addStationsFromChannelChooser(ChannelChooser chooser){
        if(chooser != null){
            StationLayer sl = new StationLayer(chooser);
            mapHandler.add(sl);
            lh.addLayer(sl,0);
            el.addEQSelectionListener(sl);
            tableModel.addEventDataListener(sl);
        }
    }

    public MapBean getMapBean(){
        if (mapBean == null){
            mapBean = new MapBean();
        }
        return mapBean;
    }

    public void addMouseMode(MapMouseMode mode){
        mouseDelegator.addMouseMode(mode);
        if (mode instanceof ZoomTool){
            ((ZoomTool)mode).addZoomListener(getMapBean());
        }
    }

    public void setActiveMouseMode(MapMouseMode mode){
        mouseDelegator.setActiveMouseMode(mode);
    }
}


