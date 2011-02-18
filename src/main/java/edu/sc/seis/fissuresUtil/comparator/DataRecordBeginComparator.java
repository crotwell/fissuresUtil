package edu.sc.seis.fissuresUtil.comparator;

import java.util.Comparator;

import edu.sc.seis.seisFile.mseed.DataRecord;


public class DataRecordBeginComparator implements Comparator<DataRecord> {

    public int compare(DataRecord o1, DataRecord o2) {
        return o1.getHeader().getStartTime().compareTo(o2.getHeader().getStartTime());
    }
}
