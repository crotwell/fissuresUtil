/**
 * StationLoader.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.chooser;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.Station;
import edu.sc.seis.fissuresUtil.exceptionHandlerGUI.ExceptionHandlerGUI;
import java.util.ArrayList;
import java.util.Collection;
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
        System.out.println("There are "+nets.length+" selected networks.");
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
                System.out.println("Before get stations");
                Station[] newStations = nets[i].retrieve_stations();
                for (int j = 0; j < newStations.length; j++)
                {
                    stations.add(newStations[j]);
                }
                System.out.println("got "+newStations.length+" stations");
                chooser.setProgressValue(this, progressValue+netProgressInc/2);
                
                boolean okToAdd;
                for (int j=0; j<newStations.length; j++)
                {
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

                System.out.println("finished adding stations");
                //          try {
                //          sleep((int)(.01*1000));
                //          } catch (InterruptedException e) {
                
                //          } // end of try-catch
                
            } // end of for ((int i=0; i<nets.length; i++)
            System.out.println("There are "+chooser.stationNames.getSize()+" items in the station list model");
            // stationList.validate();
            //stationList.repaint();
        }
        catch (Exception e)
        {
            ExceptionHandlerGUI.handleException("Unable to get stations.", e);
        } // end of try-catch   }
        
        //chooser.addStations((Station[])stations.toArray(new Station[stations.size()]));
        stationAdd((Station[])stations.toArray(new Station[stations.size()]));
        System.out.println("There are "+chooser.stationNames.getSize()+" items in the station list model");
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
    
    static Logger logger =
        Logger.getLogger(StationLoader.class);
    
}

