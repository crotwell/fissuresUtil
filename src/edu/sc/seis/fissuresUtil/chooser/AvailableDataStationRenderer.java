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
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.TimeRange;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.network.StationIdUtil;
import edu.sc.seis.fissuresUtil.cache.DataCenterRouter;
import java.awt.Color;
import java.awt.Component;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
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
										DataCenterRouter dc,
										ChannelChooser channelChooser){
		super(useNames);
		this.dc = dc;
		this.channelChooser = channelChooser;
		startThread(2);
    }

    public AvailableDataStationRenderer(boolean useNames,
										boolean useCodes,
										boolean codeIsFirst,
										DataCenterRouter dc,
										ChannelChooser channelChooser){
		super(useNames, useCodes, codeIsFirst);
		this.dc = dc;
		this.channelChooser = channelChooser;
		startThread(2);
    }

    protected void startThread(int numThreads) {
		logger.debug("start threads for network/station checks");
		// first start the network checker as it may be more efficient
		channelChooser.addNetworkDataListener(new NetworkDataListener() {
					public void networkDataCleared() {
						// TODO
					}

					public void networkDataChanged(NetworkDataEvent s) {
						NetworkChecker netCheck = new NetworkChecker(s.getNetwork());
						Thread t = new Thread(netCheck,
											  "NetworkChecker"+
												  s.getNetwork().get_attributes().get_code());
						t.start();
					}

				});
		// in case networks are already loaded
		NetworkAccess[] nets = channelChooser.getNetworks();
		for (int i = 0; i < nets.length; i++) {
			NetworkChecker netCheck = new NetworkChecker(nets[i]);
			Thread t = new Thread(netCheck,
								  "NetworkChecker"+
									  nets[i].get_attributes().get_code());
			t.start();
		}
		for (int i = 0; i < numThreads; i++) {
			StationChecker checker = new StationChecker();
			Thread t = new Thread(checker, "StationUpChecker"+i);
			t.setPriority(t.getPriority()-1);
			t.start();
		}
    }

	protected void addAvailableStationDataListener(AvailableStationDataListener listener){
		listenerList.add(AvailableStationDataListener.class, listener);
		Set keySet = stationsUpNow.keySet();
		Iterator it = keySet.iterator();
		while (it.hasNext()){
			Station station = (Station)it.next();
			Object obj = stationsUpNow.get(station);
			if (!(obj instanceof Station)){
				boolean b = ((Boolean)obj).booleanValue();
				listener.stationAvailabiltyChanged(new AvailableStationDataEvent(this, station, b));
			}
		}
	}

	protected synchronized void fireStationAvailabilityChanged(Station sta, boolean isUp){
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length-2; i>=0; i-=2) {
			if (listeners[i]==AvailableStationDataListener.class) {
				// Lazily create the event:
				AvailableStationDataEvent fooEvent = new AvailableStationDataEvent(this, sta, isUp);
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

    /** Creates a request filter with several common channel ids to try
	 *  and check for a station existing in a data center.
	 */
    public RequestFilter[] createFakeRequestAll(Channel[] chan) {
		MicroSecondDate now = ClockUtil.now();
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

    /** Creates a request filter with several common channel ids to try
	 *  and check for a station existing in a data center.
	 */
    public RequestFilter[] createFakeRequestOld(StationId stationId) {
		MicroSecondDate now = ClockUtil.now();
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

    protected synchronized void finishedError(Station station, Exception e) {
		stationsUpNow.remove(station);
		stationsToCheck.remove(station);
		logger.error("Problem doing available data for "+
						 StationIdUtil.toString(station.get_id()), e);
    }

    class NetworkChecker implements Runnable {
		NetworkChecker(NetworkAccess net) {
			logger.debug("NetworkChecker constructor"+net.get_attributes().get_code());
			this.net = net;
		}

		NetworkAccess net;

		boolean quitThread = false;

		public void run() {
			while ( ! quitThread) {
				checkNet();
				if (!quitThread) logger.debug("didn't work, sleep to try again");
				try {
					Thread.sleep(1000);
				}catch(InterruptedException e) {
				}
			}
			logger.debug("network checker thread quitting");
		}

		void checkNet() {
			quitThread = true;
			logger.debug("checking "+net.get_attributes().get_code());
			MicroSecondDate now = ClockUtil.now();
			TimeRange range = new TimeRange(now.subtract(TEN_MINUTES).getFissuresTime(),
											now.getFissuresTime());
			RequestFilter[] request = new RequestFilter[1];
			request[0] = new RequestFilter(new ChannelId(net.get_attributes().get_id(),
														 "*",
														 "*",
														 "*",
														 range.start_time),
										   range.start_time,
										   range.end_time);
			if (dc.getDataCenter(net) != null) {
				logger.debug("before available_data call");
				request = dc.available_data(request);
				logger.debug("after avaiable_data call");
				if (request.length != 0) {
					logger.debug("got response for network wildcard request");
					Station[] stations = net.retrieve_stations();
					LinkedList allStations = new LinkedList();
					for (int i = 0; i < stations.length; i++) {
						allStations.add(stations[i]);
					}
					for (int i = 0; i < request.length; i++) {
						Iterator it = allStations.iterator();
						while (it.hasNext()) {
							Station station = (Station)it.next();
							if (station.get_code().equals(request[i].channel_id.station_code)) {
								finishedCheck(station, true);
								it.remove();
							}
						}
						it = null;
					}
					Iterator it = allStations.iterator();
					while (it.hasNext()) {
						Station station = (Station)it.next();
						finishedCheck(station, false);
					}

				} else {
					logger.debug("got nothing from available_data for "+net.get_attributes().get_code());

				}
			} else {
				logger.debug("no datacenter for network");
				quitThread = false;
			}
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

					if (request.length == 0) {
						logger.warn("No channels active now for "+
										StationIdUtil.toString(station.get_id()));
						finishedCheck(station, false);
					}
					if (dc.getDataCenter(net) != null) {
						if (dc.available_data(request).length != 0) {
							finishedCheck(station, true);
						} else {
							finishedCheck(station, false);
						}
					} else {
						finishedError(station);
					}
				} catch (InterruptedException e) {
				} catch (Exception e) {
					logger.warn("Trouble checkng on the available data for "+
									StationIdUtil.toString(station.get_id()), e);
					finishedError(station, e);

				}
			}
		}

    }

    private static Color STATION_AVAILABLE = Color.GREEN.darker().darker();

    private static Color STATION_UNAVAILABLE = Color.RED;

    protected LinkedList stationsToCheck = new LinkedList();

    protected HashMap stationsUpNow = new HashMap();

    protected DataCenterRouter dc;

    protected TimeInterval TEN_MINUTES = new TimeInterval(60, UnitImpl.MINUTE);

    protected ChannelChooser channelChooser = null;

    protected JList jlist = null;

    static Logger logger = Logger.getLogger(AvailableDataStationRenderer.class);
}

