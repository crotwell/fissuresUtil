/**
 * VerticalLine.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.display.drawable;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import edu.sc.seis.fissuresUtil.display.DisplayUtils;
import edu.sc.seis.fissuresUtil.display.SeismogramDisplay;
import edu.sc.seis.fissuresUtil.display.registrar.AmpEvent;
import edu.sc.seis.fissuresUtil.display.registrar.TimeEvent;



public class VerticalLine implements MovableDrawable {

    private Color color;
    private int x;
    private boolean visible = true;

    public VerticalLine(int x, Color color){
        setXY(x, 0);
        setColor(color);
    }

    public void draw(Graphics2D canvas, Dimension size, TimeEvent currentTime, AmpEvent currentAmp) {
        if (visible && !SeismogramDisplay.PRINTING){
            canvas.setStroke(DisplayUtils.TWO_PIXEL_STROKE);
            canvas.setPaint(color);
            canvas.drawLine(x, 0, x, (int)size.getHeight());
        }
    }

    public void setVisibility(boolean b) {
        visible = b;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color){
        this.color = color;
    }

    public void setXY(int x, int y){
        this.x = x;
    }

}

