package edu.sc.seis.fissuresUtil.map.projections;

import java.awt.Point;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import com.bbn.openmap.LatLonPoint;
import com.bbn.openmap.MapBean;
import com.bbn.openmap.proj.Mercator;
import com.bbn.openmap.proj.Projection;

/**
 * @author groves Created on Nov 25, 2004
 */
public class ProjectionHandler {

    public ProjectionHandler(String view) {
        LatLonPoint center = new LatLonPoint(MapBean.DEFAULT_CENTER_LAT,
                                             MapBean.DEFAULT_CENTER_LON);
        float scale = Float.MAX_VALUE;
        proj = new Mercator(center, scale, 800, 550);
        if(!view.equals("World")) {
            LatLonPoint upperLeft = getLatLonProp(view, "upperLeft");
            LatLonPoint lowerRight = getLatLonProp(view, "lowerRight");
            Point upperLeftPix = new Point(0, 0);
            Point lowerRightPix = new Point(800, 550);
            scale = proj.getScale(upperLeft,
                                  lowerRight,
                                  upperLeftPix,
                                  lowerRightPix);
            proj = new Mercator(getLatLonProp(view, "center"), scale, 800, 550);
        } //else its the default world view
        views.put(view, this);
    }

    public static ProjectionHandler get(String viewName) {
        if(!views.containsKey(viewName)) {
            views.put(viewName, new ProjectionHandler(viewName));
        }
        return (ProjectionHandler)views.get(viewName);
    }

    public float[] inverse(int x, int y) {
        LatLonPoint llp = proj.inverse(x, y);
        return new float[] {llp.getLatitude(), llp.getLongitude()};
    }

    public static LatLonPoint getLatLonProp(String view, String type) {
        String viewType = view + '.' + type;
        double lat = Double.parseDouble(viewProps.getProperty(viewType + "Lat"));
        double lon = Double.parseDouble(viewProps.getProperty(viewType + "Lon"));
        return new LatLonPoint(lat, lon);
    }

    public Projection getProjeciton() {
        return proj;
    }

    private Projection proj;

    private static Map views = Collections.synchronizedMap(new HashMap());

    private static Properties viewProps;

    private static final String VIEW_PROP_LOC = "edu/sc/seis/fissuresUtil/map/projections/view.props";
    static {
        viewProps = new Properties();
        ClassLoader cl = ProjectionHandler.class.getClassLoader();
        try {
            InputStream in = cl.getResourceAsStream(VIEW_PROP_LOC);
            if (in == null) {
                throw new IOException("Can't find "+VIEW_PROP_LOC+" as resource");
            }
            viewProps.load(in);
        } catch(IOException e) {
            throw new RuntimeException("Unable to find " + VIEW_PROP_LOC
                    + " in jar", e);
        }
    }
}