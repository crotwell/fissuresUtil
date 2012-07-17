package edu.sc.seis.fissuresUtil.display;

import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.time.MicroSecondTimeRange;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;
import edu.sc.seis.fissuresUtil.xml.RequestFilterChangeListener;
import edu.sc.seis.fissuresUtil.xml.SeisDataChangeListener;

/**
 * <code>SeismogramContainer</code> Takes a DataSetSeismogram and requests its
 * data. It holds whatever it gets in soft references so that they can be
 * garbage collected if need be. If it gets a request for data, and some of the
 * items it has once held have been garbage collected, it will reerequest them.
 */
public interface SeismogramContainer extends SeisDataChangeListener,
        RequestFilterChangeListener {

    /**
     * @return - a SeismogramIterator over the full time range of the seismogram
     */
    public SeismogramIterator getIterator();

    public SeismogramIterator getIterator(MicroSecondTimeRange timeRange);

    public LocalSeismogramImpl[] getSeismograms();

    public void addListener(SeismogramContainerListener listener);

    public void removeListener(SeismogramContainerListener listener);

    public String getDataStatus();

    public DataSetSeismogram getDataSetSeismogram();

    public static final String NO_DATA = "No data available";

    public static final String GETTING_DATA = "Trying to get data";

    public static final String HAVE_DATA = "";

    public static final String ERROR = "Error encountered getting data";
}