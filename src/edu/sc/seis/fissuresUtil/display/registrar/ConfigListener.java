package edu.sc.seis.fissuresUtil.display.registrar;


/**
 * ConfigListener.java
 *
 *
 * Created: Thu Aug 29 13:45:11 2002
 *
 * @author <a href="mailto:">Charlie Groves</a>
 * @version
 */

public interface ConfigListener extends AmpListener, TimeListener{
    public void update(ConfigEvent event);

}// ConfigListener
