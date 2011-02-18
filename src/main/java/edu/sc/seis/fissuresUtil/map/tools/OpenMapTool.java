package edu.sc.seis.fissuresUtil.map.tools;

import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.event.MouseEvent;

import javax.swing.Icon;

import com.bbn.openmap.MapBean;
import com.bbn.openmap.event.MapMouseListener;
import com.bbn.openmap.event.MapMouseMode;
import com.bbn.openmap.event.MapMouseSupport;

public abstract class OpenMapTool implements MapMouseMode {

    private boolean active = false;

    private boolean isPressed = false;

    private Cursor modeCursor, pressedCursor, currentCursor;

    public void setActive(boolean isActive) {
        active = isActive;
    }

    public boolean isActive() {
        return active;
    }

    /**
     * Gets the Icon to represent the Mouse Mode in a GUI.
     */
    public Icon getGUIIcon() {
        return null;
    }

    public Cursor getModeCursor() {
        return modeCursor;
    }

    public Cursor getPressedCursor() {
        return pressedCursor;
    }

    public void setModeCursor(Cursor cursor) {
        modeCursor = cursor;
    }

    public void setPressedCursor(Cursor cursor) {
        pressedCursor = cursor;
    }

    /**
     * Add a MapMouseListener to the MouseMode.
     * 
     * @param l
     *            the MapMouseListener to add.
     */
    public void addMapMouseListener(MapMouseListener l) {}

    /**
     * Remove all MapMouseListeners from the mode.
     */
    public void removeAllMapMouseListeners() {}

    /**
     * Remove a MapMouseListener from the MouseMode.
     * 
     * @param l
     *            the MapMouseListener to remove.
     */
    public void removeMapMouseListener(MapMouseListener l) {}

    /**
     * Invoked when the mouse button has been clicked (pressed and released) on
     * a component.
     */
    public void mouseClicked(MouseEvent e) {}

    /**
     * Invoked when a mouse button is pressed on a component and then dragged.
     * <code>MOUSE_DRAGGED</code> events will continue to be delivered to the
     * component where the drag originated until the mouse button is released
     * (regardless of whether the mouse position is within the bounds of the
     * component).
     * <p>
     * Due to platform-dependent Drag&Drop implementations,
     * <code>MOUSE_DRAGGED</code> events may not be delivered during a native
     * Drag&Drop operation.
     */
    public void mouseDragged(MouseEvent e) {}

    /**
     * Invoked when the mouse cursor has been moved onto a component but no
     * buttons have been pushed.
     */
    public void mouseMoved(MouseEvent e) {
        if(!isPressed) {
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
        setCursor(modeCursor, e);
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
        setCursor(pressedCursor, e);
    }

    private void setCursor(Cursor cursor, MouseEvent me) {
        if(currentCursor != cursor && me.getComponent() instanceof MapBean) {
            MapBean mapBean = (MapBean)me.getComponent();
            mapBean.setCursor(cursor);
            currentCursor = cursor;
        }
    }

    /**
     * Lets the MouseDelegator know if the MapMouseMode should be visible in the
     * GUI, in order to create certain mouse modes that may be controlled by
     * other tools.
     */
    public boolean isVisible() {
        // TODO
        return false;
    }

    /**
     * Request to have the MapMouseMode act as a proxy for a MapMouseMode that
     * wants to remain hidden. Can be useful for directing events to one object.
     * With this call, no events will be forwared to the proxy's target.
     * 
     * @param mmm
     *            the hidden MapMouseMode for this MapMouseMode to send events
     *            to.
     * @return true if the proxy setup (essentially a lock) is successful, false
     *         if the proxy is already set up for another listener.
     */
    public boolean actAsProxyFor(MapMouseMode mmm) {
        // TODO
        return false;
    }

    /**
     * Request to have the MapMouseMode act as a proxy for a MapMouseMode that
     * wants to remain hidden. Can be useful for directing events to one object.
     * 
     * @param mmm
     *            the hidden MapMouseMode for this MapMouseMode to send events
     *            to.
     * @param pdm
     *            the proxy distribution mask to use, which lets this proxy
     *            notify its targets of events.
     * @return true if the proxy setup (essentially a lock) is successful, false
     *         if the proxy is already set up for another listener.
     */
    public boolean actAsProxyFor(MapMouseMode mmm, int pdm) {
        // TODO
        return false;
    }

    /**
     * Can check if the MapMouseMode is acting as a proxy for a MapMouseMode.
     */
    public boolean isProxyFor(MapMouseMode mmm) {
        // TODO
        return false;
    }

    /**
     * Release the proxy lock on the MapMouseMode.
     */
    public void releaseProxy() {
    // TODO
    }

    /**
     * Set the mask that dictates which events get sent to this support object's
     * targets even if the parent mouse mode is acting as a proxy.
     * 
     * @see MapMouseSupport for definitions of mask bits.
     */
    public void setProxyDistributionMask(int mask) {
    // TODO
    }

    /**
     * Get the mask that dictates which events get sent to this support object's
     * targets even if the parent mouse mode is acting as a proxy.
     * 
     * @see MapMouseSupport for definitions of mask bits.
     */
    public int getProxyDistributionMask() {
        // TODO
        return 0;
    }

    public void listenerPaint(Graphics graphics) {
    // TODO
    }
}
