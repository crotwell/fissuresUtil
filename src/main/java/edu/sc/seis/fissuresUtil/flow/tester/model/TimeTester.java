package edu.sc.seis.fissuresUtil.flow.tester.model;

import edu.iris.Fissures.Time;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeUtils;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;
import edu.sc.seis.fissuresUtil.flow.extractor.model.TimeExtractor;
import edu.sc.seis.fissuresUtil.flow.tester.Fail;
import edu.sc.seis.fissuresUtil.flow.tester.NoTestSubject;
import edu.sc.seis.fissuresUtil.flow.tester.Pass;
import edu.sc.seis.fissuresUtil.flow.tester.TestResult;
import edu.sc.seis.fissuresUtil.flow.tester.Tester;

public class TimeTester implements Tester {

    public TimeTester() {
        this(new MicroSecondTimeRange(new MicroSecondDate(0),
                                      new MicroSecondDate(TimeUtils.future)));
    }

    public TimeTester(MicroSecondTimeRange range) {
        this.range = range;
    }

    public MicroSecondTimeRange getRange() {
        return range;
    }

    public TestResult test(Object o) {
        Time t = extractor.extract(o);
        if(t == null) {
            return new NoTestSubject("No time found in " + o);
        }
        return test(t);
    }

    public TestResult test(Time t) {
        MicroSecondDate d = new MicroSecondDate(t);
        if(range.contains(new MicroSecondDate(t))) {
            return new Pass(d + " inside " + range);
        }
        return new Fail(d + " outside " + range);
    }

    private MicroSecondTimeRange range;

    private TimeExtractor extractor = new TimeExtractor();
}
