package edu.sc.seis.fissuresUtil.comparator;

import java.util.Comparator;

import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;


public class SeisBeginTimeComparator implements Comparator<LocalSeismogramImpl> {

    public int compare(LocalSeismogramImpl o1, LocalSeismogramImpl o2) {
        return msdCompare.compare(o1.getBeginTime(), o2.getBeginTime());
    }
    
    MicroSecondDateComparator msdCompare = new MicroSecondDateComparator();
}
