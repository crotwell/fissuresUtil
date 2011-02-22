package edu.sc.seis.fissuresUtil.comparator;

import java.util.Comparator;

import edu.sc.seis.seisFile.mseed.Btime;
import edu.sc.seis.seisFile.mseed.DataRecord;


public class DataRecordBeginComparator implements Comparator<DataRecord> {

    public int compare(DataRecord o1, DataRecord o2) {
        Btime b1 = o1.getHeader().getStartBtime();
        Btime b2 = o2.getHeader().getStartBtime();
        return compare(b1, b2);
    }

    public int compare(Btime b1, Btime b2) {
        int result = compare(b1.year, b2.year);
        if( result == 0) {
            result = compare(b1.jday, b2.jday);
            if( result == 0) {
                result = compare(b1.hour, b2.hour);
                if( result == 0) {
                    result = compare(b1.min, b2.min);
                    if( result == 0) {
                        result = compare(b1.sec, b2.sec);
                        if( result == 0) {
                            return compare(b1.tenthMilli, b2.tenthMilli);
                        } else {
                            return result;
                        }
                    } else {
                        return result;
                    }
                } else {
                    return result;
                }
            } else {
                return result;
            }
        } else {
            return result;
        }
    }
    
    public static int compare(int i1, int i2) {
        if(i1 == i2) {
            return 0;
        } else {
            return i1 < i2 ? -1 : 1;
        }
    }
}
