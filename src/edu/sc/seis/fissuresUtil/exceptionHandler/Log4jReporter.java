/**
 * Log4jReporter.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.exceptionHandler;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;



public class Log4jReporter implements ExceptionReporter{

    public void report(String message, Throwable e, List sections) throws IOException{
        logger.error(message, e);
        if (e.getCause() != null) {
            logger.error("...caused by:", e.getCause());
        } else {
            Iterator it = GlobalExceptionHandler.getExtractors().iterator();
            while(it.hasNext()) {
                Extractor ext = (Extractor)it.next();
                if (ext.canExtract(e)) {
                    report("...caused by subthrowable:", ext.getSubThrowable(e), new ArrayList());
                }
            }
        }
        Iterator it = sections.iterator();
        while(it.hasNext()) {
            Section section = (Section)it.next();
            logger.debug(section.getName()+":"+section.getContents());
        }
    }

    private static Logger logger = Logger.getLogger(Log4jReporter.class);

}

