/**
 * Crosshair.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.display.drawable;
import edu.sc.seis.fissuresUtil.display.DisplayUtils;
import edu.sc.seis.fissuresUtil.display.registrar.AmpEvent;
import edu.sc.seis.fissuresUtil.display.registrar.TimeEvent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;



public class Crosshair implements Drawable{
    public Crosshair(int x, int y){
        setXY(x, y);
    }

    public void setXY(int x, int y){
        this.x = x;
        this.y = y;
    }

    public void toggleVisibility() {
        setVisibility(!visible);
    }

    public void setVisibility(boolean b) {
        visible = true;
    }

    public void draw(Graphics2D canvas, Dimension size, TimeEvent currentTime, AmpEvent currentAmp) {
        canvas.setStroke(DisplayUtils.ONE_PIXEL_STROKE);
        canvas.setPaint(Color.BLACK);
        canvas.draw(new Line2D.Float(0, y, (int)size.getWidth(), y));
        canvas.draw(new Line2D.Float(x, 0, x, (int)size.getHeight()));
    }

    private int x, y;

    private boolean visible = true;
}

