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
        handle("An uncaught exception has occured. Please report this to geebugs@seis.sc.edu", thrown);
    }
    
    public static void handle(String message, Throwable thrown) {
        if(reporters.size() == 0){
            System.err.println(message);
            thrown.printStackTrace(System.err);
        }else{
            List parsedContents = new ArrayList(sectionToContents.size());
            Iterator it = sectionToContents.keySet().iterator();
            while(it.hasNext()){
                String name = (String)it.next();
                parsedContents.add(new Section(name, parse(sectionToContents.get(name))));
            }
            if(showSysInfo){
                parsedContents.add(new Section("System Information", ExceptionReporterUtils.getSysInfo()));
            }
            it = reporters.iterator();
            List reporterExceptions = new ArrayList();
            while(it.hasNext()){
                try {
                    ((ExceptionReporter)it.next()).report(message, thrown, parsedContents);
                } catch (Exception e) {
                    it.remove();
                    reporterExceptions.add(e);
                }
            }
            it = reporterExceptions.iterator();
            while(it.hasNext()){
                handle("An exception reporter caused this exception.  It has been removed from the GlobalExceptionHandler", (Exception)it.next());
            }
        }
    }
    
    public static void add(ExceptionReporter reporter){
        reporters.add(reporter);
    }
    
    public static void add(String sectionName, File file){
        sectionToContents.put(sectionName, file);
    }
    
    public static void append(String sectionName, String contents){
        List contentsList = null;
        if(sectionToContents.containsKey(sectionName)){
            contentsList = (List)sectionToContents.get(sectionName);
        }else{
            contentsList = new LinkedList();
            sectionToContents.put(sectionName, contentsList);
        }
        if(contentsList.size() > 1000){
            contentsList.remove(0);
        }
        contentsList.add(contents);
    }
    
    private static String parse(Object item) {
        if(item instanceof List)
            return createString((List)item);
        else if(item instanceof File)
            return createString((File)item);
        else
            throw new IllegalArgumentException();
    }
    
    private static String createString(File file){
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            StringBuffer message = new StringBuffer();
            try {
                String line = reader.readLine();
                while(line != null){
                    message.append(line + "\n");
                    line = reader.readLine();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return message.toString();
        } catch (FileNotFoundException e) {
            GlobalExceptionHandler.handle("File to be displayed in Exceptionhandler not found", e);
        }
        return "";
    }
    
    private static String createString(List stringList) {
        StringBuffer message = new StringBuffer();
        Iterator it = stringList.iterator();
        while(it.hasNext()){
            message.append((String)it.next() + "\n");
        }
        return message.toString();
    }
    private static Map sectionToContents = new HashMap();
    
    private static Logger logger = Logger.getLogger(GlobalExceptionHandler.class);
    
    private static List reporters = new ArrayList();
    
    private static boolean showSysInfo = true;
}

