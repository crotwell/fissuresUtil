package edu.sc.seis.fissuresUtil.map.layers;

/**
 * MouseAdapterLayer extends OpenMap's layer and implements most of
 * MapMouseListener to make it easier for subclasses to get only the mouse
 * events they're interested in.
 *
 * @author Created by Charlie Groves
 */

import java.awt.event.MouseEvent;
import javax.swing.JPopupMenu;
import com.bbn.openmap.Layer;
import com.bbn.openmap.event.MapMouseListener;
import com.bbn.openmap.event.NavMouseMode;
import com.bbn.openmap.event.NullMouseMode;
import com.bbn.openmap.event.ProjectionEvent;
import com.bbn.openmap.event.SelectMouseMode;

public abstract class MouseAdapterLayer extends Layer implements MapMouseListener{
	protected JPopupMenu currentPopup;
	
    public void mouseMoved() {}
	
    public abstract void projectionChanged(ProjectionEvent e);
	
    /**
	 * Return a list of the modes that are interesting to the
	 * MapMouseListener.  The source MouseEvents will only get sent to
	 * the MapMouseListener if the mode is set to one that the
	 * listener is interested in.
	 * Layers interested in receiving events should register for
	 * receiving events in "select" mode:
	 * <code>
	 * <pre>
	 *  return new String[] {
	 *      SelectMouseMode.modeID
	 *  };
	 * </pre>
	 * <code>
	 * @return String[] of modeID's
	 * @see NavMouseMode#modeID
	 * @see SelectMouseMode#modeID
	 * @see NullMouseMode#modeID
	 */
    public abstract String[] getMouseModeServiceList();
	
    public MapMouseListener getMapMouseListener(){
		return this;
    }
	
    public void mouseEntered(MouseEvent e) {}
	
    public boolean mouseDragged(MouseEvent e) { return false;   }
	
    public boolean mouseReleased(MouseEvent e) {return false;}
	
	public boolean mouseClicked(MouseEvent e) { return false; }
	
	public void mouseExited(MouseEvent e) {  }
	
    public boolean mouseMoved(MouseEvent e) { return false;  }
	
	public boolean mousePressed(MouseEvent e) {return false;  }
	
	protected void maybeKillCurrentPopup(){
		if (currentPopup != null){
			currentPopup.setVisible(false);
			currentPopup = null;
		}
	}
}

