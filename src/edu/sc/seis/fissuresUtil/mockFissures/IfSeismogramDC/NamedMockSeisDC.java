package edu.sc.seis.fissuresUtil.mockFissures.IfSeismogramDC;

import edu.iris.Fissures.IfSeismogramDC.DataCenter;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;

public class NamedMockSeisDC {

    public static DataCenter create(String name) {
        if(name.equals("AvailAvailAgain")) {
            return new AvailAvailAgain();
        } else if(name.equals("Simple")) {
            return new MockDC();
        }
        throw new RuntimeException("No mock dc by the name of " + name
                + " known");
    }

    private static class AvailAvailAgain extends MockDC {

        // Alternates between no available data then all data available per
        // request
        public RequestFilter[] available_data(RequestFilter[] a_filterseq) {
            flipOnRequest = !flipOnRequest;
            if(flipOnRequest) {
                return a_filterseq;
            }
            return new RequestFilter[0];
        }

        boolean flipOnRequest = true;
    }
}
