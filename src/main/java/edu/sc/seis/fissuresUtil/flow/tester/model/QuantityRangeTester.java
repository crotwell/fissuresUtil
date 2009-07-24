package edu.sc.seis.fissuresUtil.flow.tester.model;

import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.flow.extractor.model.QuantityExtractor;
import edu.sc.seis.fissuresUtil.flow.tester.Fail;
import edu.sc.seis.fissuresUtil.flow.tester.NoTestSubject;
import edu.sc.seis.fissuresUtil.flow.tester.Pass;
import edu.sc.seis.fissuresUtil.flow.tester.TestResult;
import edu.sc.seis.fissuresUtil.flow.tester.Tester;

public abstract class QuantityRangeTester implements Tester {

    public QuantityRangeTester(QuantityImpl min, QuantityImpl max) {
        this.min = min;
        this.max = max.convertTo(min.getUnit());
        this.unit = min.getUnit();
    }

    public QuantityImpl getMin() {
        return min;
    }

    public QuantityImpl getMax() {
        return max;
    }

    public void setExtractor(QuantityExtractor qe) {
        this.extractor = qe;
    }

    public TestResult test(Object o) {
        QuantityImpl q = extractor.extract(o);
        if(q == null) {
            return new NoTestSubject("Unable to extract from " + o);
        }
        q = q.convertTo(unit);
        if(q.getValue() < min.getValue()) {
            return new Fail(q + " is too small");
        } else if(q.getValue() > max.getValue()) {
            return new Fail(q + " is too large");
        }
        return new Pass(q + " is within range");
    }

    private QuantityImpl min, max;

    private UnitImpl unit;

    private QuantityExtractor extractor;
}
