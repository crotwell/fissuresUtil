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
import edu.iris.Fissures.model.MicroSecondDate;

/**
 * TestBSD.java
 *
 *
 * Created: Tue Aug 06 21:02:31 2002
 *
 * @author <a href="mailto:Charlie"></a>
 * @version
 */

public class TestBSD extends JFrame{
    public TestBSD(){
	super("TestBSD");
	edu.iris.Fissures.Time begin = 
        new edu.iris.Fissures.Time("19911015T163000.000Z", -1);
    DataSetSeismogram[] seismos = {new DataSetSeismogram(((LocalSeismogramImpl)SimplePlotUtil.
								createSineWave()),null)};
    String[] names = {"FIRST"};
    bsd = new BasicSeismogramDisplay(seismos, names, null);
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
	DataSetSeismogram[] seismos = new DataSetSeismogram[numSeis];
	String[] names = new String[numSeis];
	for(int i = 0; i < numSeis; i++){
	    seismos[i] = new DataSetSeismogram(((LocalSeismogramImpl)SimplePlotUtil.createTestData()), null);
	    names[i] = "SINE" + 1;
	}
	bsd.add(seismos, names);

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
				    bsd.getRegistrar().shaleTime(.0005, 1);
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
				    bsd.getRegistrar().shaleTime(-.0005, 1);
				}
			    }, new Date(), 25);
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
		    bsd.getRegistrar().shaleTime(.25, .5);
		}
	    });
	panel.add(zoomButton);
    }	


    public void addZoomOut(){
	JButton zoomButton = new JButton("Zoom Out");
	zoomButton.addActionListener(new ActionListener(){
		public void actionPerformed(ActionEvent e){
		    bsd.getRegistrar().shaleTime(-.5, 2);
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
