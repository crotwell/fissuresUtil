package edu.sc.seis.fissuresUtil.display;

import java.util.LinkedList;
import java.util.Iterator;
import java.io.IOException;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.iris.Fissures.IfSeismogramDC.LocalSeismogram;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * VerticalSeismogramDisplay.java
 *
 *
 * Created: Tue Jun  4 10:52:23 2002
 *
 * @author Charlie Groves
 * @version 0.1
 */

public class VerticalSeismogramDisplay extends JScrollPane{
    
    public VerticalSeismogramDisplay(){
	seismograms = new JLayeredPane();
	seismograms.setLayout(new BoxLayout(seismograms, BoxLayout.Y_AXIS));
	this.getViewport().add(seismograms);
    }
    
    public void addDisplay(LocalSeismogram seis){

	if(basicDisplays.size() > 0)
	    this.addDisplay((LocalSeismogramImpl)seis,((SeismogramDisplay)basicDisplays.getFirst()).getTimeConfig(), 
			    ((SeismogramDisplay)basicDisplays.getFirst()).getAmpConfig());
	else	    
	    this.addDisplay((LocalSeismogramImpl)seis, new BoundedTimeConfig(), new RMeanAmpConfig());
    }
    
    public void addDisplay(LocalSeismogramImpl seis){
	if(basicDisplays.size() > 0)
	    this.addDisplay(seis,((SeismogramDisplay)basicDisplays.getFirst()).getTimeConfig(), 
			((SeismogramDisplay)basicDisplays.getFirst()).getAmpConfig());
	else	    
	    this.addDisplay(seis, new BoundedTimeConfig(), new RMeanAmpConfig());
    }
    
    public void addDisplay(LocalSeismogramImpl seis, TimeRangeConfig tr, AmpRangeConfig ar){
	BasicSeismogramDisplay disp = new BasicSeismogramDisplay((LocalSeismogram)seis, tr,
								 ar, true);
	seismograms.add(disp);
	disp.addMouseMotionListener(motionForwarder);
	disp.addMouseListener(mouseForwarder);
	if(basicDisplays.size() > 0){
	    ((SeismogramDisplay)basicDisplays.getLast()).removeBottomTimeBorder();
	    ((SeismogramDisplay)basicDisplays.getFirst()).addTopTimeBorder();
	}
	basicDisplays.add(disp);
	disp.revalidate();
    }

    public void addSeismogram(LocalSeismogramImpl seis, int index){
	((SeismogramDisplay)basicDisplays.get(index)).addSeismogram((LocalSeismogram)seis);
    }

    public LinkedList getDisplays(){ return basicDisplays; }
    
    public void addViewMouseListener(MouseListener listen) {
	mouseForwarder.addMouseListener(listen);
    }

    public void removeViewMouseListener(MouseListener listen) {
	mouseForwarder.removeMouseListener(listen);
    }

    public void addViewMouseMotionListener(MouseMotionListener listen) {
	motionForwarder.addMouseMotionListener(listen);
    }

    public void removeViewMouseMotionListener(MouseMotionListener listen) {
	motionForwarder.removeMouseMotionListener(listen);
    }

    public void redraw(){
	Iterator e = basicDisplays.iterator();
	while(e.hasNext())
	    ((SeismogramDisplay)e.next()).redraw();
    }

    public void removeAll(){
	seismograms.removeAll();
	remove(seismograms);
	basicDisplays = new LinkedList();
    }

    public void removeSeismogram(MouseEvent me){
	if(basicDisplays.size() == 1){
	    this.removeAll();
	    return;
	}
	BasicSeismogramDisplay clicked = ((BasicSeismogramDisplay)me.getComponent());
	seismograms.remove(clicked);
	basicDisplays.remove(clicked);
	((SeismogramDisplay)basicDisplays.getFirst()).addTopTimeBorder();
	this.redraw();
	seismograms.revalidate();
    }
    
    protected LinkedList basicDisplays = new LinkedList();

    protected MouseForwarder mouseForwarder = new MouseForwarder();

    protected MouseMotionForwarder motionForwarder = new MouseMotionForwarder();

    protected JComponent seismograms;

    public static void main(String[] args){
	try{
	    JFrame jf = new JFrame("Test Seismogram View");
	    LocalSeismogram test1 = SeisPlotUtil.createSineWave();
	    LocalSeismogram test2 = SeisPlotUtil.createTestData();
	    LocalSeismogram test3 = SeisPlotUtil.createSineWave();
	    LocalSeismogram test4 = SeisPlotUtil.createTestData();
	    VerticalSeismogramDisplay sv = new VerticalSeismogramDisplay();
	    sv.addDisplay((LocalSeismogramImpl)test1, new BoundedTimeConfig(), new RMeanAmpConfig());
	    sv.addDisplay((LocalSeismogramImpl)test2);
	    sv.addDisplay((LocalSeismogramImpl)test3);
	    sv.addDisplay((LocalSeismogramImpl)test4);
	    sv.addSeismogram((LocalSeismogramImpl)test2, 0);
	    Dimension size = new Dimension(400, 400);
	    sv.setPreferredSize(size);
	    jf.getContentPane().add(sv);
	    jf.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	    jf.setSize(size);
	    jf.setVisible(true);
	    jf.addWindowListener(new WindowAdapter() {
		    public void windowClosed(WindowEvent e) {
			System.exit(0);
		    }
		});
	}
	catch(Exception e){ e.printStackTrace(); }
    }
}// VerticalSeismogramDisplay
