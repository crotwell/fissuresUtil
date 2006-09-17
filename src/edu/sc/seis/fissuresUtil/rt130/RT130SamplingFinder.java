package edu.sc.seis.fissuresUtil.rt130;

import java.io.IOException;

public class RT130SamplingFinder implements SamplingFinder {

    public RT130SamplingFinder(RT130FileReader rtFileReader) {
        this.rtFileReader = rtFileReader;
    }

    public int find(String file) throws RT130FormatException, IOException {
        return rtFileReader.processRT130Data(file, false)[0].sample_rate;
    }

    private RT130FileReader rtFileReader;
}
