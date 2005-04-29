package edu.sc.seis.fissuresUtil.map;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import com.bbn.openmap.BufferedMapBean;
import com.bbn.openmap.InformationDelegator;
import com.bbn.openmap.LatLonPoint;
import com.bbn.openmap.Layer;
import com.bbn.openmap.LayerHandler;
import com.bbn.openmap.MapBean;
import com.bbn.openmap.MapHandler;
import com.bbn.openmap.MouseDelegator;
import com.bbn.openmap.MultipleSoloMapComponentException;
import com.bbn.openmap.event.CenterEvent;
import com.bbn.openmap.event.LayerStatusEvent;
import com.bbn.openmap.event.LayerStatusListener;
import com.bbn.openmap.event.MapMouseMode;
import com.bbn.openmap.event.ProjectionEvent;
import com.bbn.openmap.event.ProjectionListener;
import com.bbn.openmap.event.ZoomEvent;
import com.bbn.openmap.gui.OMComponentPanel;
import com.bbn.openmap.image.SunJPEGFormatter;
import com.bbn.openmap.layer.GraticuleLayer;
import com.bbn.openmap.layer.etopo.ETOPOLayer;
import com.bbn.openmap.layer.shape.ShapeLayer;
import com.bbn.openmap.proj.Length;
import com.bbn.openmap.proj.ProjMath;
import com.bbn.openmap.proj.Projection;
import edu.sc.seis.fissuresUtil.exceptionHandler.ExceptionReporterUtils;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.fissuresUtil.map.colorizer.event.DepthEventColorizer;
import edu.sc.seis.fissuresUtil.map.layers.ColorMapEtopoLayer;
import edu.sc.seis.fissuresUtil.map.layers.DistanceLayer;
import edu.sc.seis.fissuresUtil.map.layers.EventLayer;
import edu.sc.seis.fissuresUtil.map.layers.EventTableLayer;
import edu.sc.seis.fissuresUtil.map.layers.FissuresGraticuleLayer;
import edu.sc.seis.fissuresUtil.map.layers.FissuresShapeLayer;
import edu.sc.seis.fissuresUtil.map.layers.OverriddenOMLayer;
import edu.sc.seis.fissuresUtil.map.layers.ShapeLayerPropertiesHandler;
import edu.sc.seis.fissuresUtil.map.layers.StationLayer;
import edu.sc.seis.fissuresUtil.map.tools.PanTool;
import edu.sc.seis.fissuresUtil.map.tools.ZoomTool;

public class OpenMap extends OMComponentPanel implements LayerStatusListener,
        ProjectionListener {

    public static final Color WATER = new Color(54, 179, 221);

    public static final float DEFAULT_SCALE = 200000000f;

    private LatLonPoint originalCenter = new LatLonPoint(0, 0);

    private float originalScale = DEFAULT_SCALE;

    private MapHandler mapHandler;

    private LayerHandler lh;

    private EventLayer el;

    private StationLayer stl;

    private DistanceLayer dl;

    private ETOPOLayer topol;

    private MapBean mapBean;

    private MouseDelegator mouseDelegator;

    private Map layerStatusMap = new HashMap();

    private static Logger logger = Logger.getLogger(OpenMap.class);

    private List shapeLayers = new ArrayList();

    /**
     * Creates a map with a shapelayer based on the file in fissuresUtil, a
     * graticule layer, an empty event layer with depth based colorizationand an
     * empty station layer
     */
    public OpenMap() {
        this(true);
    }

    public OpenMap(boolean graticule) {
        this(ShapeLayerPropertiesHandler.getProperties(), graticule);
        setEventLayer(new EventLayer(this, new DepthEventColorizer()));
        setStationLayer(new StationLayer());
    }

    /**
     * Create a map with a shape layer and a graticule
     */
    public OpenMap(Properties shapeLayerProps) {
        this(shapeLayerProps, true);
    }

    public OpenMap(Properties[] shapeLayerProps) {
        this(shapeLayerProps, true);
    }

    public OpenMap(Properties shapeLayerProps, boolean graticule) {
        this(new Properties[] {shapeLayerProps}, null, graticule);
    }

    public OpenMap(Properties[] shapeLayerProps, boolean graticule) {
        this(shapeLayerProps, null, graticule);
    }

    public OpenMap(Properties shapeLayerProps, Projection projection) {
        this(new Properties[] {shapeLayerProps}, projection, true);
    }

    public OpenMap(Properties[] shapeLayerProps, Projection projection) {
        this(shapeLayerProps, projection, true);
    }

    public OpenMap(Properties[] shapeLayerProps, Projection projection,
            boolean graticule) {
        try {
            setLayout(new BorderLayout());
            mapHandler = new MapHandler();
            mapHandler.add(this);
            mapBean = getMapBean();
            mapBean.addProjectionListener(this);
            if(projection != null) {
                mapBean.setProjection(projection);
            }
            originalCenter = mapBean.getProjection().getCenter();
            originalScale = mapBean.getScale();
            //            ProjectionFactory projFac = ProjectionFactory.getInstance();
            //            mapBean.setProjection(projFac.makeProjection(new
            // EckertIVLoader(),
            //                                                         mapBean.getCenter()
            //                                                                 .getLatitude(),
            //                                                         mapBean.getCenter()
            //                                                                 .getLongitude(),
            //                                                         mapBean.getScale(),
            //                                                         mapBean.getWidth(),
            //                                                         mapBean.getHeight()));
            mapBean.setBackgroundColor(WATER);
            mapHandler.add(mapBean);
            // Create and add a LayerHandler to the MapHandler. The
            // LayerHandler manages Layers, whether they are part of the
            // map or not. layer.setVisible(true) will add it to the map.
            // The LayerHandler has methods to do this, too. The
            // LayerHandler will find the MapBean in the MapHandler.
            lh = new LayerHandler();
            mapHandler.add(lh);
            //Create shape layers
            for(int i = 0; i < shapeLayerProps.length; i++) {
                FissuresShapeLayer shapeLayer = new FissuresShapeLayer();
                shapeLayer.addLayerStatusListener(this);
                shapeLayer.setProperties(shapeLayerProps[i]);
                shapeLayer.setVisible(true);
                shapeLayers.add(shapeLayer);
                mapHandler.add(shapeLayer);
                lh.addLayer(shapeLayer);
                updateShapeLayerProps();
            }
            //            Properties shapeLayerProps = new Properties();
            //            shapeLayerProps.put("prettyName", "Global Shape Layer");
            //            shapeLayerProps.put("lineColor", "FF000000");
            //            shapeLayerProps.put("lineColor", "FF39DA87");
            //            shapeLayerProps.put("lineWidth", "0");
            //            shapeLayerProps.put("fillColor", "FF39DA87");
            //            shapeLayerProps.put("shapeFile", globalShapefile + ".shp");
            //            shapeLayerProps.put("spatialIndex", globalShapefile + ".ssx");
            //            globalShapeLayer.setProperties(shapeLayerProps);
            //            globalShapeLayer.setVisible(true);
            //            shapeLayers.add(0, globalShapeLayer);
            //            mapHandler.add(globalShapeLayer);
            //            lh.addLayer(globalShapeLayer);
            //            updateShapeLayerProps();
            if(graticule) {
                GraticuleLayer gl = new FissuresGraticuleLayer();
                Properties graticuleLayerProps = gl.getProperties(new Properties());
                graticuleLayerProps.setProperty("prettyName", "Graticule Layer");
                graticuleLayerProps.setProperty("show1And5Lines", "true");
                graticuleLayerProps.setProperty("threshold", "3");
                graticuleLayerProps.setProperty("10DegreeColor", "88888888");
                graticuleLayerProps.setProperty("5DegreeColor", "88666666");
                graticuleLayerProps.setProperty("1DegreeColor", "88AAAAAA");
                gl.setProperties(graticuleLayerProps);
                gl.setShowRuler(true);
                mapHandler.add(gl);
                lh.addLayer(gl, 0);
            }
            InformationDelegator infoDel = new InformationDelegator();
            infoDel.setShowLights(false);
            mouseDelegator = new MouseDelegator();
            mapHandler.add(mouseDelegator);
            mapHandler.add(infoDel);
        } catch(MultipleSoloMapComponentException msmce) {
            // The MapHandler is only allowed to have one of certain
            // items. These items implement the SoloMapComponent
            // interface. The MapHandler can have a policy that
            // determines what to do when duplicate instances of the
            // same type of object are added - replace or ignore.
            // In this class, this will never happen, since we are
            // controlling that one MapBean, LayerHandler,
            // MouseDelegator, etc is being added to the MapHandler.
            GlobalExceptionHandler.handle(msmce);
        }
    }

    public void updateShapeLayerProps() {
        ShapeLayer[] layers = getShapeLayers();
        for(int i = 0; i < layers.length; i++) {
            Properties p = new Properties();
            layers[i].getProperties(p);
            int threshold = Integer.parseInt((String)p.get("lineWidthThreshold"));
            if(threshold >= 0) {
                int overviewWidth = Integer.parseInt((String)p.get("overviewLineWidth"));
                if(overviewWidth <= 0) {
                    overviewWidth = 1;
                }
                if(mapBean.getScale() < threshold) {
                    p.setProperty("lineWidth", "" + overviewWidth + 1);
                } else {
                    p.setProperty("lineWidth", "" + overviewWidth);
                }
            }
            layers[i].setProperties(p);
        }
    }

    public void setStationLayer(StationLayer staLayer) {
        stl = staLayer;
        staLayer.addLayerStatusListener(this);
        mapHandler.add(stl);
        lh.addLayer(stl, 0);
        el.addEQSelectionListener(stl);
        if(el instanceof EventTableLayer) {
            ((EventTableLayer)el).getTableModel().addEventDataListener(stl);
        }
    }

    public StationLayer getStationLayer() {
        return stl;
    }

    public void setEventLayer(EventLayer evl) {
        el = evl;
        el.addLayerStatusListener(this);
        mapHandler.add(el);
        lh.addLayer(el, 0);
        dl = new DistanceLayer(mapBean);
        dl.addLayerStatusListener(this);
        el.addEQSelectionListener(dl);
        mapHandler.add(dl);
        lh.addLayer(dl, 1);
        if(el instanceof EventTableLayer) {
            ((EventTableLayer)el).getTableModel().addEventDataListener(dl);
        }
    }

    public EventLayer getEventLayer() {
        return el;
    }

    public void setEtopoLayer(ETOPOLayer topoLayer) {
        topol = topoLayer;
        topol.addLayerStatusListener(this);
        mapHandler.add(topol);
        lh.addLayer(topol, lh.getLayers().length);
        for(int i = 0; i < shapeLayers.size(); i++) {
            Layer cur = (FissuresShapeLayer)shapeLayers.get(i);
            Properties props = cur.getProperties(null);
            props.remove("fillColor");
            cur.setProperties(props);
            cur.repaint();
        }
    }

    public void setEtopoLayer(String etopoDir) {
        setEtopoLayer(etopoDir, null);
    }

    public void setEtopoLayer(String etopoDir, String colorMapFilename) {
        setEtopoLayer(etopoDir, colorMapFilename, 15);
    }

    public void setEtopoLayer(String etopoDir,
                              String colorMapFilename,
                              int minuteSpacing) {
        ETOPOLayer topoLayer = null;
        try {
            if(colorMapFilename == null) {
                topoLayer = new ColorMapEtopoLayer();
            } else {
                topoLayer = new ColorMapEtopoLayer(colorMapFilename);
            }
        } catch(Exception e) {
            GlobalExceptionHandler.handle("problem loading color map for etopo layer",
                                          e);
        }
        Properties etopoProps = new Properties();
        etopoProps.put("path", etopoDir);
        etopoProps.put("prettyName", "ETOPO Layer");
        etopoProps.put("number.colors", "216");
        etopoProps.put("opaque", "255");
        etopoProps.put("view.type", "1");
        etopoProps.put("minute.spacing", Integer.toString(minuteSpacing));
        etopoProps.put("contrast", "5");
        topoLayer.setProperties(etopoProps);
        topoLayer.setVisible(true);
        setEtopoLayer(topoLayer);
    }

    public ETOPOLayer getETOPOLayer() {
        return topol;
    }

    public FissuresShapeLayer getGlobalShapeLayer() {
        return (FissuresShapeLayer)shapeLayers.get(0);
    }

    public ShapeLayer[] getShapeLayers() {
        return (ShapeLayer[])shapeLayers.toArray(new ShapeLayer[] {});
    }

    public ShapeLayer getShapeLayer(String prettyName) {
        ShapeLayer[] layers = getShapeLayers();
        for(int i = 0; i < layers.length; i++) {
            if(layers[i].PrettyNameProperty.equals(prettyName)) { return layers[i]; }
        }
        return null;
    }

    public DistanceLayer getDistanceLayer() {
        return dl;
    }

    public Layer[] getLayers() {
        return lh.getLayers();
    }

    public MapBean getMapBean() {
        if(mapBean == null) {
            mapBean = new BufferedMapBean();
        }
        return mapBean;
    }

    public LatLonPoint getOriginalCenter() {
        return originalCenter;
    }

    public void setOriginalCenter(LatLonPoint llp) {
        originalCenter = llp;
    }

    public void setOriginalScale(float scale) {
        originalScale = scale;
    }

    public float getOriginalScale() {
        return originalScale;
    }

    public void setZoom(float zoomFactor) {
        mapBean.zoom(new ZoomEvent(this, ZoomEvent.ABSOLUTE, zoomFactor));
    }

    public void addMouseMode(MapMouseMode mode) {
        mouseDelegator.addMouseMode(mode);
        if(mode instanceof ZoomTool) {
            ((ZoomTool)mode).addZoomListener(getMapBean());
        }
    }

    public void setActiveMouseMode(MapMouseMode mode) {
        mouseDelegator.setActiveMouseMode(mode);
    }

    public void updateLayerStatus(LayerStatusEvent event) {
        layerStatusMap.put(event.getLayer(), new Integer(event.getStatus()));
    }

    //look at LayerStatusEvent in openmap for status translation
    public int getLayerStatus(Layer layer) {
        Integer statusObj = (Integer)layerStatusMap.get(shapeLayers.get(0));
        return statusObj.intValue();
    }

    public void overrideProjChangedInOMLayers(boolean override) {
        Layer[] layers = getLayers();
        for(int i = 0; i < layers.length; i++) {
            Layer cur = layers[i];
            if(cur instanceof OverriddenOMLayer) {
                ((OverriddenOMLayer)cur).setOverrideProjectionChanged(override);
            }
            //this keeps the shapelayer happy, since it doesn't like to repaint
            //sometimes
            if(cur instanceof ShapeLayer) {
                ((ShapeLayer)cur).projectionChanged(new ProjectionEvent(this,
                                                                        null));
            }
        }
    }

    public void projectionChanged(ProjectionEvent e) {
        updateShapeLayerProps();
    }

    public void writeMapToJPEG(String filename) throws IOException {
        File loc = new File(filename);
        File parent = loc.getParentFile();
        parent.mkdirs();
        File temp = File.createTempFile(loc.getName(), null, parent);
        SunJPEGFormatter formatter = new SunJPEGFormatter();
        formatter.setImageQuality(0.8f);
        byte[] imgBytes = formatter.getImageFromMapBean(mapBean);
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(temp));
        bos.write(imgBytes);
        bos.flush();
        bos.close();
        loc.delete();
        temp.renameTo(loc);
    }
    
    public void writeMapToPNG(String filename) throws IOException{
        writeMapToPNG(new File(filename));
    }
    
    public void writeMapToPNG(File loc) throws IOException {
        File parent = loc.getAbsoluteFile().getParentFile();
        parent.mkdirs();
        File temp = File.createTempFile(loc.getName(), null, parent);
        writeMapToPNG(new FileOutputStream(temp));
        loc.delete();
        temp.renameTo(loc);
        logger.debug(ExceptionReporterUtils.getMemoryUsage()
                + " after write image to file");
    }
    
    public void writeMapToPNG(OutputStream out) throws IOException {
        synchronized(OpenMap.class) {
            try {
                getGlobalShapeLayer().setOverrideProjectionChanged(true);
                Projection proj = mapBean.getProjection();
                int w = proj.getWidth(), h = proj.getHeight();
                logger.debug(ExceptionReporterUtils.getMemoryUsage()
                        + " before make Buf Image");
                BufferedImage bufImg = new BufferedImage(w,
                                                         h,
                                                         BufferedImage.TYPE_INT_RGB);
                Graphics g = bufImg.getGraphics();
                g.setColor(WATER);
                g.fillRect(0, 0, w, h);
                Layer[] layers = getLayers();
                for(int i = layers.length - 1; i >= 0; i--) {
                    logger.debug("rendering " + layers[i].getName());
                    layers[i].renderDataForProjection(proj, g);
                }
                ImageIO.write(bufImg, "png", out);
            } finally {
                getGlobalShapeLayer().setOverrideProjectionChanged(false);
            }
        }
    }

    public float getWidthDegrees() {
        Projection proj = getMapBean().getProjection();
        float distL2C = ProjMath.lonDistance(proj.getUpperLeft().radlon_,
                                             proj.getCenter().radlon_);
        float distC2R = ProjMath.lonDistance(proj.getCenter().radlon_,
                                             proj.getLowerRight().radlon_);
        return Length.DECIMAL_DEGREE.fromRadians(Math.abs(distL2C + distC2R));
    }

    public static String translateLayerStatus(int status) {
        switch(status){
            case LayerStatusEvent.DISTRESS:
                return "DISTRESS!!!";
            case LayerStatusEvent.FINISH_WORKING:
                return "finished";
            case LayerStatusEvent.START_WORKING:
                return "started";
            case LayerStatusEvent.STATUS_UPDATE:
                return "updating status";
        }
        return null;
    }

    public static void main(final String[] args) {
        BasicConfigurator.configure();
        final OpenMap om = new OpenMap();
        om.setActiveMouseMode(new PanTool(om));
        //        System.out.println("before if statement");
        //        if(args.length > 2) {
        //            System.out.println("if!");
        //            om.setEtopoLayer("edu/sc/seis/mapData", args[2]);
        //        } else {
        //            System.out.println("else!");
        om.setEtopoLayer("edu/sc/seis/mapData");
        //}
        //        om.getEventLayer()
        //                .eventDataChanged(new
        // EQDataEvent(MockEventAccessOperations.createEvents()));
        //        StationLayer staLayer = om.getStationLayer();
        //        Station sta = MockStation.createStation();
        //        Station other = MockStation.createOtherStation();
        //        staLayer.stationDataChanged(new StationDataEvent(new Station[]
        // {sta}));
        //        staLayer.stationAvailabiltyChanged(new AvailableStationDataEvent(sta,
        //                                                                         AvailableStationDataEvent.UP));
        //        staLayer.stationDataChanged(new StationDataEvent(new Station[]
        // {other}));
        //        staLayer.stationAvailabiltyChanged(new
        // AvailableStationDataEvent(other,
        //                                                                         AvailableStationDataEvent.DOWN));
        //        staLayer.printStationLocs();
        JButton reloadColorTable = new JButton("Reload Table");
        final JFrame frame = new JFrame("OpenMap Test");
        reloadColorTable.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                ETOPOLayer etopo = om.getETOPOLayer();
                if(etopo instanceof ColorMapEtopoLayer) {
                    try {
                        ((ColorMapEtopoLayer)etopo).setColorTable(args[2]);
                        om.getMapBean()
                                .center(new CenterEvent(this,
                                                        (float)Math.random(),
                                                        (float)Math.random()));
                    } catch(FileNotFoundException e1) {
                        e1.printStackTrace();
                    } catch(IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(om);
        frame.getContentPane().add(reloadColorTable, BorderLayout.SOUTH);
        frame.setSize(640, 480);
        frame.show();
    }

    public void findAndInit(Object obj) {
        if(obj instanceof MapBean) {
            add((MapBean)obj, BorderLayout.CENTER);
        } else if(obj instanceof InformationDelegator) {
            InformationDelegator infoDel = (InformationDelegator)obj;
            add(infoDel, BorderLayout.SOUTH);
        }
    }
}