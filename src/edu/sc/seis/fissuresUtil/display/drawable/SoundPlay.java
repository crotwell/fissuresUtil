package edu.sc.seis.fissuresUtil.display.drawable;

import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.display.BasicSeismogramDisplay;
import edu.sc.seis.fissuresUtil.display.DisplayUtils;
import edu.sc.seis.fissuresUtil.display.SeismogramContainer;
import edu.sc.seis.fissuresUtil.display.SeismogramDisplay;
import edu.sc.seis.fissuresUtil.display.registrar.AmpEvent;
import edu.sc.seis.fissuresUtil.display.registrar.TimeEvent;
import edu.sc.seis.fissuresUtil.exceptionHandlerGUI.GlobalExceptionHandler;
import edu.sc.seis.fissuresUtil.sound.FissuresToWAV;
import edu.sc.seis.fissuresUtil.sound.PlayEvent;
import edu.sc.seis.fissuresUtil.sound.PlayEventListener;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.swing.Timer;



public class SoundPlay extends MouseAdapter implements Drawable, MouseMotionListener,
    PlayEventListener{

    private Color drawColor = Color.BLACK;
    private int x2, x3, x1, x0, yMaxA = 9, yMaxB = 11, yMinA = 6, yMinB = 4, playCursor = 0;
    private SeismogramDisplay display;
    private boolean visible = true;
    private SeismogramContainer container;
    private TimeEvent timeEvent;
    private FissuresToWAV seisWAV;
    private PlayEvent playEvent;

    private TimeInterval timeInterval;
    private Timer timer;
    private double totalCount;
    private int currentCount;
    private double positionMultiplier;
    private MicroSecondDate clickTime;

    public SoundPlay(SeismogramDisplay display, SeismogramContainer container){
        this.display = display;
        this.container = container;
        seisWAV = new FissuresToWAV(container, 1200);
        seisWAV.addPlayEventListener(this);
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

    public Color getColor(){ return drawColor; }

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
            if(clickX >= x0 && clickX <= x3+1 &&
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
        if(visible && !BasicSeismogramDisplay.PRINTING){
            double sizeOfDisplay = size.getWidth();
            x0 = (int)sizeOfDisplay - 20;
            x1 = (int)sizeOfDisplay - 15;
            x2 = (int)sizeOfDisplay - 11;
            x3 = (int)sizeOfDisplay - 7;

            timeEvent = currentTime;
            canvas.setColor(drawColor);
            canvas.setStroke(DisplayUtils.TWO_PIXEL_STROKE);
            int yMid = (yMinA + yMaxA + yMinB + yMaxB)/4;
            canvas.drawLine(x0, yMinA, x0, yMaxA);
            canvas.drawLine(x0, yMaxA, x1, yMaxB);
            canvas.drawLine(x1, yMaxB, x1, yMinB);
            canvas.drawLine(x1, yMinB, x0, yMinA);
            canvas.drawLine(x2, yMinB, x2 + 1, yMid);
            canvas.drawLine(x2 + 1, yMid, x2, yMaxB);
            canvas.drawLine(x3, yMinB, x3 + 1, yMid);
            canvas.drawLine(x3 + 1, yMid, x3, yMaxB);

            if (playEvent != null && playEvent.getClip().isRunning()){
                long elapsedTime = playEvent.getClip().getMicrosecondPosition();
                long clipLength = playEvent.getClip().getMicrosecondLength();
                canvas.setColor(Color.RED);
                canvas.setStroke(DisplayUtils.TWO_PIXEL_STROKE);
                playCursor = (int)(((double)elapsedTime/(double)clipLength)*sizeOfDisplay);
                canvas.drawLine(playCursor, 0, playCursor, display.getHeight());
                display.repaint();
            }
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
            GlobalExceptionHandler.handleStatic("file not found", e);
        }
        try{
            seisWAV.writeWAV(dos, timeEvent.getTime(container.getDataSetSeismogram()));
            dos.close();
        }
        catch(IOException e){
            GlobalExceptionHandler.handleStatic(e);
        }
    }

    public void eventPlayed(PlayEvent e){
        playEvent = e;
        timeInterval = e.getTimeInterval();
        display.repaint();
    }
}

