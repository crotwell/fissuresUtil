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
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.Station;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;

public class StationLoader extends Thread {

    public StationLoader(ChannelChooser chooser, NetworkAccess[] n) {
        this.chooser = chooser;
        this.nets = n;
    }

    public void addStationAcceptor(StationAcceptor acceptor) {
        acceptors.add(acceptor);
    }

    private NetworkAccess[] nets;

    private ChannelChooser chooser;

    private LinkedList acceptors = new LinkedList();

    public void run() {
        List stations = new ArrayList();

        chooser.setProgressOwner(this);
        chooser.setProgressMax(this, 100);
        try {
            int netProgressInc = 50 / nets.length;
            int progressValue = 10;
            chooser.setProgressValue(this, progressValue);
            for (int i=0; i<nets.length; i++) {
                Station[] newStations = nets[i].retrieve_stations();
                for (int j = 0; j < newStations.length; j++) {
                    stations.add(newStations[j]);
                }
                logger.debug("got "+newStations.length+" stations from "+
                                 nets[i].get_attributes().get_code());
                chooser.setProgressValue(this, progressValue+netProgressInc/2);

                for (int j=0; j<newStations.length; j++) {
                    // check station name not exist, use code in that case
                    if (newStations[j].getName() == null ||
                        newStations[j].getName().length() < 3) {
                        newStations[j].setName(newStations[j].get_code());
                    }
                    chooser.setProgressValue(this, progressValue+netProgressInc/2-
                                                 (newStations.length-j)/newStations.length);
                }
                chooser.setProgressValue(this, 100);

            } // end of for ((int i=0; i<nets.length; i++)
        }
        catch (Throwable e) {
            GlobalExceptionHandler.handle("Unable to get stations.", e);
        } // end of try-catch   }
        stationAdd((Station[])stations.toArray(new Station[stations.size()]));
    }

    /** allows subclasses to veto a station. */
    protected boolean acceptStation(Station sta) {
        return true;
    }

    void stationAdd(final Station[] s) { chooser.addStationsFromThread(s); }

    private static Logger logger =
        LoggerFactory.getLogger(StationLoader.class);

}

