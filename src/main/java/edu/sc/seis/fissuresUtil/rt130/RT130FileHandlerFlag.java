package edu.sc.seis.fissuresUtil.rt130;


public class RT130FileHandlerFlag {

    private RT130FileHandlerFlag(String val) {
        this.val = val;
    }

    public String toString() {
        return val;
    }

    private String val;

    public static final RT130FileHandlerFlag SCAN = new RT130FileHandlerFlag("scan");

    public static final RT130FileHandlerFlag FULL = new RT130FileHandlerFlag("full");
}
