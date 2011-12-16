package edu.sc.seis.fissuresUtil.display;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.time.RangeTool;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;

/**
 * @author groves Created on Mar 28, 2005
 */
public class HardRefSeismogramContainer extends AbstractSeismogramContainer {

    public HardRefSeismogramContainer(DataSetSeismogram seis) {
        this(null, seis);
    }

    public HardRefSeismogramContainer(SeismogramContainerListener initial,
            DataSetSeismogram seis) {
        super(initial, seis);
        reset();
    }

    protected synchronized void addSeismograms(LocalSeismogramImpl[] seismograms) {
        for(int i = 0; i < seismograms.length; i++) {
            Iterator it = seis.iterator();
            boolean found = false;
            while(it.hasNext() && !found) {
                LocalSeismogramImpl cur = (LocalSeismogramImpl)it.next();
                if(new MicroSecondTimeRange(cur).equals(new MicroSecondTimeRange(seismograms[i]))) {
                    found = true;
                }
            }
            if(!found) {
                System.out.println(this + " got data  "
                        + seismograms[i].getBeginTime() + " to "
                        + seismograms[i].getEndTime());
                seis.add(seismograms[i]);
            }
        }
        noData = false;
        Iterator it = listeners.iterator();
        while(it.hasNext()) {
            ((SeismogramContainerListener)it.next()).updateData();
        }
    }

    public synchronized SeismogramIterator getIterator() {
        return getIterator(RangeTool.getFullTime(getSeismograms()));
    }

    public synchronized SeismogramIterator getIterator(MicroSecondTimeRange timeRange) {
        return new SeismogramIterator(getDataSetSeismogram().getName(),
                                      getSeismograms(),
                                      timeRange);
    }

    public synchronized LocalSeismogramImpl[] getSeismograms() {
        return (LocalSeismogramImpl[])seis.toArray(new LocalSeismogramImpl[0]);
    }

    protected synchronized void reset() {
        seis.clear();
        super.reset();
    }

    private List seis = new ArrayList();
}