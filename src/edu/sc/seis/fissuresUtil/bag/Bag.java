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
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import edu.iris.Fissures.AuditInfo;
import edu.iris.Fissures.Location;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.fissuresUtil.sac.SacTimeSeries;
import edu.sc.seis.fissuresUtil.sac.SacToFissures;
import edu.sc.seis.fissuresUtil.xml.DataSet;
import edu.sc.seis.fissuresUtil.xml.MemoryDataSet;
import edu.sc.seis.fissuresUtil.xml.SeisDataChangeEvent;
import edu.sc.seis.fissuresUtil.xml.SeisDataChangeListener;
import edu.sc.seis.fissuresUtil.xml.SeisDataErrorEvent;
import edu.sc.seis.fissuresUtil.xml.SeismogramFileTypes;
import edu.sc.seis.fissuresUtil.xml.URLDataSetSeismogram;

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
                String cmd = "";
                if (it.hasNext() ) {
                    cmd = (String)it.next();
                } else {
                    continue;
                }
                if (cmd.equals("r") || cmd.equals("read")) {
                    while (it.hasNext()) {
                        File file = new File((String)it.next());
                        if (file.exists()) {
                            URLDataSetSeismogram urlSeis = new URLDataSetSeismogram(file.toURL(), SeismogramFileTypes.SAC);
                            AuditInfo[] dssAudit = new AuditInfo[1];
                            dssAudit[0] = new AuditInfo(System.getProperty("user.name"),
                                                        "Load from sac file "+file.getName());
                            dataSet.addDataSetSeismogram(urlSeis, dssAudit);
                            SacTimeSeries sac = new SacTimeSeries();
                            sac.read(file.getAbsolutePath());
                            Channel chan = SacToFissures.getChannel(sac);
                            dataSet.addParameter(dataSet.CHANNEL+ChannelIdUtil.toString(chan.get_id()), chan, dssAudit);
                        } else {
                            System.err.println("File "+file.getName()+" doesn't exist");
                        }
                    }
                } else if (cmd.equals("lh") || cmd.equals("listheader")) {
                    String[] names = dataSet.getDataSetSeismogramNames();
                    for (int i = 0; i < names.length; i++) {
                        URLDataSetSeismogram dss = (URLDataSetSeismogram)dataSet.getDataSetSeismogram(names[i]);
                        out.write("NAME:"+names[i]);
                        out.newLine();
                        out.write("CHANNEL ID:"+ChannelIdUtil.toStringNoDates(dss.getRequestFilter().channel_id));
                        out.newLine();
                        Channel chan = dataSet.getChannel(dss.getRequestFilter().channel_id);
                        if (chan != null) {
                            Location loc = chan.my_site.my_station.my_location;
                            out.write("Station Loc: "+loc.latitude+" / "+loc.longitude);
                            out.newLine();
                        }
                        dss.retrieveData(new PrintSeisDataChangeListener(out));

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
        BasicConfigurator.configure();
        Bag bag = new Bag(new BufferedReader(new InputStreamReader(System.in)),
                          new BufferedWriter(new OutputStreamWriter(System.out)));
        bag.start();
    } // end of main()

    class PrintSeisDataChangeListener implements SeisDataChangeListener {
        PrintSeisDataChangeListener(BufferedWriter out) {
            this.out = out;
        }
        BufferedWriter out;
        public void pushData(SeisDataChangeEvent sdce) {
            LocalSeismogramImpl[] seis = sdce.getSeismograms();
            for (int j = 0; j < seis.length; j++) {
                try {
                    out.write("NPTS:"+seis[j].getNumPoints());
                    out.newLine();
                    out.write("Sampling: "+seis[j].getSampling());
                    out.newLine();
                    out.write("begin: "+seis[j].getBeginTime());
                    out.newLine();
                    out.flush();
                } catch (IOException e) {
                    GlobalExceptionHandler.handle("Problem writing to Writer", e);
                }
            }
        }

        public void finished(SeisDataChangeEvent sdce) {
            // TODO
        }

        public void error(SeisDataErrorEvent sdce) {
            // TODO
        }
    }

    private static Logger logger = Logger.getLogger(Bag.class);

} // Bag
