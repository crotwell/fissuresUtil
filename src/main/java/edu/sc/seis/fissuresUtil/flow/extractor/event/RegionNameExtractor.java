package edu.sc.seis.fissuresUtil.flow.extractor.event;

import edu.iris.Fissures.FlinnEngdahlRegion;
import edu.sc.seis.fissuresUtil.display.ParseRegions;

public class RegionNameExtractor {

    public String extract(Object o) {
        if(o instanceof String) {
            return (String)o;
        }
        FlinnEngdahlRegion reg = re.extract(o);
        if(reg != null) {
            return pr.getRegionName(reg);
        }
        return null;
    }

    private RegionExtractor re = new RegionExtractor();
    private ParseRegions pr = ParseRegions.getInstance();
}
