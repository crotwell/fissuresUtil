/**
 * FileWriterReporter.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.exceptionHandler;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Map;



public class FileWriterReporter implements ExceptionReporter{
    public FileWriterReporter(File file){
        setFile(file);
    }
    
    public void setFile(File file){
        this.file = file;
    }
    
    public void report(String message, Throwable e, Map parsedContents) {
        try {
            FileWriter fw = new FileWriter(file);
            BufferedWriter bw = new BufferedWriter(fw);
            String str = message+"\n";
            bw.write(str, 0, str.length());
            str = ExceptionReporterUtils.getTrace(e);
            bw.write(str, 0, str.length());
            str = ExceptionReporterUtils.getSysInfo();
            bw.write(str, 0, str.length());
            bw.close();
            fw.close();
        } catch(Exception ex) {}
    }
    
    private File file;
    
}

