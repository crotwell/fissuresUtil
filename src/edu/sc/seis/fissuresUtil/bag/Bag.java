package edu.sc.seis.fissuresUtil.bag;

/**
 * Bag.java
 *
 *
 * Created: Tue Mar 25 12:50:39 2003
 *
 * @author <a href="mailto:crotwell@owl.seis.sc.edu">Philip Crotwell</a>
 * @version 1.0
 */
import java.io.*;

import edu.iris.Fissures.AuditInfo;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.xml.DataSet;
import edu.sc.seis.fissuresUtil.xml.MemoryDataSet;
import edu.sc.seis.fissuresUtil.xml.MemoryDataSetSeismogram;
import edu.sc.seis.fissuresUtil.xml.SeismogramFileTypes;
import edu.sc.seis.fissuresUtil.xml.URLDataSetSeismogram;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import org.apache.log4j.Logger;

public class Bag {
    
    public Bag(BufferedReader reader, BufferedWriter writer) {
        in = reader;
        out = writer;
        AuditInfo[] audit = new AuditInfo[0];
        dataSet = new MemoryDataSet("BagMaster",
                                    "Main",
                                    System.getProperty("user.name"),
                                    audit);
    } // Bag constructor
    
    public void start() {
        String cmdLine;
        while(true) {
            try {
                out.write("bag> ");
                out.flush();
                cmdLine = in.readLine();
                StringTokenizer tokenizer = new StringTokenizer(cmdLine);
                List tokens = new LinkedList();
                while (tokenizer.hasMoreTokens()) {
                    tokens.add(tokenizer.nextToken());
                }
                Iterator it = tokens.iterator();
                String cmd = (String)it.next();
                if (cmd.equals("r") || cmd.equals("read")) {
                    while (it.hasNext()) {
                        File file = new File((String)it.next());
                        if (file.exists()) {
                            URLDataSetSeismogram urlSeis = new URLDataSetSeismogram(file.toURL(), SeismogramFileTypes.SAC);
                            AuditInfo[] dssAudit = new AuditInfo[1];
                            dssAudit[0] = new AuditInfo(System.getProperty("user.name"),
                                                        "Load from sac file "+file.getName());
                            dataSet.addDataSetSeismogram(urlSeis, dssAudit);
                        } else {
                            System.err.println("File "+file.getName()+" doesn't exist");
                        }
                    }
                } else if (cmd.equals("lh") || cmd.equals("listheader")) {
                    String[] names = dataSet.getDataSetSeismogramNames();
                    for (int i = 0; i < names.length; i++) {
                        MemoryDataSetSeismogram dss = (MemoryDataSetSeismogram)dataSet.getDataSetSeismogram(names[i]);
                        out.write("NAME:"+names[i]);
                        out.newLine();
                        LocalSeismogramImpl[] seis = dss.getCache();
                        for (int j = 0; j < seis.length; j++) {
                            out.write("NPTS:"+seis[j].getNumPoints());
                            out.newLine();
                            out.write("Sampling: "+seis[j].getSampling());
                            out.newLine();
                            out.write("begin: "+seis[j].getBeginTime());
                            out.newLine();
                        }
                    }
                } else if (cmd.equals("quit")) {
                    break;
                }
            } catch (IOException e) {
                logger.error("Got IOExcetion.",e);
                break;
            }
        }
        logger.debug("Done");
    }
    
    protected BufferedReader in;
    
    protected BufferedWriter out;
    
    protected DataSet dataSet;
    
    public static void main(String[] args) {
        Bag bag = new Bag(new BufferedReader(new InputStreamReader(System.in)),
                          new BufferedWriter(new OutputStreamWriter(System.out)));
        bag.start();
    } // end of main()
    
    private static Logger logger = Logger.getLogger(Bag.class);
    
} // Bag
