package edu.sc.seis.fissuresUtil.display;

import edu.sc.seis.fissuresUtil.xml.*;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.TauP.SphericalCoords;
import edu.iris.Fissures.Location;
import java.util.Date;

/**
 * DataSetSeismogram.java
 *
 *
 * Created: Mon Jul  8 11:45:41 2002
 *
 * @author <a href="mailto:">Charlie Groves</a>
 * @version
 */

public class DataSetSeismogram {
    public DataSetSeismogram (LocalSeismogramImpl seismo, DataSet ds){
	this.seis = seismo;
	this.dataSet = ds;
    }
    
    public LocalSeismogramImpl getSeismogram(){ return seis; }

    public DataSet getDataSet(){ return dataSet; }

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

    protected DataSet dataSet;

    protected LocalSeismogramImpl seis;
}// DataSetSeismogram
