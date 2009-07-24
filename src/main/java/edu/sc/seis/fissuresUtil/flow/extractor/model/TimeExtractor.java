package edu.sc.seis.fissuresUtil.flow.extractor.model;

import edu.iris.Fissures.Time;
import edu.iris.Fissures.IfEvent.Origin;
import edu.sc.seis.fissuresUtil.flow.extractor.event.OriginExtractor;

public class TimeExtractor {

    private OriginExtractor oExtractor = new OriginExtractor();

    public Time extract(Object object) {
        if(object instanceof Time){
            return (Time)object;
        }
        Origin o = oExtractor .extract(object);
        if(o != null){
            return o.getOriginTime();
        }
        return null;
    }
}
