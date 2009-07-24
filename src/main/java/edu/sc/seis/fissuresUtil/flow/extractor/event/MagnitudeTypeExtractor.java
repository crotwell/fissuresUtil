package edu.sc.seis.fissuresUtil.flow.extractor.event;

import edu.iris.Fissures.IfEvent.Magnitude;

public class MagnitudeTypeExtractor {

    public String extract(Object o) {
        if(o instanceof String) {
            return (String)o;
        }
        Magnitude m = me.extract(o);
        if(m != null) {
            return m.type;
        }
        return null;
    }

    private MagnitudeExtractor me = new MagnitudeExtractor();
}
