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

    public void mouseClicked(MouseEvent me) {
        if(me.getClickCount() == 2) {
            plottableDisplay.setSelectedEventFlag(me);
        }
    }
    public void mousePressed(MouseEvent me) {
        prevx = me.getX();
        prevy = me.getY();
        plottableDisplay.setSelectedSelection(prevx, prevy);
    }

    public void mouseReleased(MouseEvent me) { }

    public void mouseDragged(MouseEvent me) {
        plottableDisplay.setSelectedRectangle(prevx, prevy, me.getX(), me.getY());
    }

    public void mouseMoved(MouseEvent me) {    }

    private int prevx = 0;

    private int prevy = -1;

    private PlottableDisplay plottableDisplay;

}// PlottableMouseListener
