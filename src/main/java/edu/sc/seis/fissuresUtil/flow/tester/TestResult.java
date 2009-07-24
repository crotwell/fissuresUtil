package edu.sc.seis.fissuresUtil.flow.tester;

public class TestResult {

    protected TestResult(String reason, boolean passed) {
        this.reason = reason;
        this.passed = passed;
    }

    public String getReason() {
        return reason;
    }

    public boolean passed() {
        return passed;
    }

    private boolean passed = false;

    private String reason;
}
