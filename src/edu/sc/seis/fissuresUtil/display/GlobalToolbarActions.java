package edu.sc.seis.fissuresUtil.display;

import java.awt.event.MouseEvent;

/**
 * GlobalToolbarActions.java
 *
 *
 * Created: Thu Jun 27 12:58:05 2002
 *
 * @author <a href="mailto:">Charlie Groves</a>
 * @version
 */

public interface GlobalToolbarActions {
    public void drag(MouseEvent meOne, MouseEvent meTwo);

    public void mouseMoved(MouseEvent me);

    public void mouseReleased(MouseEvent me);

    public void remove(MouseEvent me);

    public void removeAll(MouseEvent me);

    public void zoomIn(MouseEvent me);

    public void zoomIn(MouseEvent meOne, MouseEvent meTwo);

    public void zoomOut(MouseEvent me);
}// GlobalToolbarActions
