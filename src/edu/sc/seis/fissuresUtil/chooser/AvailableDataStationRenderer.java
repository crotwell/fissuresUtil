/**
 * AvailableDataStationRenderer.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.chooser;

import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.Station;
import edu.iris.Fissures.IfNetwork.StationId;
import edu.iris.Fissures.IfSeismogramDC.DataCenterOperations;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.TimeRange;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.network.NetworkIdUtil;
import edu.iris.Fissures.network.StationIdUtil;
import java.awt.Color;
import java.awt.Component;
import java.util.HashMap;
import java.util.LinkedList;
import javax.swing.JList;
import org.apache.log4j.Logger;

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
                                        DataCenterOperations dc,
                                       ChannelChooser channelChooser){
        super(useNames);
        this.dc = dc;
        startThread();
    }

    public AvailableDataStationRenderer(boolean useNames,
                                        boolean useCodes,
                                        boolean codeIsFirst,
                                        DataCenterOperations dc,
                                       ChannelChooser channelChooser){
        super(useNames, useCodes, codeIsFirst);
        this.dc = dc;
        this.channelChooser = channelChooser;
        startThread();
    }

    protected void startThread() {
        StationChecker checker = new StationChecker();
        Thread t = new Thread(checker, "StationUpChecker");
        t.start();
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
            logger.debug("Station available found "+
                             StationIdUtil.toString(station.get_id())+ val);
            if (val.booleanValue()) {
                c.setForeground(Color.GREEN);
            } else {
                c.setForeground(Color.RED);
            }
        } else {
            logger.debug("Station available NOT found "+
                             StationIdUtil.toString(station.get_id()));
            if (stationsUpNow.get(station) == null) {
                // only add if we are not already "working on it"
                addToCheck(station);
            }
        }

        return c;
    }

    /** Creates a request filter with several common channel ids to try
     *  and check for a station existing in a data center.
     */
    public RequestFilter[] createFakeRequest(Channel[] chan) {
        MicroSecondDate now = new MicroSecondDate();
        TimeRange range = new TimeRange(now.subtract(TEN_MINUTES).getFissuresTime(),
                                        now.getFissuresTime());
        RequestFilter[] request = new RequestFilter[chan.length];
        for (int i=0; i<chan.length; i++) {
        request[i] = new RequestFilter(chan[i].get_id(),
                                       range.start_time,
                                       range.end_time);
        }
        return request;
    }

    /** Creates a request filter with several common channel ids to try
     *  and check for a station existing in a data center.
     */
    public RequestFilter[] createFakeRequestOld(StationId stationId) {
        MicroSecondDate now = new MicroSecondDate();
        TimeRange range = new TimeRange(now.subtract(TEN_MINUTES).getFissuresTime(),
                                        now.getFissuresTime());
        RequestFilter[] request = new RequestFilter[4];
        request[0] = new RequestFilter(new ChannelId(stationId.network_id,
                                                     stationId.station_code,
                                                     "00",
                                                     "BHZ",
                                                     range.start_time),
                                       range.start_time,
                                       range.end_time);
        request[1] = new RequestFilter(new ChannelId(stationId.network_id,
                                                     stationId.station_code,
                                                     "  ",
                                                     "BHZ",
                                                     range.start_time),
                                       range.start_time,
                                       range.end_time);
        request[2] = new RequestFilter(new ChannelId(stationId.network_id,
                                                     stationId.station_code,
                                                     "00",
                                                     "LHZ",
                                                     range.start_time),
                                       range.start_time,
                                       range.end_time);
        request[3] = new RequestFilter(new ChannelId(stationId.network_id,
                                                     stationId.station_code,
                                                     "  ",
                                                     "LHZ",
                                                     range.start_time),
                                       range.start_time,
                                       range.end_time);
        return request;
    }

    protected synchronized void addToCheck(Station station) {
        stationsToCheck.add(station);
        notifyAll();
    }

    protected synchronized Station getToCheck() throws InterruptedException {
        while (stationsToCheck.isEmpty()) {
            wait();
        }
        Station sta =  (Station)stationsToCheck.removeLast();
        stationsUpNow.put(sta, sta); // put station in as placeholder, means I'm working on it
        return sta;
    }

    protected synchronized void finishedCheck(Station station, boolean val) {
        if (val) {
            stationsUpNow.put(station, Boolean.TRUE);
        } else {
            stationsUpNow.put(station, Boolean.FALSE);
        }
        if (jlist != null) {
            jlist.repaint();
        }
    }

    class StationChecker implements Runnable {
        boolean quitThread = false;

        public void run() {
            Station station;
            while ( ! quitThread) {
                station = null;
                try {
                    station = getToCheck();
                    Channel[] chans = null;
                    NetworkAccess net =
                        channelChooser.getNetworkAccess(station.get_id().network_id);
                    if ( net != null) {
                        chans =
                            net.retrieve_for_station(station.get_id());
                    }

                    RequestFilter[] request = createFakeRequest(chans);

                    if (dc.available_data(request).length != 0) {
                        finishedCheck(station, true);
                    } else {
                        finishedCheck(station, false);
                    }
                } catch (InterruptedException e) {
                } catch (Exception e) {
                    logger.warn("Trouble checkng on the available data for "+
                                    StationIdUtil.toString(station.get_id()), e);
                }
            }
        }

    }

    protected LinkedList stationsToCheck = new LinkedList();

    protected HashMap stationsUpNow = new HashMap();

    protected DataCenterOperations dc;

    protected TimeInterval TEN_MINUTES = new TimeInterval(10, UnitImpl.MINUTE);

    protected ChannelChooser channelChooser = null;

    protected JList jlist = null;

    static Logger logger = Logger.getLogger(AvailableDataStationRenderer.class);
}

