package edu.sc.seis.fissuresUtil.display;

import edu.sc.seis.fissuresUtil.xml.*;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.TauP.SphericalCoords;
import edu.iris.Fissures.Location;

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
	    Location eventLoc = ((XMLDataSet)dataSet).getEvent().get_preferred_origin().my_location;
	    Location seisLoc = ((XMLDataSet)dataSet).getChannel(seis.channel_id).my_site.my_location;
	    Location seismoLoc = ((XMLDataSet)seismo.getDataSet()).getChannel(seismo.getSeismogram().channel_id).my_site.my_location;
	    if(SphericalCoords.distance(eventLoc.latitude, eventLoc.longitude, seisLoc.latitude, seisLoc.longitude) <
	       SphericalCoords.distance(eventLoc.latitude, eventLoc.longitude, seismoLoc.latitude, seismoLoc.longitude))
		return true;
	    return false;
	}catch(Exception e){}
	return true;
    }

    protected DataSet dataSet;

    protected LocalSeismogramImpl seis;
}// DataSetSeismogram
