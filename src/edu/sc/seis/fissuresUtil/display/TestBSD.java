package edu.sc.seis.fissuresUtil.display;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import org.apache.log4j.*;
import java.awt.print.*;
import java.util.*;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.freq.*;

/**
 * TestBSD.java
 *
 *
 * Created: Tue Aug 06 21:02:31 2002
 *
 * @author <a href="mailto:Charlie@BIG-JESUS"></a>
 * @version
 */

public class TestBSD extends JFrame{
    public TestBSD(){
	super("TestBSD");
	bsd = new BasicSeismogramDisplay(new DataSetSeismogram(((LocalSeismogramImpl)SeisPlotUtil.createSineWave()),
										      null), "TEST", null);
	bsd.addBottomTimeBorder();
	bsd.setSize(500, 500);
	getContentPane().add(bsd, BorderLayout.CENTER);
	addFilterButton();
	addScrollLeftButton();
	addScrollRightButton();
	addZoomIn();
	addZoomOut();
	//addSelectionTest();
	getContentPane().add(panel, BorderLayout.SOUTH);
    }

    public void addTestData(int numSeis){
	for(int i = 0; i < numSeis; i++){
	    bsd.addSeismogram(new DataSetSeismogram(((LocalSeismogramImpl)SeisPlotUtil.createTestData()), null));
	}
    }
    
    public void addFilterButton(){
	JButton filterButton = new JButton("Filter");
	filterButton.addActionListener(new ActionListener(){
		public void actionPerformed(ActionEvent e){
		    bsd.applyFilter(filter);
		}
	    });
	panel.add(filterButton);
    }

    private static ColoredFilter filter = new ColoredFilter(new SeisGramText(null), 1.0, 10, 2, 1, Color.black);
    
    public void addScrollLeftButton(){
	JButton scrollButton = new JButton("Scroll Left");
	scrollButton.addActionListener(new ActionListener(){
		public void actionPerformed(ActionEvent e){
		    if(!scrollLeft){
			scrollLeftTimer = new java.util.Timer();
			scrollLeftTimer.scheduleAtFixedRate(new TimerTask(){
				public void run(){
				    bsd.getTimeConfig().fireTimeRangeEvent(new TimeSyncEvent(.001, .001, true));
				}
			    }, new Date(), 10);
			scrollLeft = true;
		    }else{
			scrollLeft = false;
			scrollLeftTimer.cancel();
		    }
		}});
	panel.add(scrollButton);
    }

    private boolean scrollLeft = false;

    private java.util.Timer scrollLeftTimer;

    public void addScrollRightButton(){
	JButton scrollButton = new JButton("Scroll Right");
	scrollButton.addActionListener(new ActionListener(){
		public void actionPerformed(ActionEvent e){
		    if(!scrollRight){
			scrollRightTimer = new java.util.Timer();
			scrollRightTimer.scheduleAtFixedRate(new TimerTask(){
				public void run(){
				    bsd.getTimeConfig().fireTimeRangeEvent(new TimeSyncEvent(-.001, -.001, true));
				}
			    }, new Date(), 10);
			scrollRight = true;
		    }else{
			scrollRight = false;
			scrollRightTimer.cancel();
		    }
		}});
	panel.add(scrollButton);
    }

    private boolean scrollRight = false;
    
    private java.util.Timer scrollRightTimer;

    public void addZoomIn(){
	JButton zoomButton = new JButton("Zoom In");
	zoomButton.addActionListener(new ActionListener(){
		public void actionPerformed(ActionEvent e){
		    bsd.getTimeConfig().fireTimeRangeEvent(new TimeSyncEvent(.125, -.125, false));
		}
	    });
	panel.add(zoomButton);
    }	


    public void addZoomOut(){
	JButton zoomButton = new JButton("Zoom Out");
	zoomButton.addActionListener(new ActionListener(){
		public void actionPerformed(ActionEvent e){
		    bsd.getTimeConfig().fireTimeRangeEvent(new TimeSyncEvent(-.125, .125, false));
		}
	    });
	panel.add(zoomButton);
    }		     		     
				       		       
    public static void main(String[] args){
	TestBSD jf = new TestBSD();
	jf.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	jf.setSize(800, 500);
        jf.setVisible(true);
        jf.addWindowListener(new java.awt.event.WindowAdapter() {
                public void windowClosed(java.awt.event.WindowEvent e) {
                    System.exit(0);
                }
            });
    }

    protected BasicSeismogramDisplay bsd;

    protected JPanel panel = new JPanel();
}// TestBSD
