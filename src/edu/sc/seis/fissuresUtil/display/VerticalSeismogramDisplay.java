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
    
    public void addDisplay(LocalSeismogram seis, String name){
	this.addDisplay((LocalSeismogramImpl)seis, name);
    }
    
    public void addDisplay(LocalSeismogramImpl seis, String name){
	if(basicDisplays.size() > 0)
	    this.addDisplay(seis,((SeismogramDisplay)basicDisplays.getFirst()).getTimeConfig(), 
			((SeismogramDisplay)basicDisplays.getFirst()).getAmpConfig(), name);
	else{	    
	    AmpRangeConfig ar = new RMeanAmpConfig();
	    TimeRangeConfig tr = new BoundedTimeConfig();
	    //ar.visibleAmpCalc(tr);
	    this.addDisplay(seis, tr, ar, name);
	}	    
    }
    
    public BasicSeismogramDisplay addDisplay(LocalSeismogramImpl seis, TimeRangeConfig tr, AmpRangeConfig ar, String name){
	BasicSeismogramDisplay disp = new BasicSeismogramDisplay((LocalSeismogram)seis, tr,
								 ar, true, name);
	seismograms.add(disp);
	disp.addMouseMotionListener(motionForwarder);
	disp.addMouseListener(mouseForwarder);
	if(basicDisplays.size() > 0){
	    ((SeismogramDisplay)basicDisplays.getLast()).removeBottomTimeBorder();
	    ((SeismogramDisplay)basicDisplays.getFirst()).addTopTimeBorder();
	}
	basicDisplays.add(disp);
	disp.revalidate();
	return disp;
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

    public void stopImageCreation(){
	Iterator e = basicDisplays.iterator();
	while(e.hasNext())
	    ((BasicSeismogramDisplay)e.next()).stopImageCreation();
    }

    public void removeAll(){
	seismograms.removeAll();
	remove(seismograms);
	basicDisplays = new LinkedList();
	repaint();
    }

    public void removeSeismogram(MouseEvent me){
	BasicSeismogramDisplay clicked = ((BasicSeismogramDisplay)me.getComponent());
	clicked.removeAllSeismograms();
	seismograms.remove(clicked);
	basicDisplays.remove(clicked);
	((SeismogramDisplay)basicDisplays.getFirst()).addTopTimeBorder();
	((SeismogramDisplay)basicDisplays.getLast()).addBottomTimeBorder();
    }

    public void removeDisplay(BasicSeismogramDisplay display){
	System.out.println("attempting removal of display");
	if(basicDisplays.remove(display))
	    System.out.println("removed from displays");
	display.removeAllSeismograms();
	seismograms.remove(display);
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
	    sv.addDisplay((LocalSeismogramImpl)test1, new BoundedTimeConfig(), new RMeanAmpConfig(), "");
	    sv.addDisplay((LocalSeismogramImpl)test2, "");
	    sv.addDisplay((LocalSeismogramImpl)test3, "");
	    sv.addDisplay((LocalSeismogramImpl)test4, "");
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
