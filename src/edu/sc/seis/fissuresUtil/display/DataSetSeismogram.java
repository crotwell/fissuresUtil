package edu.sc.seis.fissuresUtil.display;

import edu.iris.Fissures.Location;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.TauP.SphericalCoords;
import edu.sc.seis.fissuresUtil.xml.DataSet;
import edu.sc.seis.fissuresUtil.xml.XMLDataSet;
import java.util.Date;

/**
 * DataSetSeismogram represents a single instance of a displayed seismogram.  
 * There are no two identical DataSetSeismograms.  Their names and colors are
 * all different.
 *
 *
 * Created: Mon Jul  8 11:45:41 2002
 *
 * @author <a href="mailto:">Charlie Groves</a>
 * @version
 */

public class DataSetSeismogram {
    public DataSetSeismogram(DataSetSeismogram dss, String name){
	this(dss.getSeismogram(), dss.getDataSet(), name);
    }
    
    public DataSetSeismogram (LocalSeismogramImpl seismo, DataSet ds){
	this(seismo, ds, seismo.getName());
    }

    public DataSetSeismogram(LocalSeismogramImpl seismo, DataSet ds, String name){
	if(seismo == null){
	    throw new IllegalArgumentException("the seismogram passed to create a DataSetSeismogram must not be null");
	}
	this.seis = seismo;
	this.dataSet = ds;
	this.name = name;
    }
    
    public LocalSeismogramImpl getSeismogram(){ return seis; }

    public DataSet getDataSet(){ return dataSet; }

    public String toString(){ return name; }

    public boolean isFurtherThan(DataSetSeismogram seismo){
	
	try{
	    Date beginFurther = new Date();
	    Location eventLoc = ((XMLDataSet)dataSet).getEvent().get_preferred_origin().my_location;
	    Location seisLoc = ((XMLDataSet)dataSet).getChannel(seis.channel_id).my_site.my_location;
	    Location seismoLoc = ((XMLDataSet)seismo.getDataSet()).getChannel(seismo.getSeismogram().channel_id).my_site.my_location;
	    Date endFurther = new Date();
	    long interval = endFurther.getTime() - beginFurther.getTime();
	    System.out.println("data aquisition: "+ interval + "ms");
	    Date beginDistance = new Date();
	    if(SphericalCoords.distance(eventLoc.latitude, eventLoc.longitude, seisLoc.latitude, seisLoc.longitude) <
	       SphericalCoords.distance(eventLoc.latitude, eventLoc.longitude, seismoLoc.latitude, seismoLoc.longitude)){
		Date endDistance = new Date();
		interval = endDistance.getTime() - beginDistance.getTime();
		System.out.println("distance calc:" + interval + "ms");
		return true;
	    }
	    Date endDistance = new Date();
	    interval = endDistance.getTime() - beginDistance.getTime();
	    System.out.println("distance calc:" + interval + "ms");
	    return false;
	}catch(Exception e){ e.printStackTrace(); }
	return true;
    }

    private final DataSet dataSet;

    private final LocalSeismogramImpl seis;

    private final String name;
}// DataSetSeismogram
