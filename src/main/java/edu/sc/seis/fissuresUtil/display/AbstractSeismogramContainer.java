package edu.sc.seis.fissuresUtil.display;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;
import edu.sc.seis.fissuresUtil.xml.SeisDataChangeEvent;
import edu.sc.seis.fissuresUtil.xml.SeisDataErrorEvent;

/**
 * @author groves Created on Mar 28, 2005
 */
public abstract class AbstractSeismogramContainer implements
        SeismogramContainer {

    public AbstractSeismogramContainer(
            SeismogramContainerListener initialListener, DataSetSeismogram dss) {
        j = i++;
        seismogram = dss;
        if(initialListener != null) {
            listeners.add(initialListener);
        }
        seismogram.addSeisDataChangeListener(this);
        seismogram.addRequestFilterChangeListener(this);
    }

    public void addListener(SeismogramContainerListener listener) {
        synchronized(listeners) {
            listeners.add(listener);
        }
    }

    public void removeListener(SeismogramContainerListener listener) {
        synchronized(listeners) {
            listeners.remove(listener);
        }
    }

    public DataSetSeismogram getDataSetSeismogram() {
        return seismogram;
    }

    public void pushData(SeisDataChangeEvent sdce) {
        addSeismograms(sdce.getSeismograms());
    }

    public void error(SeisDataErrorEvent sdce) {
        GlobalExceptionHandler.handle("Trouble getting data for "
                + sdce.getSource(), sdce.getCausalException());
        error = true;
    }

    public void finished(SeisDataChangeEvent sdce) {
        finished = true;
        addSeismograms(sdce.getSeismograms());
    }

    public void beginTimeChanged() {
        reset();
    }

    public void endTimeChanged() {
        reset();
    }

    protected synchronized void reset() {
        getDataSetSeismogram().retrieveData(this);
        finished = false;
        noData = true;
    }

    public String getDataStatus() {
        if(error) {
            return ERROR;
        } else if(noData && finished) {
            return NO_DATA;
        } else if(noData) {
            return GETTING_DATA;
        } else {
            return HAVE_DATA;
        }
    }

    /*
     * Implementations must add the given seismograms to their internal
     * representations. Once some acceptable data comes through, they must set
     * noData to false
     */
    protected abstract void addSeismograms(LocalSeismogramImpl[] seismograms);

    public String toString() {
        return getDataSetSeismogram().getName() + j + " Container";
    }

    private static int i = 0;

    private int j;

    private DataSetSeismogram seismogram;

    private boolean finished = false;

    protected boolean noData = true;

    private boolean error = false;

    protected List listeners = Collections.synchronizedList(new ArrayList());
}