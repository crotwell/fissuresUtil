/**
 * Log4jReporter.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.exceptionHandler;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;



public class Log4jReporter implements ExceptionReporter{

    public void report(String message, Throwable e, List sections) throws IOException{
        if (e.getCause() != null) {
            logger.error(message, e.getCause());
        }
        logger.error(message, e);
        Iterator it = sections.iterator();
        while(it.hasNext()) {
            Section section = (Section)it.next();
            logger.error(section.getName()+":"+section.getContents());
        }
    }

    private static Logger logger = Logger.getLogger(Log4jReporter.class);

}

