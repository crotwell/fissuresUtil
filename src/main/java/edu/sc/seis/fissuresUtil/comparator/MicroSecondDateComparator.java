package edu.sc.seis.fissuresUtil.comparator;

import java.util.Comparator;

import edu.iris.Fissures.model.MicroSecondDate;


public class MicroSecondDateComparator implements Comparator<MicroSecondDate> {

    public int compare(MicroSecondDate o1, MicroSecondDate o2) {
        if (o1.equals(o2)) {return 0;}
        return o1.before(o2)? -1 : 1;
    }
}
