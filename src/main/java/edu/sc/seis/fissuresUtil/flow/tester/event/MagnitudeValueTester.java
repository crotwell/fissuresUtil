package edu.sc.seis.fissuresUtil.flow.tester.event;

import edu.sc.seis.fissuresUtil.flow.extractor.event.MagnitudeValueExtractor;
import edu.sc.seis.fissuresUtil.flow.tester.Fail;
import edu.sc.seis.fissuresUtil.flow.tester.NoTestSubject;
import edu.sc.seis.fissuresUtil.flow.tester.Pass;
import edu.sc.seis.fissuresUtil.flow.tester.TestResult;
import edu.sc.seis.fissuresUtil.flow.tester.Tester;

public class MagnitudeValueTester implements Tester {

    public MagnitudeValueTester() {
        this(0, 10);
    }

    public MagnitudeValueTester(float min, float max) {
        this.min = min;
        this.max = max;
    }

    public TestResult test(Object o) {
        float val = mve.extract(o);
        if(val != -1) {
            return test(val);
        }
        return new NoTestSubject(o + " did not contain a magnitude value");
    }

    public TestResult test(float f) {
        if(f > max) {
            return new Fail(f + " is too large");
        }
        if(f < min) {
            return new Fail(f + " is too small");
        }
        return new Pass(f + " is within range");
    }

    public float getMax() {
        return max;
    }

    public float getMin() {
        return min;
    }

    private MagnitudeValueExtractor mve = new MagnitudeValueExtractor();

    private float min, max;
}
