package edu.sc.seis.fissuresUtil.map.tools;

import com.bbn.openmap.event.MapMouseMode;
import java.awt.event.MouseAdapter;
import javax.swing.Icon;
import java.awt.Cursor;
import com.bbn.openmap.event.MapMouseListener;
import java.awt.event.MouseEvent;

public abstract class OpenMapTool implements MapMouseMode{

	private boolean active = false;

	public void setActive(boolean isActive){
		active = isActive;
		System.out.println(this + ".active has been set to " + active);
	}

	public boolean isActive(){
		return active;
	}

	/**
	 * Gets the Icon to represent the Mouse Mode in a GUI.
	 */
	public Icon getGUIIcon() {
		return null;
	};

	/**
	 * Gets the mouse cursor recommended for use when this mouse mode
	 * is active.
	 * @return Cursor the mouse cursor recommended for use when this
	 * mouse mode is active.
	 */
	public Cursor getModeCursor() {
		return null;
	}

	/**
	 * Add a MapMouseListener to the MouseMode.
	 * @param l the MapMouseListener to add.
	 */
	public void addMapMouseListener(MapMouseListener l) {}

	/**
	 * Remove all MapMouseListeners from the mode.
	 */
	public void removeAllMapMouseListeners() {}

	/**
	 * Remove a MapMouseListener from the MouseMode.
	 * @param l the MapMouseListener to remove.
	 */
	public void removeMapMouseListener(MapMouseListener l) {}

	/**
	 * Invoked when the mouse button has been clicked (pressed
	 * and released) on a component.
	 */
	public void mouseClicked(MouseEvent e) {}

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
	public void mouseDragged(MouseEvent e) {}

	/**
	 * Invoked when the mouse cursor has been moved onto a component
	 * but no buttons have been pushed.
	 */
	public void mouseMoved(MouseEvent e) {}

	/**
	 * Invoked when the mouse enters a component.
	 */
	public void mouseEntered(MouseEvent e) {}

	/**
	 * Invoked when a mouse button has been released on a component.
	 */
	public void mouseReleased(MouseEvent e) {}

	/**
	 * Invoked when the mouse exits a component.
	 */
	public void mouseExited(MouseEvent e) {}

	/**
	 * Invoked when a mouse button has been pressed on a component.
	 */
	public void mousePressed(MouseEvent e) {}


}
