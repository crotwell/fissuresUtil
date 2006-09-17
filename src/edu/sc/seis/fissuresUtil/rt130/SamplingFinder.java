package edu.sc.seis.fissuresUtil.rt130;

import java.io.IOException;

public interface SamplingFinder {

    public int find(String file) throws RT130FormatException, IOException;
}
