/**
 * TimeAmpPlotter.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.display.drawable;
import edu.sc.seis.fissuresUtil.display.BasicSeismogramDisplay;
import edu.sc.seis.fissuresUtil.display.VerticalSeismogramDisplay;
import edu.sc.seis.fissuresUtil.display.registrar.AmpEvent;
import edu.sc.seis.fissuresUtil.display.registrar.TimeEvent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;



public class TimeAmpPlotter implements NamedPlotter{

    public TimeAmpPlotter(BasicSeismogramDisplay display){
        this.display = display;
    }

    public void toggleVisibility() {
        visible = !visible;
    }

    public boolean drawName(Graphics2D canvas, int xPosition, int yPosition) {
        if(visible){
            timeText = VerticalSeismogramDisplay.getTimeLabel().getText();
            ampText = VerticalSeismogramDisplay.getAmpLabel().getText();
            canvas.setPaint(Color.black);
            Rectangle2D.Float stringBounds = new Rectangle2D.Float();
            stringBounds.setRect(canvas.getFontMetrics().getStringBounds(ampText, canvas));
            canvas.drawString(timeText, xPosition + stringBounds.width, yPosition);
            stringBounds.setRect(canvas.getFontMetrics().getStringBounds(timeText, canvas));
            canvas.drawString(ampText, xPosition + stringBounds.width, yPosition - stringBounds.height);
        }
        return visible;
    }

    public void draw(Graphics2D canvas, Dimension size, TimeEvent currentTime, AmpEvent currentAmp) {
        // Does nothing
    }
    public void setVisibility(boolean b) {
        visible = b;
    }

    public String getTimeText(){ return timeText; }

    public String getAmpText(){ return ampText; }

    public String getText(){ return timeText + ampText; }

    public BasicSeismogramDisplay getDisplay(){ return display; }

    private boolean visible = false;

    private String timeText = "";

    private String ampText = "";

    private BasicSeismogramDisplay display;
}

