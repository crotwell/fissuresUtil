package edu.sc.seis.fissuresUtil.display;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.Date;
import javax.swing.JComponent;
import javax.swing.JFrame;
import junit.framework.TestCase;
import org.apache.log4j.BasicConfigurator;
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
import edu.sc.seis.fissuresUtil.database.plottable.JDBCPlottableTest;
import edu.sc.seis.fissuresUtil.database.plottable.PlottableChunk;
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
             new MicroSecondDate(t),
             KZAChannel);
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

    public void ttestMadeupData() {
        PlottableChunk c = JDBCPlottableTest.createFullDayPlottable();
        Plottable plott = c.getData();
        show(new Plottable[] {plott}, c.getBeginTime(), c.getChannel());
    }

    public void ttestPDF() throws IOException {
        getPreppedDisplayFakeData().outputToPDF("test.pdf");
    }

    public void ttestPNG() throws IOException {
        getPreppedDisplayFakeData().outputToPNG("test.png");
    }

    public void ttestPNGPDF() throws IOException {
        PlottableDisplay disp = getPreppedDisplayFakeData();
        disp.outputToPNG("test.png");
        disp.outputToPDF("test.pdf");
    }

    private static PlottableDisplay getPreppedDisplayFakeData() {
        PlottableChunk c = JDBCPlottableTest.createFullDayPlottable();
        Plottable plott = c.getData();
        PlottableDisplay disp = new PlottableDisplay(5400, false);
        disp.setPlottable(new Plottable[] {plott},
                          c.getChannel().station_code,
                          c.getChannel().channel_code,
                          c.getBeginTime(),
                          Initializer.ANDYChannel);
        return disp;
    }

    public static void show(Plottable[] plott, Date d, ChannelId id) {
        PlottableDisplay disp = new PlottableDisplay();
        disp.setPlottable(plott,
                          id.station_code,
                          id.channel_code,
                          d,
                          Initializer.ANDYChannel);
        setupFrame(disp);
        try {
            Thread.sleep(10000000);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
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