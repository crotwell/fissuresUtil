package edu.sc.seis.fissuresUtil.display;

import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;

import java.lang.*;

/**
 * ChanelChooserInterface.java
 *
 *
 * Created: Thu Feb 14 12:00:22 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public interface ChannelChooserInterface {

    public ChannelId[] getChannelIds();
    
    public String[] getNetworks();
    
    public String[] getChannels();
    
    public String[] getSites();

    public String[] getStations();
  
}// ChanelChooserInterface
