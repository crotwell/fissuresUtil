package edu.sc.seis.fissuresUtil.display;

import java.awt.Dimension;
import java.awt.Graphics2D;

public interface Graphics2DRenderer {
    
    public void renderToGraphics(Graphics2D g);

    public void renderToGraphics(Graphics2D g, Dimension size);
}
