/**
 * TimeAmpPlotter.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.display;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;



public class TimeAmpPlotter implements NamedPlotter{

    public TimeAmpPlotter(BasicSeismogramDisplay display){
        this.display = display;
    }

    public void toggleVisibility() {
        visible = !visible;
    }

    public boolean drawName(Graphics2D canvas, int xPosition, int yPosition) {
        if(visible){
            text = VerticalSeismogramDisplay.getLabelText();
            canvas.setPaint(Color.black);
            canvas.drawString(text, xPosition, yPosition);
        }
        return visible;
    }

    public void draw(Graphics2D canvas, Dimension size, TimeEvent currentTime, AmpEvent currentAmp) {
        // Does nothing
    }
    public void setVisibility(boolean b) {
        visible = b;
    }

    public String getText(){ return text; }

    public BasicSeismogramDisplay getDisplay(){ return display; }

    private boolean visible = false;

    private String text = "";

    private BasicSeismogramDisplay display;
}

