package edu.sc.seis.fissuresUtil.rt130;

import java.io.IOException;
import edu.iris.Fissures.TimeRange;

public class RT130SamplingFinder implements SamplingFinder {

    public RT130SamplingFinder(RT130FileReader rtFileReader) {
        this.rtFileReader = rtFileReader;
    }

    public int find(String file, TimeRange fileTimeWindow)
            throws RT130FormatException, IOException {
        return rtFileReader.processRT130Data(file, false, fileTimeWindow)[0].sample_rate;
    }

    private RT130FileReader rtFileReader;
}
