/**
 * StationLoader.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.chooser;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.Station;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.swing.SwingUtilities;
import org.apache.log4j.Logger;

public class StationLoader extends Thread
{

    public StationLoader(ChannelChooser chooser, NetworkAccess[] n)
    {
        this.chooser = chooser;
        this.nets = n;
        logger.debug("StationLoader constructor");
    }

    public void addStationAcceptor(StationAcceptor acceptor)
    {
        acceptors.add(acceptor);
    }

    private NetworkAccess[] nets;

    private ChannelChooser chooser;

    private LinkedList acceptors = new LinkedList();

    public void run()
    {
        List stations = new ArrayList();

        chooser.setProgressOwner(this);
        chooser.setProgressMax(this, 100);
        logger.debug("Begin StationLoader.run, there are "+nets.length+" selected networks.");
        try
        {
//          synchronized (chooser)
//          {
//              if (this.equals(chooser.getStationLoader()))
//              {
//                  chooser.clearStationsFromThread();
//                  System.out.println("StationLoader: clearing stations");
//              }
//          }
            int netProgressInc = 50 / nets.length;
            int progressValue = 10;
            chooser.setProgressValue(this, progressValue);
            for (int i=0; i<nets.length; i++)
            {
                logger.debug("Before get stations "+i);
                logger.debug("Network code = "+nets[i].get_attributes().get_code());
                Station[] newStations = nets[i].retrieve_stations();
                logger.debug("After retrieve_stations for "+nets[i].get_attributes().get_code());
                for (int j = 0; j < newStations.length; j++)
                {
                    stations.add(newStations[j]);
                }
                logger.debug("got "+newStations.length+" stations from "+
                                 nets[i].get_attributes().get_code());
                chooser.setProgressValue(this, progressValue+netProgressInc/2);

                boolean okToAdd;
                for (int j=0; j<newStations.length; j++)
                {
                    // check station name not exist, use code in that case
                    if (newStations[j].name == null ||
                        newStations[j].name.length() < 3) {
                        newStations[j].name = newStations[j].get_code();
                    }
                    Iterator it = acceptors.iterator();
                    okToAdd = true;
                    while (it.hasNext())
                    {
                        if ( ! ((StationAcceptor)it.next()).accept(newStations[j]))
                        {
                            okToAdd = false;
                        }
                    }
                    chooser.setProgressValue(this, progressValue+netProgressInc/2-
                                                 (newStations.length-j)/newStations.length);
                }
                chooser.setProgressValue(this, 100);

                logger.debug("finished adding stations for "+nets[i].get_attributes().get_code());
                //          try {
                //          sleep((int)(.01*1000));
                //          } catch (InterruptedException e) {

                //          } // end of try-catch

            } // end of for ((int i=0; i<nets.length; i++)
            logger.debug("There are "+chooser.stationNames.getSize()+" items in the station list model");
            // stationList.validate();
            //stationList.repaint();
        }
        catch (Throwable e)
        {
            GlobalExceptionHandler.handle("Unable to get stations.", e);
        } // end of try-catch   }

        //chooser.addStations((Station[])stations.toArray(new Station[stations.size()]));
        stationAdd((Station[])stations.toArray(new Station[stations.size()]));
        logger.debug("There are "+chooser.stationNames.getSize()+" items in the station list model");
    }

    /** allows subclasses to veto a station. */
    protected boolean acceptStation(Station sta)
    {
        return true;
    }

    void stationAdd(final Station[] s)
    {
        SwingUtilities.invokeLater(new Runnable()
                                   {
                    public void run()
                    {
                        chooser.addStationsFromThread(s);
                    }
                });
    }

    private static Logger logger =
        Logger.getLogger(StationLoader.class);

}

