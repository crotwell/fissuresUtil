/**
 * StationLoader.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.chooser;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.iris.Fissures.IfNetwork.Station;
import edu.iris.Fissures.network.StationImpl;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;

public class StationLoader extends Thread {

    public StationLoader(ChannelChooser chooser, NetworkFromSource[] nets2) {
        this.chooser = chooser;
        this.nets = nets2;
    }

    public void addStationAcceptor(StationAcceptor acceptor) {
        acceptors.add(acceptor);
    }

    private NetworkFromSource[] nets;

    private ChannelChooser chooser;

    private LinkedList acceptors = new LinkedList();

    public void run() {
        List<StationImpl> stations = new ArrayList();

        chooser.setProgressOwner(this);
        chooser.setProgressMax(this, 100);
        try {
            int netProgressInc = 50 / nets.length;
            int progressValue = 10;
            chooser.setProgressValue(this, progressValue);
            for (int i=0; i<nets.length; i++) {
                List<StationImpl> newStations = nets[i].getSource().getStations(nets[i].getNetAttr());
                stations.addAll(newStations);
                logger.debug("got "+newStations.size()+" stations from "+
                                 nets[i].getNetAttr().get_code());
                chooser.setProgressValue(this, progressValue+netProgressInc/2);
                for (int j = 0; j < newStations.size(); j++) {
                    // check station name not exist, use code in that case
                    StationImpl newStationImpl = newStations.get(j);
                    if (newStationImpl.getName() == null ||
                            newStationImpl.getName().length() < 3) {
                        newStationImpl.setName(newStationImpl.get_code());
                    }
                    chooser.setProgressValue(this, progressValue+netProgressInc/2-
                                                 (newStations.size()-j)/newStations.size());
                }
                chooser.setProgressValue(this, 100);

            } // end of for ((int i=0; i<nets.length; i++)
        }
        catch (Throwable e) {
            GlobalExceptionHandler.handle("Unable to get stations.", e);
        } // end of try-catch   }
        stationAdd(stations);
    }

    /** allows subclasses to veto a station. */
    protected boolean acceptStation(Station sta) {
        return true;
    }

    void stationAdd(final List<StationImpl>  s) { chooser.addStationsFromThread(s); }

    private static Logger logger =
        LoggerFactory.getLogger(StationLoader.class);

}

