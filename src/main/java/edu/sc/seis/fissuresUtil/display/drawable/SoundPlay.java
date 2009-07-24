package edu.sc.seis.fissuresUtil.display.drawable;

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
import edu.iris.Fissures.FissuresException;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.sc.seis.fissuresUtil.display.DisplayUtils;
import edu.sc.seis.fissuresUtil.display.SeismogramContainer;
import edu.sc.seis.fissuresUtil.display.SeismogramDisplay;
import edu.sc.seis.fissuresUtil.display.registrar.AmpEvent;
import edu.sc.seis.fissuresUtil.display.registrar.TimeEvent;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.fissuresUtil.sound.FissuresToWAV;
import edu.sc.seis.fissuresUtil.sound.PlayEvent;
import edu.sc.seis.fissuresUtil.sound.PlayEventListener;

public class SoundPlay extends MouseAdapter implements Drawable,
        MouseMotionListener, PlayEventListener {

    private Color drawColor = Color.BLACK;

    private int x2, x3, x1, x0, yMaxA = 9, yMaxB = 11, yMinA = 6, yMinB = 4,
            playCursor = 0;

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

    public SoundPlay(SeismogramDisplay display, SeismogramContainer container) {
        this.display = display;
        this.container = container;
        seisWAV = new FissuresToWAV(container, 1200);
        seisWAV.addPlayEventListener(this);
        display.addMouseListener(this);
        display.addMouseMotionListener(this);
    }

    public void setVisibility(boolean b) {
        visible = b;
    }

    public Color getColor() {
        return drawColor;
    }

    public void setColor(Color c) {
        throw new UnsupportedOperationException("These colors are controlled by mousing, you can't set them");
    }

    /**
     * Invoked when the mouse cursor has been moved onto a component but no
     * buttons have been pushed.
     */
    public void mouseMoved(MouseEvent e) {
        if(intersects(e)) {
            setDrawColor(Color.RED);
        } else {
            setDrawColor(Color.BLACK);
        }
    }

    public void mouseClicked(MouseEvent e) {
        if(intersects(e)) {
            seisWAV.play(timeEvent.getTime(container.getDataSetSeismogram()));
        }
    }

    private boolean intersects(MouseEvent e) {
        if(e.getSource() == display) {
            int clickX = e.getX() - display.getInsets().left;
            int clickY = e.getY() - display.getInsets().top;
            if(clickX >= x0 && clickX <= x3 + 1 && clickY >= yMinB
                    && clickY <= yMaxB) { return true; }
        }
        return false;
    }

    private void setDrawColor(Color newColor) {
        Color prevColor = drawColor;
        drawColor = newColor;
        if(prevColor != newColor) {
            display.repaint();
        }
    }

    public void draw(Graphics2D canvas,
                     Dimension size,
                     TimeEvent currentTime,
                     AmpEvent currentAmp) {
        if(visible && !SeismogramDisplay.PRINTING) {
            double sizeOfDisplay = size.getWidth();
            x0 = (int)sizeOfDisplay - 20;
            x1 = (int)sizeOfDisplay - 15;
            x2 = (int)sizeOfDisplay - 11;
            x3 = (int)sizeOfDisplay - 7;
            timeEvent = currentTime;
            canvas.setColor(drawColor);
            canvas.setStroke(DisplayUtils.TWO_PIXEL_STROKE);
            int yMid = (yMinA + yMaxA + yMinB + yMaxB) / 4;
            canvas.drawLine(x0, yMinA, x0, yMaxA);
            canvas.drawLine(x0, yMaxA, x1, yMaxB);
            canvas.drawLine(x1, yMaxB, x1, yMinB);
            canvas.drawLine(x1, yMinB, x0, yMinA);
            canvas.drawLine(x2, yMinB, x2 + 1, yMid);
            canvas.drawLine(x2 + 1, yMid, x2, yMaxB);
            canvas.drawLine(x3, yMinB, x3 + 1, yMid);
            canvas.drawLine(x3 + 1, yMid, x3, yMaxB);
            if(playEvent != null && playEvent.getClip().isRunning()) {
                long elapsedTime = playEvent.getClip().getMicrosecondPosition();
                long clipLength = playEvent.getClip().getMicrosecondLength();
                canvas.setColor(Color.RED);
                canvas.setStroke(DisplayUtils.TWO_PIXEL_STROKE);
                playCursor = (int)(((double)elapsedTime / (double)clipLength) * sizeOfDisplay);
                canvas.drawLine(playCursor, 0, playCursor, display.getHeight());
                display.repaint();
            }
        }
    }

    public void mouseDragged(MouseEvent e) {
        if(intersects(e)) {
            setDrawColor(Color.RED);
        } else if(e.getSource() == display) {
            setDrawColor(Color.BLACK);
        }
    }

    private void sendToWAV() throws FissuresException {
        DataOutputStream dos = null;
        try {
            dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream("seis.wav")));
        } catch(FileNotFoundException e) {
            GlobalExceptionHandler.handle("file not found", e);
        }
        try {
            seisWAV.writeWAV(dos,
                             timeEvent.getTime(container.getDataSetSeismogram()));
            dos.close();
        } catch(IOException e) {
            GlobalExceptionHandler.handle(e);
        }
    }

    public void eventPlayed(PlayEvent e) {
        playEvent = e;
        timeInterval = e.getTimeInterval();
        display.repaint();
    }
}