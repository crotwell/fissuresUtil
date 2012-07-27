package edu.sc.seis.fissuresUtil.dataset;


public interface DataSetChangeListener extends java.util.EventListener {

    public void datasetChanged(DataSetChangeEvent e);

    public void datasetAdded(DataSetChangeEvent e);

    public void datasetRemoved(DataSetChangeEvent e);

}
