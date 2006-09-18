package edu.sc.seis.fissuresUtil.rt130;

import java.io.IOException;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;

public interface SamplingFinder {

    public int find(String file, MicroSecondTimeRange fileTimeWindow)
            throws RT130FormatException, IOException;
}
