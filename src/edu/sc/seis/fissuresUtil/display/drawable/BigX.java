package edu.sc.seis.fissuresUtil.display.drawable;

/**
 * BigX.java
 *
 * @author Created by Charlie Groves
 */

import edu.sc.seis.fissuresUtil.display.DisplayUtils;
import edu.sc.seis.fissuresUtil.display.SeismogramDisplay;
import edu.sc.seis.fissuresUtil.display.registrar.AmpEvent;
import edu.sc.seis.fissuresUtil.display.registrar.TimeEvent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

public abstract class BigX extends MouseAdapter implements Plotter, MouseMotionListener {
    public BigX(SeismogramDisplay display){
        this.display = display;
        SeismogramDisplay.getMouseForwarder().addPermMouseListener(this);
        SeismogramDisplay.getMouseMotionForwarder().addMouseMotionListener(this);
    }

    public abstract void clicked();

    public void setXY(int x, int y){
        xMin = x;
        xMax = x + 5;
        yMin = y;
        yMax = y + 5;
    }

    public void draw(Graphics2D canvas, Dimension size, TimeEvent currentTime, AmpEvent currentAmp) {
        if(visible){
            canvas.setColor(drawColor);
            canvas.setStroke(DisplayUtils.THREE_PIXEL_STROKE);
            canvas.drawLine(xMin, yMin, xMax, yMax);
            canvas.drawLine(xMin, yMax, xMax, yMin);
            canvas.setStroke(DisplayUtils.ONE_PIXEL_STROKE);
        }
    }

    public void toggleVisibility() {
        visible = !visible;
    }

    public void setVisibility(boolean b) {
        visible = b;
    }

    public void mouseClicked(MouseEvent e){
        if(intersects(e)){
            clicked();
            SeismogramDisplay.getMouseForwarder().removePermMouseListener(this);
            SeismogramDisplay.getMouseMotionForwarder().removeMouseMotionListener(this);
        }
    }

    public void mouseMoved(MouseEvent e) {
        if(intersects(e)){
            setDrawColor(Color.RED);
        }else{
            setDrawColor(Color.BLACK);
        }
    }

    public void useInsets(boolean insideInsets){
        inside = insideInsets;
    }

    public void mouseDragged(MouseEvent e) {
        if(intersects(e)){
            setDrawColor(Color.RED);
        }else if(e.getSource() == display){
            setDrawColor(Color.BLACK);
        }
    }

    protected boolean intersects(MouseEvent e){
        if(e.getSource() == display){
            int clickX = e.getX();
            if(inside){
                clickX -= display.getInsets().left;
            }
            int clickY = e.getY();
            if(inside){
                clickY -= display.getInsets().top;
            }
            if(clickX >= xMin && clickX <= xMax &&
               clickY >= yMin && clickY <= yMax){
                return true;
            }
        }
        return false;
    }

    private void setDrawColor(Color newColor){
        Color prevColor = drawColor;
        drawColor = newColor;
        if(prevColor != newColor){
            display.repaint();
        }
    }

    public Color getDrawColor(){ return drawColor; }

    private Color drawColor = Color.BLACK;

    private int xMax = 10, xMin = 5, yMax = 10, yMin = 5;

    private boolean visible = true, inside = true;

    private SeismogramDisplay display;
}

