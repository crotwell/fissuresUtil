package edu.sc.seis.fissuresUtil.display.drawable;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
/**
 * NamedPlotter.java
 *
 *
 * Created: Mon Nov 11 17:09:15 2002
 *
 * @author <a href="mailto:">Charlie Groves</a>
 * @version
 */

public interface NamedDrawable extends Drawable{

    /**
     *@returns a rectangle around the string drawn
     */
    public Rectangle2D drawName(Graphics2D canvas, int xPosition, int yPosition);

}// NamedPlotter
