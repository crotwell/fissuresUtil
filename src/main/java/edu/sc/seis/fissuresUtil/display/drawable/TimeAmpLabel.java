package edu.sc.seis.fissuresUtil.display.drawable;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import edu.sc.seis.fissuresUtil.display.DisplayUtils;
import edu.sc.seis.fissuresUtil.display.SeismogramDisplay;
import edu.sc.seis.fissuresUtil.display.registrar.AmpEvent;
import edu.sc.seis.fissuresUtil.display.registrar.TimeEvent;

public class TimeAmpLabel implements NamedDrawable {

    public Rectangle2D drawName(Graphics2D canvas, int xPosition, int yPosition) {
        if(visible && !SeismogramDisplay.PRINTING) {
            canvas.setPaint(Color.BLACK);
            Rectangle2D.Float stringBounds = new Rectangle2D.Float();
            stringBounds.setRect(canvas.getFontMetrics()
                    .getStringBounds(ampText, canvas));
            canvas.drawString(timeText,
                              xPosition + stringBounds.width,
                              yPosition);
            stringBounds.setRect(canvas.getFontMetrics()
                    .getStringBounds(timeText, canvas));
            canvas.drawString(ampText,
                              xPosition + stringBounds.width,
                              yPosition - stringBounds.height);
        }
        //Returns an empty rectangle as this doesn't factor into top left
        // corner
        //space concerns
        return DisplayUtils.EMPTY_RECTANGLE;
    }

    public void draw(Graphics2D canvas,
                     Dimension size,
                     TimeEvent currentTime,
                     AmpEvent currentAmp) {
    // Does nothing
    }

    public void setVisibility(boolean b) {
        visible = b;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color c) {
        color = c;
    }

    public String getText() {
        return timeText + ampText;
    }

    public void setText(String[] timeAmp) {
        timeText = timeAmp[0];
        ampText = timeAmp[1];
    }

    private Color color = Color.BLACK;

    private boolean visible = true;

    private String timeText = "";

    private String ampText = "";
}