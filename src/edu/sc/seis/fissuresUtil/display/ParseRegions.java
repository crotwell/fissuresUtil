package edu.sc.seis.fissuresUtil.display;


import java.io.*;
import java.util.*;
import java.lang.*;
/**
 * ParseRegions.java
 *
 *
 * Created: Fri Sep 28 12:35:36 2001
 *
 * @author <a href="mailto: "Srinivasa Telukutla</a>
 * @version
 */

public class ParseRegions {
    public ParseRegions (){
	load();
    }
    
    /** Gets the Geographic region number for a name. Returns 0 if the
	name cannot be found. */
    public int getRegionValue(String region) {
	String geoNum = feProps.getProperty(region.replace(' ','_'));
	if (geoNum != null) {
	    return Integer.parseInt(geoNum);
	} // end of if (feProps.get(region.replace(' ','_')))
	return 0;
    }

    public String getGeographicRegionName(int geoNum) {
	if (geoNum > 0 && geoNum <= 729) {
	    return feProps.getProperty("GeogRegion"+geoNum);
	} // end of if (geoNum > 0 && geoNum <= 729)
	return "Unknown";
    }

    public String getSeismicRegionName(int seisNum) {
	if (seisNum > 0 && seisNum <= 50) {
	    return feProps.getProperty("SeismicRegion"+seisNum);
	} // end of if (seisNum > 0 && seisNum <= 50)
	return "Unknown";
    }

    protected void load() {
	try {
	    ClassLoader loader = getClass().getClassLoader();
	    InputStream fstream = 
		loader.getResourceAsStream("edu/sc/seis/fissuresUtil/display/FERegions.prop");
	    feProps = new Properties();
	    feProps.load(fstream);
	} catch (IOException e) {
	    System.err.println("Cannot load FE regions");
	    e.printStackTrace();
	} // end of catch
    }	
	
    protected Properties feProps;

}// parseRegions
