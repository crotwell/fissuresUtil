/**
 * StationLoader.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.chooser;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import org.apache.log4j.Logger;
import javax.swing.SwingUtilities;
import edu.iris.Fissures.IfNetwork.Station;
import edu.sc.seis.fissuresUtil.exceptionHandlerGUI.ExceptionHandlerGUI;
import java.util.LinkedList;
import java.util.Iterator;

public class StationLoader extends Thread {

    public StationLoader(ChannelChooser chooser, NetworkAccess[] n) {
        this.chooser = chooser;
        this.nets = n;
    }

    public void addStationAcceptor(StationAcceptor acceptor) {
        acceptors.add(acceptor);
    }

    NetworkAccess[] nets;

    ChannelChooser chooser;

    LinkedList acceptors = new LinkedList();

    public void run() {
        chooser.setProgressOwner(this);
        chooser.setProgressMax(this, 100);
        logger.debug("There are "+nets.length+" selected networks.");
        try {
            synchronized (chooser) {
                if (this.equals(chooser.getStationLoader())) {
                    chooser.clearStationsFromThread();
                }
            }
            int netProgressInc = 50 / nets.length;
            int progressValue = 10;
            chooser.setProgressValue(this, progressValue);
            for (int i=0; i<nets.length; i++) {
                logger.debug("Before get stations");
                Station[] newStations = nets[i].retrieve_stations();
                logger.debug("got "+newStations.length+" stations");
                chooser.setProgressValue(this, progressValue+netProgressInc/2);

                boolean okToAdd;
                for (int j=0; j<newStations.length; j++) {
                    Iterator it = acceptors.iterator();
                    okToAdd = true;
                    while (it.hasNext()) {
                        if ( ! ((StationAcceptor)it.next()).accept(newStations[j])) {
                            okToAdd = false;
                        }
                    }
                    synchronized (chooser) {
                        if (okToAdd && this.equals(chooser.getStationLoader())) {
                            stationAdd(newStations[j]);
                            //              try {
                            //                  sleep((int)(.01*1000));
                            //              } catch (InterruptedException e) {

                            //              } // end of try-catch
                        } else {
                            // no longer the active station loader
                            return;
                        } // end of else

                    }
                    chooser.setProgressValue(this, progressValue+netProgressInc/2-
                                         (newStations.length-j)/newStations.length);
                }
                chooser.setProgressValue(this, 100);
                logger.debug("finished adding stations");
                //          try {
                //          sleep((int)(.01*1000));
                //          } catch (InterruptedException e) {

                //          } // end of try-catch

            } // end of for ((int i=0; i<nets.length; i++)
            logger.debug("There are "+chooser.stationNames.getSize()+" items in the station list model");
            // stationList.validate();
            //stationList.repaint();
        } catch (Exception e) {
            ExceptionHandlerGUI.handleException("Unable to get stations.", e);
        } // end of try-catch   }
    }

    /** allows subclasses to veto a station. */
    protected boolean acceptStation(Station sta) {
        return true;
    }

    void stationAdd(final Station s) {
        SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        chooser.addStationFromThread(s);
                    }
                });
    }

    static Logger logger =
        Logger.getLogger(StationLoader.class);

}

