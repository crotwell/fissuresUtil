/**
 * NetworkDataListener.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.chooser;

import java.util.EventListener;

public interface NetworkDataListener extends EventListener{

    public void networkDataChanged(StationDataEvent s);

    public void networkDataCleared();
}

