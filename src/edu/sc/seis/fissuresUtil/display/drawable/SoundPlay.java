package edu.sc.seis.fissuresUtil.display.drawable;

import edu.sc.seis.fissuresUtil.display.DisplayUtils;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;
import edu.sc.seis.fissuresUtil.display.SeismogramContainer;
import edu.sc.seis.fissuresUtil.display.SeismogramDisplay;
import edu.sc.seis.fissuresUtil.display.SeismogramIterator;
import edu.sc.seis.fissuresUtil.display.registrar.AmpEvent;
import edu.sc.seis.fissuresUtil.display.registrar.TimeEvent;
import edu.sc.seis.fissuresUtil.sound.FissuresToWAV;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.BufferedOutputStream;



public class SoundPlay extends MouseAdapter implements Plotter, MouseMotionListener{
    private Color drawColor = Color.BLACK;
    private int xMax, xMin, yMaxA = 9, yMaxB = 11, yMinA = 6, yMinB = 4;
    private SeismogramDisplay display;
    private boolean visible = true;
    private SeismogramContainer container;
    private TimeEvent timeEvent;
    private FissuresToWAV seisWAV;

    public SoundPlay(SeismogramDisplay display, SeismogramContainer container){
        this.display = display;
        this.container = container;
        seisWAV = new FissuresToWAV(container, 200);
        SeismogramDisplay.getMouseForwarder().addPermMouseListener(this);
        SeismogramDisplay.getMouseMotionForwarder().addMouseMotionListener(this);
    }


    /**
     * Method setVisibility
     *
     * @param    b                   a  boolean
     *
     */
    public void setVisibility(boolean b) {
        visible = b;
    }

    /**
     * Method toggleVisibility
     *
     */
    public void toggleVisibility() {
        visible = !visible;
    }

    /**
     * Invoked when the mouse cursor has been moved onto a component
     * but no buttons have been pushed.
     */
    public void mouseMoved(MouseEvent e) {
        if(intersects(e)){
            setDrawColor(Color.RED);
        }else{
            setDrawColor(Color.BLACK);
        }
    }

    public void mouseClicked(MouseEvent e){
        if(intersects(e)){
            seisWAV.play(timeEvent.getTime(container.getDataSetSeismogram()));
        }
    }

    private boolean intersects(MouseEvent e){
        if(e.getSource() == display){
            int clickX = e.getX() - display.getInsets().left;
            int clickY = e.getY() - display.getInsets().top;
            if(clickX >= xMin && clickX <= xMax &&
               clickY >= yMinB && clickY <= yMaxB){
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

    /**
     * Method draw
     *
     * @param    canvas              a  Graphics2D
     * @param    size                a  Dimension
     * @param    currentTime         a  TimeEvent
     * @param    currentAmp          an AmpEvent
     *
     */
    public void draw(Graphics2D canvas, Dimension size, TimeEvent currentTime, AmpEvent currentAmp) {
        if(visible){
            int sizeOfDisplay = (int)(size.getWidth() - (double)display.getInsets().right);
            xMin = sizeOfDisplay - 6;
            xMax = sizeOfDisplay - 1;

            timeEvent = currentTime;
            canvas.setColor(drawColor);
            canvas.setStroke(DisplayUtils.THREE_PIXEL_STROKE);
            canvas.drawLine(xMin, yMinA, xMin, yMaxA);
            canvas.drawLine(xMin, yMaxA, xMax, yMaxB);
            canvas.drawLine(xMax, yMaxB, xMax, yMinB);
            canvas.drawLine(xMax, yMinB, xMin, yMinA);
        }
    }

    /**
     * Invoked when a mouse button is pressed on a component and then
     * dragged.  <code>MOUSE_DRAGGED</code> events will continue to be
     * delivered to the component where the drag originated until the
     * mouse button is released (regardless of whether the mouse position
     * is within the bounds of the component).
     * <p>
     * Due to platform-dependent Drag&Drop implementations,
     * <code>MOUSE_DRAGGED</code> events may not be delivered during a native
     * Drag&Drop operation.
     */
    public void mouseDragged(MouseEvent e) {
        if(intersects(e)){
            setDrawColor(Color.RED);
        }else if(e.getSource() == display){
            setDrawColor(Color.BLACK);
        }

    }

    private void sendToWAV(){
        DataOutputStream dos = null;
        try{
            dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream("seis.wav")));
        }
        catch (FileNotFoundException e){
            System.out.println("file not found");
        }
        try{
            seisWAV.writeWAV(dos, timeEvent.getTime(container.getDataSetSeismogram()));
            dos.close();
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }
}
