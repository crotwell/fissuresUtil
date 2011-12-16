package edu.sc.seis.fissuresUtil.flow.extractor.event;

import edu.iris.Fissures.IfEvent.Magnitude;

public class MagnitudeValueExtractor {

    public float extract(Object object) {
        Magnitude m = me.extract(object);
        if(m != null) {
            return m.value;
        }
        return -1f;
    }

    private MagnitudeExtractor me = new MagnitudeExtractor();
}
