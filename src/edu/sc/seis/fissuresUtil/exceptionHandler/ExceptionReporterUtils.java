/**
 * ExceptionReporterUtils.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.exceptionHandler;

import edu.iris.Fissures.FissuresException;
import edu.sc.seis.fissuresUtil.chooser.ClockUtil;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

public class ExceptionReporterUtils{
    public static String getTrace(Throwable exception) {
        String traceString = "";
        if (exception instanceof FissuresException) {
            traceString += "Description: "+((FissuresException)exception).the_error.error_description+"\n";
            traceString += "Error Code= "+((FissuresException)exception).the_error.error_code+"\n";
        }
        if (exception.getCause() != null) {
            traceString += getTrace(exception.getCause())+"\n";
        }
        traceString += extractTrace(exception);
        return traceString;
    }

    public static String getSysInfo() {
        String sysInfo = "";
        sysInfo += "Date : "+new java.util.Date().toString()+"\n";
        try {
            sysInfo += "Server offset : "+ClockUtil.getServerTimeOffset()+"\n";
        } catch (IOException e) {
            sysInfo += "Server offset : "+e.toString()+"\n";
        }
        sysInfo += "os.name : "+System.getProperty("os.name")+"\n";
        sysInfo += "os.version : "+System.getProperty("os.version")+"\n";
        sysInfo += "os.arch : "+System.getProperty("os.arch")+"\n";
        sysInfo += "java.runtime.version : "+System.getProperty("java.runtime.version")+"\n";
        sysInfo += "java.class.version : "+System.getProperty("java.class.version")+"\n";
        sysInfo += "java.class.path : "+System.getProperty("java.class.path")+"\n";
        sysInfo += "edu.sc.seis.gee.configuration : "+System.getProperty("edu.sc.seis.gee.configuration")+"\n";
        sysInfo += "user.name : "+System.getProperty("user.name")+"\n";
        sysInfo += "user.timeZone : "+System.getProperty("user.timeZone")+"\n";
        sysInfo += "user.region : "+System.getProperty("user.region")+"\n";

        sysInfo += "\n\n\n Other Properties:\n";
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        java.util.Properties props = System.getProperties();
        props.list(printWriter);
        printWriter.close();
        sysInfo += stringWriter.getBuffer();
        return sysInfo;
    }

    private static String extractTrace(Throwable e) {
        StringWriter  stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        e.printStackTrace(printWriter);
        return stringWriter.toString();
    }

    public static String getExceptionClassName(Throwable e){
        String defaultName = e.getClass().toString();
        int lastPeriod = defaultName.lastIndexOf(".");
        if(lastPeriod != -1) defaultName = defaultName.substring(++lastPeriod);
        return defaultName;
    }

    public static String getMemoryUsage() {
        return ((Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory())/MB)+"/"+
            (Runtime.getRuntime().totalMemory()/MB)+"/"+
            (Runtime.getRuntime().maxMemory()/MB+" Mb");
    }

    private static final long MB = 1024*1024;
}

