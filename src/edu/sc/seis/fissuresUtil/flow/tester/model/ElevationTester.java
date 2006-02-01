package edu.sc.seis.fissuresUtil.flow.tester.model;

import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.flow.extractor.model.ElevationExtractor;

public class ElevationTester extends QuantityRangeTester {

    public ElevationTester() {
        this(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
    }

    public ElevationTester(double min, double max) {
        this(new QuantityImpl(min, UnitImpl.KILOMETER),
             new QuantityImpl(max, UnitImpl.KILOMETER));
    }

    public ElevationTester(QuantityImpl min, QuantityImpl max) {
        super(min, max);
        setExtractor(new ElevationExtractor());
    }
}
