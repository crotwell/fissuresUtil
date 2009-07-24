package edu.sc.seis.fissuresUtil.display.mouse;

import java.util.EventListener;

public interface SDMouseMotionListener extends EventListener{
    public void mouseMoved(SDMouseEvent me);
    public void mouseDragged(SDMouseEvent me);
}

