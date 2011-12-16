package edu.sc.seis.fissuresUtil.hibernate;

import java.util.List;

import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.network.NetworkAttrImpl;
import edu.iris.Fissures.network.NetworkIdUtil;
import edu.iris.Fissures.network.StationIdUtil;
import edu.iris.Fissures.network.StationImpl;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;


public class NetworkConsistencyCheck {

    public static boolean isConsistent(NetworkAttrImpl net, StationImpl sta) {
        MicroSecondTimeRange staRange = new MicroSecondTimeRange(sta.getEffectiveTime());
        MicroSecondTimeRange netRange = new MicroSecondTimeRange(net.getEffectiveTime());
        if (netRange.getBeginTime().after(staRange.getBeginTime())) {
            logger.warn("Network begins after station: "+NetworkIdUtil.toString(net)+"  "+StationIdUtil.toString(sta));
            return false;
        }
        return true;
    }

    public static boolean isConsistent(List<? extends StationImpl> staList) {
        if (staList.size() < 2) {
            return true;
        }
        List<? extends StationImpl> subStaList = staList.subList(1, staList.size());
        StationImpl first = staList.get(0);
        for (StationImpl nextSta : subStaList) {
            if ( ! isConsistent(first, nextSta)) {
                return false;
            }
        }
        return isConsistent(subStaList);
    }
    
    public static boolean isConsistent(StationImpl staA, StationImpl staB) {
        if ( ! staA.getNetworkAttrImpl().get_code().equals(staB.getNetworkAttrImpl().get_code()) 
                || ! staA.get_code().equals(staB.get_code())) {
            //different stations, so ok
            return true;
        }
        MicroSecondTimeRange staARange = new MicroSecondTimeRange(staA.getEffectiveTime());
        MicroSecondTimeRange staBRange = new MicroSecondTimeRange(staB.getEffectiveTime());
        if (staARange.intersects(staBRange)) {
            logger.warn("Station overlaps other station: "+StationIdUtil.toString(staA)+"-"+staA.getEndTime().date_time
                         +"   "+StationIdUtil.toString(staB)+"-"+staB.getEndTime().date_time);
            return false;
        }
        return true;
    }

    public static boolean isConsistent(StationImpl sta, ChannelImpl chan) {
        MicroSecondTimeRange staRange = new MicroSecondTimeRange(sta.getEffectiveTime());
        MicroSecondTimeRange chanRange = new MicroSecondTimeRange(chan.getEffectiveTime());
        if (staRange.getBeginTime().after(chanRange.getBeginTime())) {
            logger.warn("Station begins after channel: "+ChannelIdUtil.toStringNoDates(chan.getId())+" "+chanRange
                         +"    "+StationIdUtil.toString(sta)+" "+staRange);
            return false;
        }
        return true;
    }   

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(NetworkConsistencyCheck.class);
}
