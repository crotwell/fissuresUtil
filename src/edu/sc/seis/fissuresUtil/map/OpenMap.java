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
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import com.bbn.openmap.BufferedMapBean;
import com.bbn.openmap.InformationDelegator;
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
import com.bbn.openmap.event.ZoomEvent;
import com.bbn.openmap.gui.OMComponentPanel;
import com.bbn.openmap.image.SunJPEGFormatter;
import com.bbn.openmap.layer.GraticuleLayer;
import com.bbn.openmap.layer.etopo.ETOPOLayer;
import com.bbn.openmap.layer.shape.ShapeLayer;
import com.bbn.openmap.proj.Projection;
import edu.iris.Fissures.IfNetwork.Station;
import edu.sc.seis.fissuresUtil.chooser.AvailableStationDataEvent;
import edu.sc.seis.fissuresUtil.chooser.StationDataEvent;
import edu.sc.seis.fissuresUtil.display.EQDataEvent;
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
import edu.sc.seis.fissuresUtil.map.layers.StationLayer;
import edu.sc.seis.fissuresUtil.map.tools.PanTool;
import edu.sc.seis.fissuresUtil.map.tools.ZoomTool;
import edu.sc.seis.fissuresUtil.mockFissures.IfEvent.MockEventAccessOperations;
import edu.sc.seis.fissuresUtil.mockFissures.IfNetwork.MockStation;

public class OpenMap extends OMComponentPanel implements LayerStatusListener {

    public static final Color WATER = new Color(54, 179, 221);

    public static final float DEFAULT_SCALE = 200000000f;

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

    private ShapeLayer shapeLayer;

    /**
     * Creates a map with a shapelayer based on the file in fissuresUtil, a
     * graticule layer, an empty event layer with depth based colorizationand an
     * empty station layer
     */
    public OpenMap(){
        this(true);
    }
    
    public OpenMap(boolean graticule) {
        this("edu/sc/seis/fissuresUtil/data/maps/dcwpo-browse", graticule);
        setEventLayer(new EventLayer(getMapBean(), new DepthEventColorizer()));
        setStationLayer(new StationLayer());
    }

    /**
     * Create a map with a shape layer and a graticule
     * 
     * @param shapefile -
     *            the file to be used in the shapelayer
     */
    public OpenMap(String shapefile){
        this(shapefile, true);
    }
    
    public OpenMap(String shapefile, boolean graticule) {
        try {
            setLayout(new BorderLayout());
            mapHandler = new MapHandler();
            mapHandler.add(this);
            mapBean = getMapBean();
            mapBean.setBackgroundColor(WATER);
            mapHandler.add(mapBean);
            // Create and add a LayerHandler to the MapHandler. The
            // LayerHandler manages Layers, whether they are part of the
            // map or not. layer.setVisible(true) will add it to the map.
            // The LayerHandler has methods to do this, too. The
            // LayerHandler will find the MapBean in the MapHandler.
            lh = new LayerHandler();
            mapHandler.add(lh);
            if(graticule) {
                GraticuleLayer gl = new FissuresGraticuleLayer();
                Properties graticuleLayerProps = gl.getProperties(new Properties());
                graticuleLayerProps.setProperty("prettyName", "Graticule Layer");
                graticuleLayerProps.setProperty("show1And5Lines", "true");
                graticuleLayerProps.setProperty("10DegreeColor", "FF888888");
                graticuleLayerProps.setProperty("1DegreeColor", "C7003300");
                gl.setProperties(graticuleLayerProps);
                gl.setShowRuler(true);
                mapHandler.add(gl);
                lh.addLayer(gl, 0);
            }
            // Create a ShapeLayer to show world political boundaries.
            shapeLayer = new FissuresShapeLayer();
            shapeLayer.addLayerStatusListener(this);
            //Create shape layer properties
            Properties shapeLayerProps = new Properties();
            shapeLayerProps.put("prettyName", "Political Boundaries Layer");
            shapeLayerProps.put("lineColor", "FF000000");
            shapeLayerProps.put("fillColor", "FF39DA87");
            shapeLayerProps.put("shapeFile", shapefile + ".shp");
            shapeLayerProps.put("spatialIndex", shapefile + ".ssx");
            shapeLayer.setProperties(shapeLayerProps);
            shapeLayer.setVisible(true);
            mapHandler.add(shapeLayer);
            lh.addLayer(shapeLayer);
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
        lh.addLayer(topol);
        Properties props = shapeLayer.getProperties(null);
        props.remove("fillColor");
        shapeLayer.setProperties(props);
        shapeLayer.repaint();
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
        etopoProps.put("prettyName", "World Terrain Elevation / Ocean Depth");
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

    public ShapeLayer getShapeLayer() {
        return shapeLayer;
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
        logger.debug("Layer " + event.getLayer().getName() + " status: "
                + translateLayerStatus(event.getStatus()));
    }

    //look at LayerStatusEvent in openmap for status translation
    public int getLayerStatus(Layer layer) {
        Integer statusObj = (Integer)layerStatusMap.get(shapeLayer);
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

    public void writeMapToPNG(String filename) throws IOException {
        synchronized(OpenMap.class) {
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
            File loc = new File(filename);
            File parent = loc.getAbsoluteFile().getParentFile();
            parent.mkdirs();
            File temp = File.createTempFile(loc.getName(), null, parent);
            ImageIO.write(bufImg, "png", temp);
            loc.delete();
            temp.renameTo(loc);
            logger.debug(ExceptionReporterUtils.getMemoryUsage()
                    + " after write image to file");
        }
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
        if(args.length > 2) {
            om.setEtopoLayer("edu/sc/seis/mapData", args[2]);
        } else {
            om.setEtopoLayer("edu/sc/seis/mapData");
        }
        om.getEventLayer()
                .eventDataChanged(new EQDataEvent(MockEventAccessOperations.createEvents()));
        StationLayer staLayer = om.getStationLayer();
        Station sta = MockStation.createStation();
        Station other = MockStation.createOtherStation();
        staLayer.stationDataChanged(new StationDataEvent(new Station[] {sta}));
        staLayer.stationAvailabiltyChanged(new AvailableStationDataEvent(sta,
                                                                         AvailableStationDataEvent.UP));
        staLayer.stationDataChanged(new StationDataEvent(new Station[] {other}));
        staLayer.stationAvailabiltyChanged(new AvailableStationDataEvent(other,
                                                                         AvailableStationDataEvent.DOWN));
        staLayer.printStationLocs();
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