package edu.sc.seis.fissuresUtil.display.drawable;

/**
 * BigX.java
 * 
 * @author Created by Charlie Groves
 */
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import edu.sc.seis.fissuresUtil.display.DisplayUtils;
import edu.sc.seis.fissuresUtil.display.SeismogramDisplay;
import edu.sc.seis.fissuresUtil.display.SeismogramDisplayProvider;
import edu.sc.seis.fissuresUtil.display.registrar.AmpEvent;
import edu.sc.seis.fissuresUtil.display.registrar.TimeEvent;

public abstract class BigX extends MouseAdapter implements Drawable,
        MouseMotionListener {

    public BigX(SeismogramDisplayProvider display) {
        display.addMouseListener(this);
        display.addMouseMotionListener(this);
    }

    public abstract void clicked();

    public void setXY(int x, int y) {
        xMin = x;
        xMax = x + 5;
        yMin = y;
        yMax = y + 5;
    }

    public void draw(Graphics2D canvas,
                     Dimension size,
                     TimeEvent currentTime,
                     AmpEvent currentAmp) {
        if(visible && !SeismogramDisplay.PRINTING) {
            canvas.setColor(drawColor);
            canvas.setStroke(DisplayUtils.THREE_PIXEL_STROKE);
            canvas.drawLine(xMin, yMin, xMax, yMax);
            canvas.drawLine(xMin, yMax, xMax, yMin);
            canvas.setStroke(DisplayUtils.ONE_PIXEL_STROKE);
        }
    }

    public Color getColor() {
        return drawColor;
    }

    public void setColor(Color c) {
        throw new UnsupportedOperationException("These colors are controlled by mousing, you can't set them");
    }

    public void setVisibility(boolean b) {
        visible = b;
    }

    public void mouseClicked(MouseEvent e) {
        if(intersects(e)) {
            clicked();
        }
    }

    public void mouseMoved(MouseEvent e) {
        if(intersects(e)) {
            setDrawColor(Color.RED);
        } else {
            setDrawColor(Color.BLACK);
        }
    }

    public void useInsets(boolean insideInsets) {
        inside = insideInsets;
    }

    public void mouseDragged(MouseEvent e) {
        if(intersects(e)) {
            setDrawColor(Color.RED);
        } else {
            setDrawColor(Color.BLACK);
        }
    }

    protected boolean intersects(MouseEvent e) {
        int clickX = e.getX();
        int clickY = e.getY();
        if(clickX >= xMin && clickX <= xMax && clickY >= yMin && clickY <= yMax) { return true; }
        return false;
    }

    private void setDrawColor(Color newColor) {
        drawColor = newColor;
    }

    public Color getDrawColor() {
        return drawColor;
    }

    private Color drawColor = Color.BLACK;

    private int xMax = 10, xMin = 5, yMax = 10, yMin = 5;

    private boolean visible = true, inside = true;
}