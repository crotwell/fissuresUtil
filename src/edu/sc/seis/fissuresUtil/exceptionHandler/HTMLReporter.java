/**
 * HTMLReporter.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.exceptionHandler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import edu.iris.Fissures.model.MicroSecondDate;

public class HTMLReporter implements ExceptionReporter{
    public HTMLReporter(File directory) throws IOException{
        this.directory = directory;
        initIndexFile();
    }

    public void report(String message, Throwable e, List sections) throws IOException{
        File outFile = new File(directory, "Exception_"+lastFileNum+".html");
        appendToIndexFile(outFile, e);

        BufferedWriter bw = new BufferedWriter(new FileWriter(outFile));
        String str = getHeader(e, lastFileNum);
        str += message;
        str += "\n<br/>\n<br/>\n";
        String stackTrace = "<h2>Stack Trace</h2><br/>";
        str +=   stackTrace + "<pre>"+makeDivider(stackTrace.length()) + ExceptionReporterUtils.getTrace(e)+"</pre>";
        bw.write(constructString(str, sections));
        bw.close();
        lastFileNum++;
    }

    protected void initIndexFile() throws IOException {
        File index = new File(directory, "index.html");
        BufferedWriter out = new BufferedWriter(new FileWriter(index));
        writeln(out, "<html>");
        writeln(out, "   <head>");
        writeln(out, "      <title>Errors</title>");
        writeln(out, "   </head>");
        writeln(out, "   <body>");
        writeln(out, "      <h2>Errors found:</h2>");
        writeln(out, "<br/>");
        out.close();
        // we do not end the body or html tags to allow simple appends to this
        // file. Most browsers are ok with this
    }

    protected void appendToIndexFile(File errorFile, Throwable t) throws IOException {
        File index = new File(directory, "index.html");
        BufferedWriter out = new BufferedWriter(new FileWriter(index, true));
        writeln(out, new MicroSecondDate()+" <a href="+'"'+errorFile.getName()+'"'+">"+t.getClass().getName()+"</a><br/>");
    }
    protected void writeln(BufferedWriter out, String s) throws IOException {
        out.write(s); out.newLine();
    }

    private String constructString(String initialBit, List sections){
        Iterator it = sections.iterator();
        while(it.hasNext()){
            initialBit += "<br/>\n" + constructString((Section)it.next());
        }
        return initialBit;
    }

    private String constructString(Section sec) {
        String result = sec.getName() + makeDivider(sec.getName().length());
        return result += sec.getContents();
    }

    private String makeDivider(int len){
        return "<hline/>";
    }

    protected String getHeader(Throwable t, int i) {
        String s = "<html>\n";
        s+="  <head>\n";
        s+="     <title>"+t.getClass().getName()+" "+i+"</title>\n";
        s+="  </head>\n";
        s+="  <body>\n";
        return s;
    }

    private File directory;

    private int lastFileNum = 1;

}

