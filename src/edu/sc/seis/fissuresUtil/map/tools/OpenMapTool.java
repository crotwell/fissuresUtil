package edu.sc.seis.fissuresUtil.map.tools;

import com.bbn.openmap.MapBean;
import com.bbn.openmap.event.MapMouseListener;
import com.bbn.openmap.event.MapMouseMode;
import java.awt.Cursor;
import java.awt.event.MouseEvent;
import javax.swing.Icon;

public abstract class OpenMapTool implements MapMouseMode{

	private boolean active = false;
	private boolean isPressed = false;
	private Cursor modeCursor, pressedCursor, currentCursor;

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
	}

	/**
	 * Gets the mouse cursor recommended for use when this mouse mode
	 * is active.
	 * @return Cursor the mouse cursor recommended for use when this
	 * mouse mode is active.
	 */
	public Cursor getModeCursor() {
		return modeCursor;
	}

	public Cursor getPressedCursor(){
		return pressedCursor;
	}

	public void setModeCursor(Cursor cursor){
		modeCursor = cursor;
	}

	public void setPressedCursor(Cursor cursor){
		pressedCursor = cursor;
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
	public void mouseMoved(MouseEvent e) {
		if (!isPressed && currentCursor != modeCursor){
			setCursor(modeCursor, e);
		}
	}

	/**
	 * Invoked when the mouse enters a component.
	 */
	public void mouseEntered(MouseEvent e) {}

	/**
	 * Invoked when a mouse button has been released on a component.
	 */
	public void mouseReleased(MouseEvent e) {
		isPressed = false;
		if (currentCursor != modeCursor){
			setCursor(modeCursor, e);
		}
	}

	/**
	 * Invoked when the mouse exits a component.
	 */
	public void mouseExited(MouseEvent e) {}

	/**
	 * Invoked when a mouse button has been pressed on a component.
	 */
	public void mousePressed(MouseEvent e) {
		isPressed = true;
		if (currentCursor != pressedCursor){
			setCursor(pressedCursor, e);
		}
	}

	private void setCursor(Cursor cursor, MouseEvent me){
		if (me.getComponent() instanceof MapBean){
			MapBean mapBean = (MapBean)me.getComponent();
			mapBean.setCursor(cursor);
			currentCursor = cursor;
		}
	}


}
