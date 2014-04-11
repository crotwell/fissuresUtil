package edu.sc.seis.fissuresUtil.chooser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfNetwork.StationId;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.network.NetworkIdUtil;
import edu.sc.seis.fissuresUtil.time.MicroSecondTimeRange;

public class CoarseAvailableData {

    public synchronized void append(ChannelId chan, MicroSecondTimeRange range) {
        getList(chan).add(range);
    }
    
    public synchronized void update(ChannelId chan, List<MicroSecondTimeRange> rangeList) {
        cache.put(ChannelIdUtil.toString(chan), rangeList);
    }
    
    /** returns null if no availability is cached for this chan
     * 
     * @param chan
     * @return
     */
    public synchronized List<MicroSecondTimeRange> get(ChannelId chan) {
        if (isCached(chan)) {
            return getList(chan);
        }
        return null;
    }
    
    public synchronized boolean isCached(ChannelId chan) {
        return cache.containsKey(ChannelIdUtil.toString(chan));
    }
    
    public synchronized boolean overlaps(ChannelId chan, MicroSecondTimeRange range) {
        if (isCached(chan)) {
            List<MicroSecondTimeRange> chanList = get(chan);
            for (MicroSecondTimeRange dataRange : chanList) {
                if (range.intersects(dataRange)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public synchronized boolean anyChannelOverlaps(StationId station, MicroSecondTimeRange range) {
        String staPrefix = station.network_id.network_code+NetworkIdUtil.DOT+station.station_code;
        for (String chanStr : cache.keySet()) {
            if (chanStr.startsWith(staPrefix)) {
                List<MicroSecondTimeRange> chanList = cache.get(chanStr);
                for (MicroSecondTimeRange dataRange : chanList) {
                    if (range.intersects(dataRange)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    private synchronized List<MicroSecondTimeRange> getList(ChannelId chan) {
        String chanIdStr = ChannelIdUtil.toString(chan);
        List<MicroSecondTimeRange> chanCache = cache.get(chanIdStr);
        if (chanCache == null) {
            chanCache = new ArrayList<MicroSecondTimeRange>();
            cache.put(chanIdStr, chanCache);
        }
        return chanCache;
    }
    
    Map<String, List<MicroSecondTimeRange>> cache = new HashMap<String, List<MicroSecondTimeRange>>();


    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(CoarseAvailableData.class);

}