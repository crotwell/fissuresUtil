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
import java.awt.Polygon;



public class Crosshair implements Drawable{
    public Crosshair(int x, int y){
        int[] xTriX = {x-2,x,x+2};
        int[] xTriY = {1, 3, 1};
        topXTriangle = new Polygon(xTriX, xTriY, 3);
        int[] yTriX = {1, 3, 1};
        int[] yTriY = {y-2,y,y+2};
        leftYTriangle = new Polygon(yTriX,yTriY, 3);
        this.x = x;
        this.y = y;
    }

    public void setXY(int x, int y){
        topXTriangle.translate(x - this.x, 0);
        leftYTriangle.translate(0 , y - this.y);
        this.x = x;
        this.y = y;
    }

    public Color getColor(){
        return Color.BLACK;
    }

    public void setVisibility(boolean b) {
        visible = b;
    }

    public void draw(Graphics2D canvas, Dimension size, TimeEvent currentTime, AmpEvent currentAmp) {
        if(visible){
            canvas.setStroke(DisplayUtils.TWO_PIXEL_STROKE);
            canvas.setPaint(Color.BLACK);
            canvas.draw(topXTriangle);
            canvas.draw(leftYTriangle);
            int[] bottomXTriX = {x-2,x,x+2};
            int[] bottomXTriY = {size.height-1, size.height-3, size.height-1};
            canvas.draw(new Polygon(bottomXTriX, bottomXTriY, 3));
            int[] rightYTriX = {size.width-1, size.width-3, size.width-1};
            int[] rightYTriY = {y-2,y,y+2};
            canvas.draw(new Polygon(rightYTriX, rightYTriY, 3));

        }
    }

    private int x, y;

    private Polygon leftYTriangle, topXTriangle;

    private boolean visible = true;
}

