package edu.sc.seis.fissuresUtil.mockFissures.IfSeismogramDC;

import org.omg.CORBA.Context;
import org.omg.CORBA.ContextList;
import org.omg.CORBA.DomainManager;
import org.omg.CORBA.ExceptionList;
import org.omg.CORBA.NVList;
import org.omg.CORBA.NamedValue;
import org.omg.CORBA.Object;
import org.omg.CORBA.Policy;
import org.omg.CORBA.Request;
import org.omg.CORBA.SetOverrideType;

import edu.iris.Fissures.FissuresException;
import edu.iris.Fissures.Time;
import edu.iris.Fissures.IfSeismogramDC.DataCenter;
import edu.iris.Fissures.IfSeismogramDC.DataCenterCallBack;
import edu.iris.Fissures.IfSeismogramDC.DataCenterPOA;
import edu.iris.Fissures.IfSeismogramDC.LocalSeismogram;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;

/**
 * @author groves Created on Nov 9, 2004
 */
public class MockDC extends DataCenterPOA implements DataCenter{

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
            seis[i] = MockSeismogram.createSpike(tr.getBeginTime(),
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

    public void _release() {}

    public int _hash(int maximum) {
        return 0;
    }

    public DomainManager[] _get_domain_managers() {
        return null;
    }

    public Object _duplicate() {
        return null;
    }

    public boolean _is_equivalent(Object other) {
        return false;
    }

    public Policy _get_policy(int policy_type) {
        return null;
    }

    public Request _request(String operation) {
        return null;
    }

    public Object _set_policy_override(Policy[] policies, SetOverrideType set_add) {
        return null;
    }

    public Request _create_request(Context ctx, String operation, NVList arg_list, NamedValue result) {
        return null;
    }

    public Request _create_request(Context ctx, String operation, NVList arg_list, NamedValue result, ExceptionList exclist, ContextList ctxlist) {
        return null;
    }
}