package edu.sc.seis.fissuresUtil.display;

import edu.sc.seis.fissuresUtil.xml.*;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;

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

    public boolean isFurtherThan(DataSetSeismogram seis){
	return true;
    }

    protected DataSet dataSet;

    protected LocalSeismogramImpl seis;
}// DataSetSeismogram
