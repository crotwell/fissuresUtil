package edu.sc.seis.fissuresUtil.dataset;

import edu.sc.seis.fissuresUtil.xml.DataSet;

public class DataSetChangeEvent extends java.util.EventObject {

    public DataSetChangeEvent(Object source, DataSet dataset) {
	super(source);
	this.dataset = dataset;
    }

    protected DataSet dataset;

    public DataSet getDataSet() {
	return dataset;
    }

}

