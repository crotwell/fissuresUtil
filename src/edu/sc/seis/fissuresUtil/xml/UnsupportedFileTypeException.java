/**
 * UnsupportedFileTypeException.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.xml;

public class UnsupportedFileTypeException extends Exception
{
    public UnsupportedFileTypeException() {
        super();
    }

    public UnsupportedFileTypeException(String msg) {
        super(msg);
    }

    public UnsupportedFileTypeException(String msg, Throwable cause) {
        super(msg, cause);
    }
}

