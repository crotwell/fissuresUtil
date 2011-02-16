package edu.sc.seis.fissuresUtil.cache;

import edu.iris.Fissures.FissuresException;
import edu.iris.Fissures.Time;
import edu.iris.Fissures.IfSeismogramDC.DataCenterCallBack;
import edu.iris.Fissures.IfSeismogramDC.LocalSeismogram;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.sc.seis.fissuresUtil.bag.Cut;


public class CoarseRequestCutSeismogramDC extends AbstractProxySeismogramDC {

    public CoarseRequestCutSeismogramDC(ProxySeismogramDC dc) {
        super(dc);
    }

    public RequestFilter[] available_data(RequestFilter[] aFilterseq) {
        return getWrappedDC().available_data(aFilterseq);
    }

    public void cancel_request(String aRequest) throws FissuresException {
        getWrappedDC().cancel_request(aRequest);
    }

    public String queue_seismograms(RequestFilter[] aFilterseq) throws FissuresException {
        return getWrappedDC().queue_seismograms(aFilterseq);
    }

    public String request_seismograms(RequestFilter[] aFilterseq,
                                      DataCenterCallBack aClient,
                                      boolean longLived,
                                      Time expirationTime) throws FissuresException {
        return getWrappedDC().request_seismograms(aFilterseq, aClient, longLived, expirationTime);
    }

    public String request_status(String aRequest) throws FissuresException {
        return request_status(aRequest);
    }

    public LocalSeismogram[] retrieve_queue(String aRequest) throws FissuresException {
        return getWrappedDC().retrieve_queue(aRequest); 
    }

    public LocalSeismogram[] retrieve_seismograms(RequestFilter[] aFilterseq) throws FissuresException {
        LocalSeismogram[] orig = getWrappedDC().retrieve_seismograms(aFilterseq);
        LocalSeismogram[] out = Cut.coarseCut(aFilterseq, orig);
        int origNpts = 0;
        for (int i = 0; i < orig.length; i++) {
            origNpts += orig[i].num_points;
        }
        int outNpts = 0;
        for (int i = 0; i < out.length; i++) {
            outNpts += out[i].num_points;
        }
        if (origNpts - outNpts > .2*outNpts) {
            logger.warn("Server sent seismograms significantly beyond request: orig num points="+origNpts+"  cut num points="+outNpts);
        }
        return out;
    }

    public String toString() {
        return "CoarseCut " + getWrappedDC().toString();
    }

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(CoarseRequestCutSeismogramDC.class);
}
