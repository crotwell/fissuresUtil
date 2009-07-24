package edu.sc.seis.fissuresUtil.flow.tester.event;

import edu.sc.seis.fissuresUtil.flow.extractor.event.MagnitudeTypeExtractor;
import edu.sc.seis.fissuresUtil.flow.tester.Fail;
import edu.sc.seis.fissuresUtil.flow.tester.NoTestSubject;
import edu.sc.seis.fissuresUtil.flow.tester.Pass;
import edu.sc.seis.fissuresUtil.flow.tester.TestResult;
import edu.sc.seis.fissuresUtil.flow.tester.Tester;

public class MagnitudeTypeTester implements Tester {

    public MagnitudeTypeTester() {
        this("%");
    }

    public MagnitudeTypeTester(String acceptableType) {
        this.acceptable = acceptableType;
    }

    public TestResult test(Object o) {
        String subject = mte.extract(o);
        if(subject == null) {
            return new NoTestSubject("No Magnitude type found in " + o);
        }
        return test(subject);
    }

    public TestResult test(String type) {
        if(acceptable.equals("%")) {
            return new Pass("I accept anything");
        }
        if(acceptable.equals(type)) {
            return new Pass(type + " equals " + acceptable);
        }
        return new Fail(type + " is different than " + acceptable);
    }

    private MagnitudeTypeExtractor mte = new MagnitudeTypeExtractor();

    private String acceptable;
}
