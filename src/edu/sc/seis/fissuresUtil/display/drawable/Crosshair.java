package edu.sc.seis.fissuresUtil.display.drawable;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import edu.sc.seis.fissuresUtil.display.DisplayUtils;
import edu.sc.seis.fissuresUtil.display.SeismogramDisplay;
import edu.sc.seis.fissuresUtil.display.registrar.AmpEvent;
import edu.sc.seis.fissuresUtil.display.registrar.TimeEvent;

public class Crosshair implements MovableDrawable{
    public Crosshair(int x, int y){
        int[] xTriX =  {x-5,x,x+5, x+5,x-5};
        int[] xTriY = {6, 1, 6,12,12};
        topXTriangle = new Polygon(xTriX, xTriY, 5);
        int[] yTriX = {6, 1, 6,12,12};
        int[] yTriY = {y-5,y,y+5,y+5,y-5};
        leftYTriangle = new Polygon(yTriX,yTriY, 5);
        this.x = x;
        this.y = y;
    }

    public void setXY(int x, int y){
        topXTriangle.translate(x - this.x, 0);
        leftYTriangle.translate(0 , y - this.y);
        this.x = x;
        this.y = y;
    }

    public Color getColor(){ return Color.BLACK; }

    public void setVisibility(boolean b) { visible = b; }

    public void draw(Graphics2D canvas, Dimension size, TimeEvent currentTime, AmpEvent currentAmp) {
        if(visible && !SeismogramDisplay.PRINTING){
            canvas.setStroke(DisplayUtils.ONE_PIXEL_STROKE);
            canvas.setPaint(Color.RED);
            if(topTriangleVisible)canvas.draw(topXTriangle);
            canvas.draw(leftYTriangle);
            if(bottomTriangleVisible){
                int[] bottomXTriX = {x-5,x,x+5, x+5,x-5};
                int[] bottomXTriY = {size.height-6, size.height -1 , size.height-6, size.height -12,size.height -12};
                canvas.draw(new Polygon(bottomXTriX, bottomXTriY, 5));
            }
        }
    }

    public void setBottomTriangleVisible(boolean visible){
        bottomTriangleVisible = visible;
    }

    public void setTopTriangleVisible(boolean visible){
        topTriangleVisible = visible;
    }

    private int x, y;

    private Polygon leftYTriangle, topXTriangle;

    private boolean visible = true, bottomTriangleVisible = false,
        topTriangleVisible = false;
}

