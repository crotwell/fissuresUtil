package edu.sc.seis.fissuresUtil.display;

import edu.sc.seis.fissuresUtil.xml.*;
import edu.iris.Fissures.IfSeismogramDC.LocalSeismogram;

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
    public DataSetSeismogram (LocalSeismogram seismo, DataSet ds){
	this.seis = seismo;
	this.dataSet = ds;
    }
    
    public LocalSeismogram getSeismogram(){ return seis; }

    public DataSet getDataSet(){ return dataSet; }

    protected DataSet dataSet;

    protected LocalSeismogram seis;
}// DataSetSeismogram
