/**
 * TimeAmpPlotter.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.display.drawable;
import edu.sc.seis.fissuresUtil.display.BasicSeismogramDisplay;
import edu.sc.seis.fissuresUtil.display.DisplayUtils;
import edu.sc.seis.fissuresUtil.display.VerticalSeismogramDisplay;
import edu.sc.seis.fissuresUtil.display.registrar.AmpEvent;
import edu.sc.seis.fissuresUtil.display.registrar.TimeEvent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;



public class TimeAmpLabel implements NamedDrawable{

    public TimeAmpLabel(BasicSeismogramDisplay display){
        this.display = display;
    }

    public Rectangle2D drawName(Graphics2D canvas, int xPosition, int yPosition) {
        if(visible && !BasicSeismogramDisplay.PRINTING){
            canvas.setPaint(Color.BLACK);
            Rectangle2D.Float stringBounds = new Rectangle2D.Float();
            stringBounds.setRect(canvas.getFontMetrics().getStringBounds(ampText, canvas));
            canvas.drawString(timeText, xPosition + stringBounds.width, yPosition);
            stringBounds.setRect(canvas.getFontMetrics().getStringBounds(timeText, canvas));
            canvas.drawString(ampText, xPosition + stringBounds.width, yPosition - stringBounds.height);
        }
        //Returns an empty rectangle as this doesn't factor into top left corner
        //space concerns
        return DisplayUtils.EMPTY_RECTANGLE;
    }

    public void draw(Graphics2D canvas, Dimension size, TimeEvent currentTime, AmpEvent currentAmp) {
        // Does nothing
    }
    public void setVisibility(boolean b) {
        if(visible != b){
            visible = b;
            display.repaint();
        }
    }

    public Color getColor(){ return Color.BLACK; }

    public String getTimeText(){ return timeText; }

    public String getAmpText(){ return ampText; }

    public String getText(){ return timeText + ampText; }

    public void setText(String[] timeAmp){
        timeText = timeAmp[0];
        ampText = timeAmp[1];
        display.repaint();
    }

    public BasicSeismogramDisplay getDisplay(){ return display; }

    private boolean visible = false;

    private String timeText = "";

    private String ampText = "";

    private BasicSeismogramDisplay display;
}


