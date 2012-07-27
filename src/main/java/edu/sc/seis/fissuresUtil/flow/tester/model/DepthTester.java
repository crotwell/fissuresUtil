package edu.sc.seis.fissuresUtil.flow.tester.model;

import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.flow.extractor.model.DepthExtractor;

public class DepthTester extends QuantityRangeTester{

    public DepthTester() {
        this(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
    }

    public DepthTester(double min, double max) {
        this(new QuantityImpl(min, UnitImpl.KILOMETER),
             new QuantityImpl(max, UnitImpl.KILOMETER));
    }

    public DepthTester(QuantityImpl min, QuantityImpl max) {
        super(min, max);
        setExtractor(new DepthExtractor());
    }

}
