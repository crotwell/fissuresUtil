package edu.sc.seis.fissuresUtil.rt130;

public class RT130FormatError extends RuntimeException {

    public RT130FormatError(String message) {
        super(message);
    }

    public RT130FormatError(Exception e) {
        super(e);
    }
}
