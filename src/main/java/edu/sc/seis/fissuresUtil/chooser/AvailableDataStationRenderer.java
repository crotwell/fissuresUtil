/**
 * AvailableDataStationRenderer.java
 * 
 * @author Created by Omnicore CodeGuide
 */
package edu.sc.seis.fissuresUtil.chooser;

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.iris.Fissures.TimeRange;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfNetwork.NetworkId;
import edu.iris.Fissures.IfNetwork.Station;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.network.NetworkIdUtil;
import edu.iris.Fissures.network.StationIdUtil;
import edu.iris.Fissures.network.StationImpl;
import edu.sc.seis.fissuresUtil.cache.AbstractJob;
import edu.sc.seis.fissuresUtil.cache.JobTracker;
import edu.sc.seis.fissuresUtil.cache.WorkerThreadPool;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;

public class AvailableDataStationRenderer extends NameListCellRenderer {

    public AvailableDataStationRenderer(boolean useNames) {
        super(useNames);
    }

    public AvailableDataStationRenderer(boolean useNames, boolean useCodes, boolean codeIsFirst) {
        super(useNames, useCodes, codeIsFirst);
    }

    public AvailableDataStationRenderer(boolean useNames,
                                        boolean useCodes,
                                        boolean codeIsFirst,
                                        ChannelChooserSeisSource dc,
                                        ChannelChooser channelChooser) {
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

            public void networkDataCleared() {}

            public void networkDataChanged(NetworkDataEvent s) {
                startNetworkChecker(s.getNetworkFromSource());
            }
        });
    }

    protected void recheckNetworks() {
        stationsUpNow.clear();
        List<NetworkFromSource> nets = channelChooser.getNetworks();
        for (NetworkFromSource networkFromSource : nets) {
            startNetworkChecker(networkFromSource);
        }
    }

    @Override
    public void stopChecking() {
        super.stopChecking();
        synchronized(netCheckers) {
            Iterator it = netCheckers.iterator();
            while (it.hasNext()) {
                ((NetworkChecker)it.next()).stop();
            }
        }
    }

    private void startNetworkChecker(NetworkFromSource na) {
        NetworkChecker checker = null;
        synchronized(netCheckers) {
            Iterator it = netCheckers.iterator();
            NetworkId naId = na.getNetAttr().get_id();
            while (it.hasNext()) {
                NetworkChecker cur = (NetworkChecker)it.next();
                NetworkId curId = cur.getNetwork().getNetAttr().get_id();
                if (NetworkIdUtil.areEqual(curId, naId)) {
                    checker = cur;
                    checker.stop();
                    break;
                }
            }
            if (checker == null) {
                checker = new NetworkChecker(na);
                netCheckers.add(checker);
            }
            netCheckerPool.invokeLater(checker);
        }
    }

    protected synchronized void addAvailableStationDataListener(AvailableStationDataListener listener) {
        listenerList.add(AvailableStationDataListener.class, listener);
        Set keySet = stationsUpNow.keySet();
        Iterator it = keySet.iterator();
        while (it.hasNext()) {
            Station station = (Station)it.next();
            Object obj = stationsUpNow.get(station);
            if (!(obj instanceof Station)) {
                int status = ((Boolean)obj).booleanValue() ? AvailableStationDataEvent.UP
                        : AvailableStationDataEvent.DOWN;
                listener.stationAvailabiltyChanged(new AvailableStationDataEvent(station, status));
            }
        }
    }

    protected synchronized void fireStationAvailabilityChanged(Station sta, boolean isUp) {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        AvailableStationDataEvent fooEvent = null;
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == AvailableStationDataListener.class) {
                // Lazily create the event:
                if (fooEvent == null) {
                    int status = isUp ? AvailableStationDataEvent.UP : AvailableStationDataEvent.DOWN;
                    fooEvent = new AvailableStationDataEvent(sta, status);
                }
                ((AvailableStationDataListener)listeners[i + 1]).stationAvailabiltyChanged(fooEvent);
            }
        }
    }

    public void setJList(JList jlist) {
        this.jlist = jlist;
        if (jlist instanceof SortedStationJList) {
            ((SortedStationJList)jlist).setNamer(this);
        }
    }

    public Component getListCellRendererComponent(JList list,
                                                  Object value,
                                                  int index,
                                                  boolean isSelected,
                                                  boolean cellHasFocus) {
        Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        Station station = (Station)value;
        if (stationsUpNow.get(station) != null && stationsUpNow.get(station) instanceof Boolean) {
            Boolean val = (Boolean)stationsUpNow.get(station);
            if (val.booleanValue()) {
                c.setForeground(STATION_AVAILABLE);
            } else {
                c.setForeground(STATION_UNAVAILABLE);
            }
        } else {
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

    /**
     * Creates a request filter with several common channel ids to try and check
     * for a station existing in a data center.
     */
    public RequestFilter[] createFakeRequestBHZ(Channel[] chan) {
        MicroSecondDate now = ClockUtil.now();
        TimeRange range = new TimeRange(now.subtract(TEN_MINUTES).getFissuresTime(), now.getFissuresTime());
        RequestFilter[] request = new RequestFilter[0];
        for (int i = 0; i < chan.length; i++) {
            if (chan[i].get_code().equals("BHZ") || chan[i].get_code().equals("LHZ")
                    || chan[i].get_code().equals("SHZ")) {
                RequestFilter[] tmp = new RequestFilter[request.length + 1];
                System.arraycopy(request, 0, tmp, 0, request.length);
                tmp[tmp.length - 1] = new RequestFilter(chan[i].get_id(), range.start_time, range.end_time);
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
        Station sta = (Station)stationsToCheck.removeFirst();
        stationsUpNow.put(sta, sta); // put station in as placeholder, means I'm
        // working on it
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
        logger.warn("Seismogram server not found for " + StationIdUtil.toString(station.get_id()));
    }

    protected synchronized void finishedError(Station station, Throwable e) {
        stationsUpNow.remove(station);
        stationsToCheck.remove(station);
        logger.error("Problem doing available data for " + StationIdUtil.toString(station.get_id()), e);
    }

    class NetworkChecker extends AbstractJob {

        NetworkChecker(NetworkFromSource na) {
            super(na.getNetAttr().get_code() + " Available Data");
            this.net = na;
            JobTracker.getTracker().add(this);
            logger.info("constructor NetworkChecker "+net.getNetAttr().get_id().network_code);
        }

        public void stop() {
            quitThread = true;
        }

        public int hashCode() {
            return NetworkIdUtil.hashCode(net.getNetAttr().get_id());
        }

        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (o instanceof NetworkChecker) {
                NetworkId myId = net.getNetAttr().get_id();
                NetworkId oId = ((NetworkChecker)o).net.getNetAttr().get_id();
                return NetworkIdUtil.areEqual(myId, oId);
            }
            return false;
        }

        public NetworkFromSource getNetwork() {
            return net;
        }

        NetworkFromSource net;

        boolean quitThread;

        public void runJob() {
            setFinished(false);
            quitThread = false;
            while (!quitThread) {
                try {
                    checkNet();
                } catch(ChannelChooserException e) {
                    logger.warn("Problem checking networks", e);
                }
            }
            logger.debug("network checker thread quitting");
            setFinished();
        }

        void checkNet() throws ChannelChooserException {
            logger.info("checking " + net.getNetAttr().get_code());
            TimeRange range;
            setStatus("Creating available data request");
            if (origin == null) {
                // use current time
                MicroSecondDate now = ClockUtil.now();
                range = new TimeRange(now.subtract(TEN_MINUTES).getFissuresTime(), now.getFissuresTime());
            } else {
                // use origin time offset
                MicroSecondDate oTime = new MicroSecondDate(origin.getOriginTime());
                range = new TimeRange(oTime.getFissuresTime(), oTime.add(TEN_MINUTES).getFissuresTime());
            }
            String[] siteCodes = new String[] {"00", "  "};
            String[] chanCodes = channelChooser.getSelectedChanCodes();
            RequestFilter[] request = new RequestFilter[chanCodes.length*siteCodes.length];
            for (int c = 0; c < chanCodes.length; c++) {
                while (chanCodes[c].length() < 3) {
                    chanCodes[c] += "?";
                }
                for (int l = 0; l < siteCodes.length; l++) {
                    request[c*chanCodes.length+l] = new RequestFilter(new ChannelId(net.getNetAttr().get_id(),
                                                                   "*",
                                                                   siteCodes[l],
                                                                   chanCodes[c],
                                                                   range.start_time), 
                                                     range.start_time,
                                                     range.end_time);
                }
            }
            List<StationImpl> stations = net.getSource().getStations(net.getNetAttr());
            LinkedList allStations = new LinkedList();
            for (StationImpl stationImpl : stations) {
                allStations.add(stationImpl);
                logger.debug(StationIdUtil.toString(stationImpl.get_id()));
            }
            setStatus("Checking data availability");
            List<RequestFilter> available = new ArrayList<RequestFilter>();
            try {
                available = dc.availableData(Arrays.asList(request));
            } catch(Throwable t) {
                Iterator it = allStations.iterator();
                while (it.hasNext()) {
                    Station station = (Station)it.next();
                    finishedError(station, t);
                }
                quitThread = true;
                GlobalExceptionHandler.handle(t);
                return;
            }
            logger.info(available.size() + " items returned for " + net.getNetAttr().get_code());
            setStatus(available.size() + " items returned");
            if (available.size() == 0) {
                logger.warn("there is no available data " + net.getNetAttr().get_code());
            }
            for (RequestFilter requestFilter : available) {
                stripStationCodeSuffix(requestFilter.channel_id, "-farm");
                stripStationCodeSuffix(requestFilter.channel_id, "-spyder");
                Iterator it = allStations.iterator();
                while (it.hasNext()) {
                    Station station = (Station)it.next();
                    if (station.get_code().equals(requestFilter.channel_id.station_code)) {
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
        }
    }

    private static void stripStationCodeSuffix(ChannelId id, String suffix) {
        if (id.station_code.endsWith(suffix)) {
            id.station_code = id.station_code.substring(0, suffix.length());
        }
    }

    private static Color STATION_AVAILABLE = Color.BLUE;

    private static Color STATION_UNAVAILABLE = Color.GRAY;

    protected LinkedList stationsToCheck = new LinkedList();

    protected Map stationsUpNow = new HashMap();

    protected ChannelChooserSeisSource dc;

    protected TimeInterval TEN_MINUTES = new TimeInterval(20, UnitImpl.MINUTE);

    protected ChannelChooser channelChooser = null;

    protected JList jlist = null;

    protected Origin origin = null;

    private WorkerThreadPool netCheckerPool = new WorkerThreadPool("Network Station Availability Checker", 5);

    private List netCheckers = new ArrayList();

    private static Logger logger = LoggerFactory.getLogger(AvailableDataStationRenderer.class);
}
