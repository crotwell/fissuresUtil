/**
 * AvailableDataStationRenderer.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.chooser;

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JList;
import org.apache.log4j.Logger;
import edu.iris.Fissures.TimeRange;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.Station;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.network.NetworkIdUtil;
import edu.iris.Fissures.network.StationIdUtil;
import edu.sc.seis.fissuresUtil.cache.AbstractJob;
import edu.sc.seis.fissuresUtil.cache.DataCenterRouter;
import edu.sc.seis.fissuresUtil.cache.JobTracker;
import edu.sc.seis.fissuresUtil.cache.WorkerThreadPool;

public class AvailableDataStationRenderer extends NameListCellRenderer {

    public AvailableDataStationRenderer(boolean useNames){
        super(useNames);
    }

    public AvailableDataStationRenderer(boolean useNames,
                                        boolean useCodes,
                                        boolean codeIsFirst){
        super(useNames, useCodes, codeIsFirst);
    }

    public AvailableDataStationRenderer(boolean useNames,
                                        DataCenterRouter dc,
                                        ChannelChooser channelChooser){
        super(useNames);
        this.dc = dc;
        this.channelChooser = channelChooser;
        startThread();
    }

    public AvailableDataStationRenderer(boolean useNames,
                                        boolean useCodes,
                                        boolean codeIsFirst,
                                        DataCenterRouter dc,
                                        ChannelChooser channelChooser){
        super(useNames, useCodes, codeIsFirst);
        this.dc = dc;
        this.channelChooser = channelChooser;
        startThread();
    }

    public void setOrigin(Origin origin) {
        this.origin = origin;
        recheckNetworks();
    }

    protected void startThread() {
        // first start the network checker as it may be more efficient
        // in case networks are already loaded
        recheckNetworks();
        channelChooser.addNetworkDataListener(new NetworkDataListener() {
                    public void networkDataCleared() { }

                    public void networkDataChanged(NetworkDataEvent s) {
                        startNetworkChecker(s.getNetwork());
                    }
                });
    }

    protected void recheckNetworks() {
        stationsUpNow.clear();
        NetworkAccess[] nets = channelChooser.getNetworks();
        for (int i = 0; i < nets.length; i++) {
            startNetworkChecker(nets[i]);
        }
    }

    private void startNetworkChecker(NetworkAccess na){
        NetworkChecker checker = null;
        Iterator it = netCheckers.iterator();
        while(it.hasNext()){
            NetworkChecker cur = (NetworkChecker)it.next();
            if(cur.getNetwork().equals(na)){
                checker = cur;
                break;
            }
        }
        if(checker == null){
            checker = new NetworkChecker(na);
            netCheckers.add(checker);
        }
        netCheckerPool.invokeLater(checker);
    }

    protected synchronized void addAvailableStationDataListener(AvailableStationDataListener listener){
        listenerList.add(AvailableStationDataListener.class, listener);
        Set keySet = stationsUpNow.keySet();
        Iterator it = keySet.iterator();
        while (it.hasNext()){
            Station station = (Station)it.next();
            Object obj = stationsUpNow.get(station);
            if (!(obj instanceof Station)){
                int status = ((Boolean)obj).booleanValue()
                    ? AvailableStationDataEvent.UP
                    : AvailableStationDataEvent.DOWN;
                listener.stationAvailabiltyChanged(new AvailableStationDataEvent(station,
                                                                                 status));
            }
        }
    }

    protected synchronized void fireStationAvailabilityChanged(Station sta, boolean isUp){
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        AvailableStationDataEvent fooEvent = null;
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==AvailableStationDataListener.class) {
                // Lazily create the event:
                if (fooEvent == null) {
                    int status = isUp
                        ? AvailableStationDataEvent.UP
                        : AvailableStationDataEvent.DOWN;
                    fooEvent = new AvailableStationDataEvent(sta, status); }
                ((AvailableStationDataListener)listeners[i+1]).stationAvailabiltyChanged(fooEvent);
            }
        }
    }

    public void setJList(JList jlist) {
        this.jlist = jlist;
    }

    public Component getListCellRendererComponent(JList list,
                                                  Object value,
                                                  int index,
                                                  boolean isSelected,
                                                  boolean cellHasFocus) {

        Component c = super.getListCellRendererComponent(list,
                                                         value,
                                                         index,
                                                         isSelected,
                                                         cellHasFocus);
        Station station = (Station)value;
        if (stationsUpNow.get(station) != null &&
            stationsUpNow.get(station) instanceof Boolean) {

            Boolean val = (Boolean)stationsUpNow.get(station);
            //            logger.debug("Station available found "+
            //                             StationIdUtil.toString(station.get_id())+ val);
            if (val.booleanValue()) {
                c.setForeground(STATION_AVAILABLE);
            } else {
                c.setForeground(STATION_UNAVAILABLE);
            }
        } else {
            //            logger.debug("Station available NOT found "+
            //                             StationIdUtil.toString(station.get_id()));
            if (stationsUpNow.get(station) == null) {
                // only add if we are not already "working on it"
                if (stationsToCheck.contains(station)) {
                    increasePriority(station);
                } else {
                    addToCheck(station);
                }
            }
        }
        return c;
    }

    public RequestFilter[] createFakeRequest(Channel[] chan) {
        MicroSecondDate now = ClockUtil.now();
        Channel[] nowChan = BestChannelUtil.pruneChannels(chan, now);
        return createFakeRequestBHZ(nowChan);
    }


    /** Creates a request filter with several common channel ids to try
     *  and check for a station existing in a data center.
     */
    public RequestFilter[] createFakeRequestBHZ(Channel[] chan) {
        MicroSecondDate now = ClockUtil.now();
        TimeRange range = new TimeRange(now.subtract(TEN_MINUTES).getFissuresTime(),
                                        now.getFissuresTime());
        RequestFilter[] request = new RequestFilter[0];
        for (int i=0; i<chan.length; i++) {
            if (chan[i].get_code().equals("BHZ") ||
                chan[i].get_code().equals("LHZ") ||
                chan[i].get_code().equals("SHZ")) {
                RequestFilter[]  tmp = new RequestFilter[request.length+1];
                System.arraycopy(request, 0, tmp, 0, request.length);
                tmp[tmp.length-1] = new RequestFilter(chan[i].get_id(),
                                                      range.start_time,
                                                      range.end_time);
                request = tmp;
            }
        }
        return request;
    }

    protected synchronized void increasePriority(Station station) {
        if (stationsToCheck.contains(station)) {
            // move to front of array
            stationsToCheck.remove(station);
            stationsToCheck.addFirst(station);
        }
    }

    protected synchronized void addToCheck(Station station) {
        stationsToCheck.addFirst(station);
        notifyAll();
    }

    protected synchronized Station getToCheck() throws InterruptedException {
        while (stationsToCheck.isEmpty()) {
            wait();
        }
        Station sta =  (Station)stationsToCheck.removeFirst();
        stationsUpNow.put(sta, sta); // put station in as placeholder, means I'm working on it
        return sta;
    }

    protected synchronized void finishedCheck(Station station, boolean val) {
        if (val) {
            stationsUpNow.put(station, Boolean.TRUE);
        } else {
            stationsUpNow.put(station, Boolean.FALSE);
        }
        fireStationAvailabilityChanged(station, val);
        stationsToCheck.remove(station);
        if (jlist != null) {
            jlist.repaint();
        }
    }

    protected synchronized void finishedError(Station station) {
        stationsUpNow.remove(station);
        stationsToCheck.remove(station);
        logger.warn("Seismogram server not found for "+
                        StationIdUtil.toString(station.get_id()));
    }

    protected synchronized void finishedError(Station station, Throwable e) {
        stationsUpNow.remove(station);
        stationsToCheck.remove(station);
        logger.error("Problem doing available data for "+
                         StationIdUtil.toString(station.get_id()), e);
    }

    class NetworkChecker extends AbstractJob{
        NetworkChecker(NetworkAccess net) {
            super(net.get_attributes().get_code()+" Available Data");
            this.net = net;
            JobTracker.getTracker().add(this);
        }

        public NetworkAccess getNetwork(){ return net; }

        NetworkAccess net;

        boolean quitThread;

        int consecutiveFailures = 0;

        public void runJob() {
            int maxFail = 5;
            setFinished(false);
            consecutiveFailures = 0;
            quitThread = false;
            while (!quitThread) {
                try {
                    checkNet();
                } catch (RuntimeException e) {
                    if(consecutiveFailures > maxFail) {
                        throw e;
                    }
                    try {
                        setStatus("Waiting "+ (2*consecutiveFailures)+" seconds before making another attempt");
                        consecutiveFailures++;
                        Thread.sleep(2000 * consecutiveFailures);
                        if(consecutiveFailures > maxFail) quitThread = true;
                    }catch(InterruptedException interrupted) {
                    }

                }
            }
            logger.debug("network checker thread quitting");
            setFinished();
            if(consecutiveFailures > maxFail) setStatus("Failed");
        }

        void checkNet() {
            logger.debug("checking "+net.get_attributes().get_code());
            TimeRange range;
            if (consecutiveFailures==0) {
                setStatus("Creating available data request");
            } else {
                setStatus("Creating available data request after " + consecutiveFailures + " failures");
            }
            if (origin == null) {
                // use current time
                MicroSecondDate now = ClockUtil.now();
                range = new TimeRange(now.subtract(TEN_MINUTES).getFissuresTime(),
                                      now.getFissuresTime());
            } else {
                // use origin time offset
                MicroSecondDate oTime = new MicroSecondDate(origin.origin_time);
                range = new TimeRange(oTime.getFissuresTime(),
                                      oTime.add(TEN_MINUTES).getFissuresTime());
            }
            String[] chanCodes = channelChooser.getSelectedChanCodes();
            RequestFilter[] request = new RequestFilter[chanCodes.length];
            for (int i = 0; i < request.length; i++) {
                if (chanCodes[i].length() < 3) {
                    chanCodes[i] += "*";
                }
                request[i] = new RequestFilter(new ChannelId(net.get_attributes().get_id(),
                                                             "*",
                                                             "*",
                                                             chanCodes[i],
                                                             range.start_time),
                                               range.start_time,
                                               range.end_time);
            }
            if (dc.getDataCenter(net) != null) {
                if (consecutiveFailures==0) {
                    setStatus("Checking data availability");
                } else {
                    setStatus("Checking data availability after " + consecutiveFailures + " failures");
                }
                request = dc.available_data(request);
                logger.debug(request.length+" items returned for "+net.get_attributes().get_code());
                setStatus(request.length+" items returned");
                if (request.length == 0) {
                    logger.warn("there is no available data "+net.get_attributes().get_code());
                }
                Station[] stations = net.retrieve_stations();
                LinkedList allStations = new LinkedList();
                for (int i = 0; i < stations.length; i++) {
                    allStations.add(stations[i]);
                    logger.debug(StationIdUtil.toString(stations[i].get_id()));
                }
                for (int i = 0; i < request.length; i++) {
                    if (request[i].channel_id.station_code.endsWith("-farm")) {
                        request[i].channel_id.station_code =
                            request[i].channel_id.station_code.substring(0,
                                                                         request[i].channel_id.station_code.length()-5);
                    }
                    if (request[i].channel_id.station_code.endsWith("-spyder")) {
                        request[i].channel_id.station_code =
                            request[i].channel_id.station_code.substring(0,
                                                                         request[i].channel_id.station_code.length()-7);
                    }
                    Iterator it = allStations.iterator();
                    while (it.hasNext()) {
                        Station station = (Station)it.next();
                        if (station.get_code().equals(request[i].channel_id.station_code)) {
                            finishedCheck(station, true);
                            it.remove();
                        }
                    }
                }
                Iterator it = allStations.iterator();
                while (it.hasNext()) {
                    Station station = (Station)it.next();
                    finishedCheck(station, false);
                }
                quitThread = true;

            } else {
                logger.warn("no datacenter for network "+NetworkIdUtil.toString(net.get_attributes().get_id()));
                quitThread = false;
            }
        }
    }

    private static Color STATION_AVAILABLE = Color.BLUE;

    private static Color STATION_UNAVAILABLE = Color.GRAY;

    protected LinkedList stationsToCheck = new LinkedList();

    protected Map stationsUpNow = new HashMap();

    protected DataCenterRouter dc;

    protected TimeInterval TEN_MINUTES = new TimeInterval(20, UnitImpl.MINUTE);

    protected ChannelChooser channelChooser = null;

    protected JList jlist = null;

    protected Origin origin = null;

    private WorkerThreadPool netCheckerPool = new WorkerThreadPool("Network Station Availability Checker", 5);

    private List netCheckers = new ArrayList();

    private static Logger logger = Logger.getLogger(AvailableDataStationRenderer.class);
}
