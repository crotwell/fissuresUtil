package edu.sc.seis.fissuresUtil.display.registrar;


import java.util.EventListener;

/**
 * AmpListener.java
 *
 *
 * Created: Thu Sep  5 14:06:38 2002
 *
 * @author <a href="mailto:">Charlie Groves</a>
 * @version
 */

public interface AmpListener extends EventListener{
    public void updateAmp(AmpEvent event);

}// AmpListener
