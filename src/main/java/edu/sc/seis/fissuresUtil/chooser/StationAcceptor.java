/**
 * StationVetoer.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.chooser;
import edu.iris.Fissures.IfNetwork.Station;



public interface StationAcceptor {

    public boolean accept(Station station);

}

