package edu.sc.seis.fissuresUtil.mockFissures.IfSeismogramDC;

import org.omg.CORBA.UNKNOWN;
import edu.iris.Fissures.IfSeismogramDC.DataCenter;
import edu.iris.Fissures.IfSeismogramDC.LocalSeismogram;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;

public class NamedMockSeisDC {
	
	public static final String SOMETIMES_AVAIL_UNKNOWN = "SometimesAvailUnknown";
	private static AvailUnknown avUn = new AvailUnknown();

    public static DataCenter create(String name) {
        if(name.equals("SometimesNoAvail")) {
            return new AvailAvailAgain();
        } else if(name.equals(SOMETIMES_AVAIL_UNKNOWN)) {
            return avUn;
        } else if(name.equals("Simple")) {
            return new MockDC();
        } else if(name.equals("SometimesNoData")) {
            return new SometimesNoData();
        }
        throw new RuntimeException("No mock dc by the name of " + name
                + " known");
    }

    private static class FailCounter extends MockDC {

        public boolean failThisTime() {
            return numRequests++ % failEvery == 0;
        }

        private int numRequests;

        private int failEvery = 10;
    }

    private static class AvailAvailAgain extends FailCounter {

        // Alternates between no available data then all data available per 10
        // requests
        public RequestFilter[] available_data(RequestFilter[] a_filterseq) {
            if(failThisTime()) {
                return new RequestFilter[0];
            }
            return super.available_data(a_filterseq);
        }
    }

    private static class AvailUnknown extends FailCounter {

        // Alternates between an UNKNOWN then all data available per 10
        // requests
        public RequestFilter[] available_data(RequestFilter[] a_filterseq) {
            if(failThisTime()) {
                throw new UNKNOWN("The void consumes you");
            }
            return super.available_data(a_filterseq);
        }
    }

    private static class SometimesNoData extends FailCounter {

        // Alternates between an UNKNOWN then all data available per 10
        // requests
        public LocalSeismogram[] retrieve_seismograms(RequestFilter[] a_filterseq) {
            if(failThisTime()) {
                return new LocalSeismogramImpl[0];
            }
            return super.retrieve_seismograms(a_filterseq);
        }
    }
}
