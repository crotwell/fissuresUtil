package edu.sc.seis.fissuresUtil.rt130;

import java.io.IOException;
import edu.iris.Fissures.TimeRange;

public interface SamplingFinder {

    public int find(String file, TimeRange fileTimeWindow)
            throws RT130FormatException, IOException;
}
