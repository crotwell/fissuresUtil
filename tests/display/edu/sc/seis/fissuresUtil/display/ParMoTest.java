/*
 * Created on Jul 26, 2004
 */
package edu.sc.seis.fissuresUtil.display;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;
import javax.swing.JComponent;
import javax.swing.JFrame;
import junit.framework.TestCase;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.varia.NullAppender;
import edu.sc.seis.fissuresUtil.xml.DataSet;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;
import edu.sc.seis.fissuresUtil.xml.DataSetToXML;

/**
 * @author Charlie Groves
 */
public class ParMoTest extends TestCase {
    public void testParMo() throws Exception{
        BasicConfigurator.configure(new NullAppender());
        String fullFileName = "edu/sc/seis/gee/classicDataSets/BandaSea_101591/BandaSea.dsml";
        URL jarURL = getClass().getClassLoader().getResource(fullFileName);
        DataSet ds = DataSetToXML.load(jarURL);
        DataSetSeismogram dss = ds.getDataSetSeismogram("amdo.NORTH-SOUTH");
        BasicSeismogramDisplay bsd = new BasicSeismogramDisplay();
        bsd.add(new DataSetSeismogram[] { dss });
        setupFrame(bsd);
        ParticleMotionDisplay pmd = new ParticleMotionDisplay(dss,
                bsd.getTimeConfig(), Color.BLUE);
        setupFrame(pmd);
    }
    
    private static void setupFrame(JComponent contents) {
        JFrame frame = new JFrame();
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(contents);
        frame.setSize(new Dimension(400, 400));
        frame.setLocation(new Point(0, 400*frameCount++));
        frame.show();
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
    }
    
    private static int frameCount = 0;
}