package edu.sc.seis.fissuresUtil.chooser;

import java.util.List;

import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;


public interface ChannelChooserSeisSource {
    
    public List<RequestFilter> availableData(List<RequestFilter> request);
    
    public List<LocalSeismogramImpl> request(List<RequestFilter> request);
}
