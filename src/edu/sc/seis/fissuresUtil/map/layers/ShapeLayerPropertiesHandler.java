package edu.sc.seis.fissuresUtil.map.layers;

import java.util.Properties;

/**
 * @author oliverpa Created on Dec 6, 2004
 */
public class ShapeLayerPropertiesHandler {

    /**
     * @return Properties with commonly-used default values.
     */
    public static Properties getProperties() {
        return getProperties("Global Shape Layer",
                             "edu/sc/seis/fissuresUtil/data/maps/dcwpo-browse");
    }

    /**
     * @param name
     *            A pretty name for the shape layer (required)
     * @param shapefile
     *            The path of the shapefile without the .shp file extension
     *            (required)
     * @return Properties with these specified parameters and default fillColor,
     *         lineWidth, lineWidthThreshold, and lineColor.
     */
    public static Properties getProperties(String name, String shapefile) {
        return getProperties(name, shapefile, "FF39DA87");
    }

    /**
     * @param name
     *            A pretty name for the shape layer (required)
     * @param shapefile
     *            The path of the shapefile without the .shp file extension
     *            (required)
     * @param fillColor
     *            The area color. Can be null if you only want an outline.
     * @return Properties with these specified parameters and default lineWidth,
     *         lineWidthThreshold, and lineColor.
     */
    public static Properties getProperties(String name,
                                           String shapefile,
                                           String fillColor) {
        return getProperties(name, shapefile, fillColor, 1, 5500000);
    }

    /**
     * @param name
     *            A pretty name for the shape layer (required)
     * @param shapefile
     *            The path of the shapefile without the .shp file extension
     *            (required)
     * @param fillColor
     *            The area color. Can be null if you only want an outline.
     * @param overviewLineWidth
     *            This is the width of the lines as long as the scale is greater
     *            than the lineWidthThreshold. Set to -1 if you want openmap
     *            default value.
     * @param lineWidthThreshold
     *            If map scale goes below this value, then the line with is
     *            increased for easier viewing. Disable this feature by passing
     *            -1.
     * @return Properties with these specified parameters and OpenMap default
     *         line coloring.
     */
    public static Properties getProperties(String name,
                                           String shapefile,
                                           String fillColor,
                                           int overviewLineWidth,
                                           int lineWidthThreshold) {
        return getProperties(name,
                             shapefile,
                             fillColor,
                             overviewLineWidth,
                             lineWidthThreshold,
                             null);
    }

    /**
     * @param name
     *            A pretty name for the shape layer (required)
     * @param shapefile
     *            The path of the shapefile without the .shp file extension
     *            (required)
     * @param fillColor
     *            The area color. Can be null if you only want an outline.
     * @param overviewLineWidth
     *            This is the width of the lines as long as the scale is greater
     *            than the lineWidthThreshold. Set to -1 if you want openmap
     *            default value.
     * @param lineWidthThreshold
     *            If map scale goes below this value, then the line with is
     *            increased for easier viewing. Disable this feature by passing
     *            -1.
     * @param lineColor
     *            The line color. If null, then it defaults to black.
     * @return Properties with all specified parameters.
     */
    public static Properties getProperties(String name,
                                           String shapefile,
                                           String fillColor,
                                           int overviewLineWidth,
                                           int lineWidthThreshold,
                                           String lineColor) {
        Properties properties = new Properties();
        properties.setProperty("prettyName", name);
        properties.setProperty("shapeFile", shapefile + ".shp");
        properties.setProperty("spatialIndex", shapefile + ".ssx");
        if(fillColor != null) {
            properties.setProperty("fillColor", fillColor);
        }
        if(overviewLineWidth >= 0) {
            properties.setProperty("lineWidth", "" + overviewLineWidth);
        }
        properties.setProperty("overviewLineWidth", "" + overviewLineWidth);
        //}
        //if(lineWidthThreshold >= 0) {
        properties.setProperty("lineWidthThreshold", "" + lineWidthThreshold);
        //}
        if(lineColor != null) {
            properties.setProperty("lineColor", lineColor);
        }
        return properties;
    }
}