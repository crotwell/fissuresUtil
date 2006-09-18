package edu.sc.seis.fissuresUtil.rt130;

public class RT130BadPacketException extends Exception {

    public RT130BadPacketException() {
        super();
    }

    public RT130BadPacketException(String s) {
        super(s);
    }

    public RT130BadPacketException(String s, Throwable cause) {
        super(s, cause);
    }

    private static final long serialVersionUID = 1L;
}