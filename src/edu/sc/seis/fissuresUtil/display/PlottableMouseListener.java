package edu.sc.seis.fissuresUtil.display;

import java.awt.*;
import java.awt.event.*;


/**
 * PlottableMouseListener.java
 *
 *
 * Created: Tue Mar 11 15:03:48 2003
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class PlottableMouseListener extends MouseAdapter implements MouseMotionListener{
    public PlottableMouseListener (PlottableDisplay plottableDisplay){
	this.plottableDisplay = plottableDisplay;
    }

    public void mousePressed(MouseEvent me) {
	System.out.println("the x co ordinate is "+me.getX()+ " the y co ordinate is "+me.getY());
	prevx = me.getX();
	prevy = me.getY();
	//me.getX(), me.getY());
    } 

    public void mouseReleased(MouseEvent me) {

    }
    
    public void mouseDragged(MouseEvent me) {
	plottableDisplay.setSelectedRectangle(prevx, prevy, me.getX(), me.getY());
    }
    
    public void mouseMoved(MouseEvent me) {

    }
    
   
    private int prevx = 0;

    private int prevy = -1;
    
    private PlottableDisplay plottableDisplay;
    
}// PlottableMouseListener
