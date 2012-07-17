package edu.sc.seis.fissuresUtil.flow.extractor.event;

import edu.iris.Fissures.FlinnEngdahlRegion;
import edu.iris.Fissures.IfEvent.EventAttr;

public class RegionExtractor {

    public FlinnEngdahlRegion extract(Object o) {
        if(o instanceof FlinnEngdahlRegion) {
            return (FlinnEngdahlRegion)o;
        }
        EventAttr ea = eae.extract(o);
        if(ea != null) {
            return ea.region;
        }
        return null;
    }

    private EventAttrExtractor eae = new EventAttrExtractor();
}
