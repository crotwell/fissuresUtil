package edu.sc.seis.fissuresUtil.xml;

/**
 * SeismogramFileTypes.java
 *
 *
 * Created: Tue Mar 18 15:38:13 2003
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

import java.net.MalformedURLException;
import java.net.URL;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;

public class SeismogramFileTypes {
    private SeismogramFileTypes (String val){
        this.val = val;
    }

    public boolean equals(Object obj) {
        if(! (obj instanceof SeismogramFileTypes) ) return false;
        return ((SeismogramFileTypes)obj).getValue().equals(this.val);
    }

    public String getValue() {
        return this.val;
    }

    public URL getURLValue() {
        try {
            return new URL(URL_PREFIX+getValue());
        } catch (MalformedURLException e) {
            // shouldn't ever happen as these are static strings
            GlobalExceptionHandler.handle("Trouble creating URL for file type "+getValue(), e);
        }
        return null;
    }

    public static SeismogramFileTypes fromString(String typeURL) throws UnsupportedFileTypeException {
        if (typeURL.equals(MSEED.getURLValue().toString())) {
            return MSEED;
        } else if (typeURL.equals(SAC.getURLValue().toString())) {
            return SAC;
        } else if (typeURL.equals(PSN.getURLValue().toString())) {
            return PSN;
        }
        throw new UnsupportedFileTypeException(typeURL);
    }

    public static final SeismogramFileTypes SAC = new SeismogramFileTypes("sac");

    public static final SeismogramFileTypes MSEED = new SeismogramFileTypes("mseed");

    public static final SeismogramFileTypes PSN = new SeismogramFileTypes("psn");

    public static final String URL_PREFIX = "http://www.seis.sc.edu/xml/SeismogramFileTypes/";

    private String val;

}// SeismogramFileTypes
