package edu.sc.seis.fissuresUtil.mockFissures.IfSeismogramDC;

import edu.iris.Fissures.FissuresException;
import edu.iris.Fissures.Time;
import edu.iris.Fissures.IfSeismogramDC.DataCenterCallBack;
import edu.iris.Fissures.IfSeismogramDC.DataCenterPOA;
import edu.iris.Fissures.IfSeismogramDC.LocalSeismogram;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;
import edu.sc.seis.fissuresUtil.display.SimplePlotUtil;

/**
 * @author groves Created on Nov 9, 2004
 */
public class MockDC extends DataCenterPOA{

    public RequestFilter[] available_data(RequestFilter[] a_filterseq) {
        return a_filterseq;
    }

    public String request_seismograms(RequestFilter[] a_filterseq,
                                      DataCenterCallBack a_client,
                                      boolean long_lived,
                                      Time expiration_time)
            throws FissuresException {
        throw new FissuresException();
    }

    public LocalSeismogram[] retrieve_seismograms(RequestFilter[] a_filterseq) {
        LocalSeismogram[] seis = new LocalSeismogram[a_filterseq.length];
        for(int i = 0; i < seis.length; i++) {
            RequestFilter rf = a_filterseq[i];
            MicroSecondTimeRange tr = new MicroSecondTimeRange(rf);
            seis[i] = SimplePlotUtil.createSpike(tr.getBeginTime(),
                                                 tr.getInterval(),
                                                 100,
                                                 rf.channel_id);
        }
        return seis;
    }

    public String queue_seismograms(RequestFilter[] a_filterseq)
            throws FissuresException {
        throw new FissuresException();
    }

    public LocalSeismogram[] retrieve_queue(String a_request)
            throws FissuresException {
        throw new FissuresException();
    }

    public void cancel_request(String a_request) throws FissuresException {
        throw new FissuresException();
    }

    public String request_status(String a_request) throws FissuresException {
        throw new FissuresException();
    }
}