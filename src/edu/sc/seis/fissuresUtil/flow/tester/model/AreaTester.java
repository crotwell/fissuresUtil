package edu.sc.seis.fissuresUtil.flow.tester.model;

import edu.iris.Fissures.Area;
import edu.iris.Fissures.model.GlobalAreaImpl;
import edu.sc.seis.fissuresUtil.bag.AreaUtil;
import edu.sc.seis.fissuresUtil.flow.extractor.model.LocationExtractor;
import edu.sc.seis.fissuresUtil.flow.tester.Fail;
import edu.sc.seis.fissuresUtil.flow.tester.Pass;
import edu.sc.seis.fissuresUtil.flow.tester.TestResult;
import edu.sc.seis.fissuresUtil.flow.tester.Tester;

public class AreaTester implements Tester {

    public AreaTester() {
        this(new GlobalAreaImpl());
    }

    public AreaTester(Area a) {
        this.area = a;
    }

    public Area getArea() {
        return area;
    }

    public TestResult test(Object o) {
        boolean result = AreaUtil.inArea(area, le.extract(o));
        if(result) {
            return new Pass(o + " in " + area);
        }
        return new Fail(o + " not in " + area);
    }

    private Area area;

    private LocationExtractor le = new LocationExtractor();
}
