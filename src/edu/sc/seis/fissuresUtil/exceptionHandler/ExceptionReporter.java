/**
 * ExceptionReporter.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.exceptionHandler;

import java.io.File;
import java.util.Map;

public interface ExceptionReporter{
    public void report(String message, Throwable e, Map parsedContents);
}

