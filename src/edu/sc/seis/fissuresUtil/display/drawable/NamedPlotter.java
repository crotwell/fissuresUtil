package edu.sc.seis.fissuresUtil.display.drawable;

import java.awt.Graphics2D;
/**
 * NamedPlotter.java
 *
 *
 * Created: Mon Nov 11 17:09:15 2002
 *
 * @author <a href="mailto:">Charlie Groves</a>
 * @version
 */

public interface NamedPlotter extends Plotter{

    public boolean drawName(Graphics2D canvas, int xPosition, int yPosition);

}// NamedPlotter
