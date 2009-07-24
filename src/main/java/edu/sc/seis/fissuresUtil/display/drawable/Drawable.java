package edu.sc.seis.fissuresUtil.display.drawable;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import edu.sc.seis.fissuresUtil.display.registrar.AmpEvent;
import edu.sc.seis.fissuresUtil.display.registrar.TimeEvent;

/**
 * Drawables are objects to be put in the main display of a SeismogramDisplay.
 * Created: Fri Jun 7 10:27:49 2002
 * 
 * @author <a href="mailto:">Charlie Groves </a>
 * @version 0.1
 */
public interface Drawable {

    public void draw(Graphics2D canvas,
                     Dimension size,
                     TimeEvent currentTime,
                     AmpEvent currentAmp);

    public void setVisibility(boolean visible);

    public Color getColor();

    public void setColor(Color c);
}// Plotter
