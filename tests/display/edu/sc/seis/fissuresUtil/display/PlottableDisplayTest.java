package edu.sc.seis.fissuresUtil.display;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.JComponent;
import javax.swing.JFrame;

import junit.framework.TestCase;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.varia.NullAppender;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.InvalidName;
import org.omg.CosNaming.NamingContextPackage.NotFound;

import edu.iris.Fissures.Plottable;
import edu.iris.Fissures.Time;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfNetwork.NetworkId;
import edu.iris.Fissures.IfPlottable.PlottableDC;
import edu.iris.Fissures.IfPlottable.PlottableNotAvailable;
import edu.iris.Fissures.IfPlottable.UnsupportedDimension;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.iris.dmc.seedcodec.CodecException;
import edu.sc.seis.fissuresUtil.hibernate.PlottableChunk;
import edu.sc.seis.fissuresUtil.mockFissures.IfNetwork.MockChannelId;
import edu.sc.seis.fissuresUtil.namingService.FissuresNamingService;
import edu.sc.seis.fissuresUtil.simple.Initializer;

/**
 * @author groves Created on Oct 11, 2004
 */
public class PlottableDisplayTest extends TestCase {

    public void setUp() {
        BasicConfigurator.configure();
    }

    public void ttestFromServer() throws NotFound, CannotProceed, InvalidName,
            PlottableNotAvailable, UnsupportedDimension {
        System.setProperty(FissuresNamingService.CORBALOC_PROP,
                           "corbaloc:iiop:nameservice.seis.sc.edu:6371/NameService");
        PlottableDC impl = Initializer.getNS().getPlottableDC("edu/sc/seis",
                                                              "DelilahCache");
        Time t = new Time("2007113J000000.000Z", 0);
        show(impl.get_for_day(KZAChannel,
                              2005,
                              3,
                              new edu.iris.Fissures.Dimension(6000, 0)),
             new MicroSecondDate(t));
    }

    public static final NetworkId KN = new NetworkId("KN",
                                                     new Time("19910901T08:07:07.000Z",
                                                              -1));

    public static final ChannelId KZAChannel = new ChannelId(KN,
                                                             "KZA",
                                                             "  ",
                                                             "BHZ",
                                                             new Time("2000165T093659.0000Z",
                                                                      -1));
    public static PlottableChunk createFullDayPlottable() {
        return new PlottableChunk(FULL_DAY, 0, 1, 2000, PIXELS, CHAN_ID.network_id.network_code, CHAN_ID.station_code, CHAN_ID.site_code, CHAN_ID.channel_code);
    }

    public static final int PIXELS = 6000;

    private static final Time START_TIME = new Time("20000101T000000.000Z", 0);

    private static final MicroSecondDate START = new MicroSecondDate(START_TIME);

    public static final ChannelId CHAN_ID = MockChannelId.createVerticalChanId();
    
    public static final TimeInterval ONE_DAY = new TimeInterval(1, UnitImpl.DAY);
    
    private static Plottable FULL_DAY = null;
    static {
        BasicConfigurator.configure(new NullAppender());// ConsoleAppender(new
        // PatternLayout("%C{1}.%M
        // - %m\n")));
        try {
            FULL_DAY = makeDay(START);
        } catch(CodecException e) {
            e.printStackTrace();
        }
    }

    public static Plottable makeDay(MicroSecondDate startTime)
            throws CodecException {
        MicroSecondDate end = startTime.add(ONE_DAY);
        LocalSeismogramImpl seis = SimplePlotUtil.createSpike(startTime,
                                                              ONE_DAY,
                                                              757,
                                                              CHAN_ID);
        MicroSecondTimeRange fullRange = new MicroSecondTimeRange(startTime,
                                                                  end);
        return SimplePlotUtil.makePlottable(seis, PIXELS);
    }

    public void ttestMadeupData() {
        PlottableChunk c = createFullDayPlottable();
        Plottable plott = c.getData();
        show(new Plottable[] {plott}, c.getBeginTime());
    }

    public void ttestPDF() throws IOException {
        getPreppedDisplayFakeData().outputToPDF("test.pdf");
    }

    public void ttestPNG() throws IOException {
        getPreppedDisplayFakeData().outputToPNG("test.png");
    }

    public void testPNGPDF() throws IOException, ParseException {
        PlottableDisplay disp = getPreppedDisplayFakeData();
        disp.outputToPNG("test.png");
        disp.outputToPDF("test.pdf");
    }

    private static PlottableDisplay getPreppedDisplayFakeData() {
        PlottableChunk c = createFullDayPlottable();
        Plottable plott = c.getData();
        PlottableDisplay disp = new PlottableDisplay(5400, false);
        disp.setPlottable(new Plottable[] {plott},
                          c.getStationCode(),
                          c.getChannelCode(),
                          c.getBeginTime(),
                          Initializer.ANDYChannel);
        return disp;
    }

    private static PlottableDisplay getPreppedDisplayStoredData()
            throws Exception {
        String dateString = "200712121500";
        Plottable plott[] = createPlottableFromFile(dateString + ".plot");
        SimpleDateFormat fileDateFormat = new SimpleDateFormat("yyyyMMddHHmm");
        PlottableDisplay disp = new PlottableDisplay(5400, false);
        disp.setPlottable(plott,
                          "DAV",
                          "BHZ",
                          fileDateFormat.parse(dateString),
                          Initializer.ANDYChannel);
        return disp;
    }

    public static void show(Plottable[] plott, Date d) {
        PlottableDisplay disp = new PlottableDisplay();
        disp.setPlottable(plott,
                          Initializer.ANDYChannel.station_code,
                          Initializer.ANDYChannel.channel_code,
                          d,
                          Initializer.ANDYChannel);
        setupFrame(disp);
        try {
            Thread.sleep(10000000);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void savePlottableToFile(Plottable[] plot, String filename)
            throws IOException {
        Writer writer = new BufferedWriter(new FileWriter(filename));
        for(int i = 0; i < plot.length; i++) {
            Plottable curPlot = plot[i];
            writer.append("n," + curPlot.x_coor.length + '\n');
            for(int j = 0; j < curPlot.x_coor.length; j++) {
                writer.append("c," + curPlot.x_coor[j] + ","
                        + curPlot.y_coor[j] + '\n');
            }
        }
        writer.flush();
        writer.close();
    }

    public static Plottable[] createPlottableFromFile(String filename)
            throws Exception {
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        String line = reader.readLine();
        List plots = new ArrayList();
        Plottable curPlot = null;
        int cursor = 0;
        while(line != null) {
            StringTokenizer tok = new StringTokenizer(line, ",");
            String controlString = tok.nextToken();
            if(controlString.equals("n")) {
                int length = Integer.parseInt(tok.nextToken());
                if(curPlot != null) {
                    plots.add(curPlot);
                }
                curPlot = new Plottable(new int[length], new int[length]);
                cursor = 0;
            } else if(controlString.equals("c")) {
                curPlot.x_coor[cursor] = Integer.parseInt(tok.nextToken());
                curPlot.y_coor[cursor] = Integer.parseInt(tok.nextToken());
                cursor++;
            } else {
                throw new Exception("problem reading file");
            }
            line = reader.readLine();
        }
        return (Plottable[])plots.toArray(new Plottable[0]);
    }

    private static void setupFrame(JComponent contents) {
        JFrame frame = new JFrame();
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(contents);
        frame.setSize(new Dimension(600, 500));
        frame.setLocation(new Point(0, 400 * frameCount++));
        frame.show();
        frame.addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
    }

    private static int frameCount = 0;
}