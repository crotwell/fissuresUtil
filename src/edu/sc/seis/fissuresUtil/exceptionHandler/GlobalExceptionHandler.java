/**
 * GlobalExceptionHandler.java
 * 
 * @author Created by Omnicore CodeGuide
 */
package edu.sc.seis.fissuresUtil.exceptionHandler;

import java.util.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import org.apache.log4j.Logger;

public class GlobalExceptionHandler {

    public static void handle(Throwable thrown) {
        handle("Houston, we have a problem...", thrown);
    }

    public static int getNumHandled() {
        return numHandled;
    }

    public static void handle(String message, Throwable thrown) {
        try {
            if(reporters.size() == 0) {
                System.err.println(message);
                thrown.printStackTrace(System.err);
                logger.error("handle exception, but there are no Reporters.",
                             thrown);
            } else {
                List parsedContents = new ArrayList(sectionToContents.size());
                Iterator it = sectionToContents.keySet().iterator();
                while(it.hasNext()) {
                    String name = (String)it.next();
                    parsedContents.add(new Section(name,
                                                   parse(sectionToContents.get(name))));
                }
                if(showSysInfo) {
                    parsedContents.add(new Section("System Information",
                                                   ExceptionReporterUtils.getSysInfo()));
                }
                List reporterExceptions = new ArrayList();
                synchronized(reporters) {
                    numHandled++;
                    it = reporters.iterator();
                    while(it.hasNext()) {
                        try {
                            ((ExceptionReporter)it.next()).report(message,
                                                                  thrown,
                                                                  parsedContents);
                        } catch(Throwable e) {
                            it.remove();
                            reporterExceptions.add(e);
                        }
                    }
                }
                it = reporterExceptions.iterator();
                while(it.hasNext()) {
                    handle("An exception reporter caused this exception.  It has been removed from the GlobalExceptionHandler",
                           (Throwable)it.next());
                }
            }
        } catch(Throwable e) {
            paranoid(e, thrown);
        }
    }

    public static void add(Extractor extractor) {
        extractors.add(extractor);
    }

    static List getExtractors() {
        return extractors;
    }

    public static void add(ExceptionReporter reporter) {
        reporters.add(reporter);
    }

    public static void add(String sectionName, File file) {
        sectionToContents.put(sectionName, file);
    }

    public static void append(String sectionName, String contents) {
        List contentsList = null;
        if(sectionToContents.containsKey(sectionName)) {
            contentsList = (List)sectionToContents.get(sectionName);
        } else {
            contentsList = new LinkedList();
            sectionToContents.put(sectionName, contentsList);
        }
        if(contentsList.size() > 1000) {
            contentsList.remove(0);
        }
        contentsList.add(contents);
    }

    /**
     * This supposedly sets a global exception handler in the awt thread only,
     * so that uncaught exceptions can be processed/saved/viewed. Will not
     * necessarily work for future releases (> 1.4). Perhaps it will, perhaps
     * not. NOTE: It does not work for exceptions in other threads. Java1.5 is
     * supposed to have a mechanism to do this.
     */
    public static void registerWithAWTThread() {
        System.setProperty("sun.awt.exception.handler",
                           "edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler");
    }

    private static String parse(Object item) throws IOException {
        if(item instanceof List) return createString((List)item);
        else if(item instanceof File) return createString((File)item);
        else throw new IllegalArgumentException();
    }

    private static String createString(File file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        StringBuffer message = new StringBuffer();
        String line = reader.readLine();
        while(line != null) {
            message.append(line + "\n");
            line = reader.readLine();
        }
        return message.toString();
    }

    private static String createString(List stringList) {
        StringBuffer message = new StringBuffer();
        Iterator it = stringList.iterator();
        while(it.hasNext()) {
            message.append((String)it.next() + "\n");
        }
        return message.toString();
    }

    private static void paranoid(Throwable e, Throwable thrown) {
        // this is for paranoid coders
        System.err.println("Caught an exception in the exception handler: "
                + e.toString());
        e.printStackTrace(System.err);
        System.err.println("Original exception was:" + thrown.toString());
        thrown.printStackTrace(System.err);
        try {
            logger.error("Caught an exception in the exception handler: ", e);
            logger.error("Original exception was:", thrown);
        } catch(Throwable loggerException) {
            //well, lets hope System.err is good enough.  
        }
    }

    private static Map sectionToContents = new HashMap();

    private static Logger logger = Logger.getLogger(GlobalExceptionHandler.class);

    private static List reporters = Collections.synchronizedList(new ArrayList());

    private static List extractors = Collections.synchronizedList(new ArrayList());

    private static boolean showSysInfo = true;

    private static int numHandled = 0;
    static {
        // always send error to log4j
        add(new Log4jReporter());
        add(new DefaultExtractor());
    }
}