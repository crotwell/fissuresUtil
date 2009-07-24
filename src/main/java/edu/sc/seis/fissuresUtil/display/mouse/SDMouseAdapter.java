package edu.sc.seis.fissuresUtil.display.mouse;

import java.awt.event.MouseAdapter;

public abstract class SDMouseAdapter extends MouseAdapter
    implements SDMouseListener, SDMouseMotionListener{
    public void mouseMoved(SDMouseEvent me){}
    public void mouseDragged(SDMouseEvent me){}
    public void mouseClicked(SDMouseEvent e){}
    public void mousePressed(SDMouseEvent e){}
    public void mouseReleased(SDMouseEvent e){}
    public void mouseEntered(SDMouseEvent e){}
    public void mouseExited(SDMouseEvent e){}
}

