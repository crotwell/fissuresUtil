package edu.sc.seis.fissuresUtil.display;

import java.util.EventListener;

/**
 * ControlChangeListener.java
 *
 *
 * Created: Mon Jun  3 10:12:08 2002
 *
 * @author Charlie Groves
 * @version
 */

public interface ControlChangeListener extends EventListener {
    public void setControlBehavior(String type);
    
}// ControlChangeListener
