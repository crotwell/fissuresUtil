package edu.sc.seis.fissuresUtil.display;

import java.io.*;
import java.util.*;
import java.lang.*;
import edu.iris.Fissures.*;

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
	    geoNum = geoNum.substring("GeogRegion".length(), geoNum.length());
	    return Integer.parseInt(geoNum);
	} // end of if (feProps.get(region.replace(' ','_')))
	return 0;
    }

    public String getGeographicRegionName(int geoNum) {
        String propValue = 
	    feProps.getProperty("GeogRegion"+geoNum);
	if (propValue != null && propValue.length > 1) {
            return propValue;
	}
	return "GeoRegion"+geoNum;
    }

    public String getSeismicRegionName(int seisNum) {
        String propValue =
           feProps.getProperty("SeismicRegion"+seisNum);
	if (propValue != null && propValue.length > 1) {
            return propValue;
	}
	return "SeisRegion"+seisNum;
    }

    public String getRegionName(FlinnEngdahlRegion region){
	if (region.type.equals(FlinnEngdahlType.SEISMIC_REGION)) {
	    return getSeismicRegionName(region.number);
	}
	if (region.type.equals(FlinnEngdahlType.GEOGRAPHIC_REGION)) {
	    return getGeographicRegionName(region.number);
	}
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
