package edu.sc.seis.fissuresUtil.display.drawable;
import edu.sc.seis.fissuresUtil.display.BasicSeismogramDisplay;
import edu.sc.seis.fissuresUtil.display.SeismogramDisplay;
import edu.sc.seis.fissuresUtil.display.registrar.AmpEvent;
import edu.sc.seis.fissuresUtil.display.registrar.TimeEvent;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;



/**
 * DisplayRemove.java
 *
 * @author Created by Charlie Groves
 */

public class DisplayRemove extends MouseAdapter implements Plotter, MouseMotionListener{

    public DisplayRemove(BasicSeismogramDisplay display){
        this.display = display;
        SeismogramDisplay.getMouseForwarder().addPermMouseListener(this);
        SeismogramDisplay.getMouseMotionForwarder().addMouseMotionListener(this);
    }


    public void draw(Graphics2D canvas, Dimension size, TimeEvent currentTime, AmpEvent currentAmp) {
        if(visible){
            canvas.setColor(drawColor);
            canvas.setStroke(new BasicStroke(3));
            canvas.drawLine(xMin, yMin, xMax, yMax);
            canvas.drawLine(xMin, yMax, xMax, yMin);
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
            display.remove();
            SeismogramDisplay.getMouseForwarder().removePermMouseListener(this);
            SeismogramDisplay.getMouseMotionForwarder().removeMouseMotionListener(this);
        }
    }

    public void mouseMoved(MouseEvent e) {
        if(intersects(e)){
            setDrawColor(Color.RED);
        }else if(e.getSource() == display){
           setDrawColor(Color.BLACK);
        }else {
            setDrawColor(Color.BLUE);
        }
    }
    public void mouseDragged(MouseEvent e) {
        if(intersects(e)){
            setDrawColor(Color.RED);
        }else if(e.getSource() == display){
            setDrawColor(Color.BLACK);
        }
    }

    public void mouseEntered(MouseEvent e){
        if(e.getSource() == display) {
           setDrawColor(Color.BLACK);
        } else {
            setDrawColor(Color.BLUE);
        }
    }

    private boolean intersects(MouseEvent e){
        if(e.getSource() == display){
            int clickX = e.getX() - display.getInsets().left;
            int clickY = e.getY() - display.getInsets().top;
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

    private Color drawColor = Color.BLUE;

    private int xMax = 10, xMin = 5, yMax = 10, yMin = 5;

    private BasicSeismogramDisplay display;

    private boolean visible = true;
}
