package edu.sc.seis.fissuresUtil.rt130;

import java.net.MalformedURLException;
import java.net.URL;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.fissuresUtil.xml.UnsupportedFileTypeException;

public class RT130FileHandlerFlag {

    private RT130FileHandlerFlag(String val, int intVal) {
        this.val = val;
        this.intVal = intVal;
    }

    public String toString() {
        return getValue();
    }

    public String getValue() {
        return this.val;
    }

    public int getIntValue() {
        return this.intVal;
    }

    public URL getURLValue() {
        try {
            return new URL(URL_PREFIX + getValue());
        } catch(MalformedURLException e) {
            // shouldn't ever happen as these are static strings
            GlobalExceptionHandler.handle("Trouble creating URL for file type "
                    + getValue(), e);
        }
        return null;
    }

    public static RT130FileHandlerFlag fromString(String modeURL)
            throws UnsupportedFileTypeException {
        if(modeURL.equals(FULL.getURLValue().toString())) {
            return FULL;
        } else if(modeURL.equals(NO_LOGS.getURLValue().toString())) {
            return NO_LOGS;
        } else if(modeURL.equals(POP_DB_WITH_CHANNELS.getURLValue().toString())) {
            return POP_DB_WITH_CHANNELS;
        }
        throw new UnsupportedFileTypeException(modeURL);
    }

    public static RT130FileHandlerFlag fromInt(int type)
            throws UnsupportedFileTypeException {
        if(type == FULL.getIntValue()) {
            return FULL;
        } else if(type == NO_LOGS.getIntValue()) {
            return NO_LOGS;
        } else if(type == POP_DB_WITH_CHANNELS.getIntValue()) {
            return POP_DB_WITH_CHANNELS;
        }
        throw new UnsupportedFileTypeException("" + type);
    }

    public static final RT130FileHandlerFlag SCAN = new RT130FileHandlerFlag("scan",
                                                                             1);

    public static final RT130FileHandlerFlag FULL = new RT130FileHandlerFlag("full",
                                                                             2);

    public static final RT130FileHandlerFlag NO_LOGS = new RT130FileHandlerFlag("nologs",
                                                                                3);
    
    public static final RT130FileHandlerFlag MAKE_LOGS = new RT130FileHandlerFlag("nologs",
                                                                                4);

    public static final RT130FileHandlerFlag POP_DB_WITH_CHANNELS = new RT130FileHandlerFlag("popdb",
                                                                                             5);

    public static final String URL_PREFIX = "http://www.seis.sc.edu/xml/RT130FileHandlerMode/";

    private String val;

    private int intVal;
}
