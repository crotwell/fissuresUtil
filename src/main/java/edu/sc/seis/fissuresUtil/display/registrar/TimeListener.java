package edu.sc.seis.fissuresUtil.display.registrar;

import java.util.EventListener;

/**
 * TimeListener.java
 *
 *
 * Created: Thu Sep  5 14:22:38 2002
 *
 * @author <a href="mailto:">Charlie Groves</a>
 * @version
 */

public interface TimeListener extends EventListener{

    public void updateTime(TimeEvent event);
    
}// TimeListener
