package edu.sc.seis.fissuresUtil.display.mouse;

import java.util.EventListener;



public interface SDMouseListener extends EventListener{
    public void mouseClicked(SDMouseEvent e);
    public void mousePressed(SDMouseEvent e);
    public void mouseReleased(SDMouseEvent e);
    public void mouseEntered(SDMouseEvent e);
    public void mouseExited(SDMouseEvent e);
}
