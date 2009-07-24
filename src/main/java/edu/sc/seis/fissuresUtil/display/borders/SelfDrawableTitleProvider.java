package edu.sc.seis.fissuresUtil.display.borders;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

/**
 * @author hedx Created on Jul 6, 2005
 */
public interface SelfDrawableTitleProvider extends TitleProvider {

    public Rectangle2D getBounds(Graphics2D canvas);

    public void draw(int x, int y, Graphics2D canvas);
}
