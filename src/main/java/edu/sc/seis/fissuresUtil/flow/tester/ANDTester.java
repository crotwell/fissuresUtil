package edu.sc.seis.fissuresUtil.flow.tester;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class ANDTester implements Tester {

    public ANDTester(Tester[] testers2) {
        testers = Arrays.asList(testers2);
    }

    public TestResult test(Object o) {
        Iterator it = testers.iterator();
        while(it.hasNext()) {
            TestResult result = ((Tester)it.next()).test(o);
            if(!result.passed()) {
                return result;
            }
        }
        return new Pass("All subtests passed");
    }

    private List testers;
}
