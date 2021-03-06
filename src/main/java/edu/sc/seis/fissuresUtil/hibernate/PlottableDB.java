package edu.sc.seis.fissuresUtil.hibernate;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.sc.seis.fissuresUtil.time.MicroSecondTimeRange;
import edu.sc.seis.fissuresUtil.time.RangeTool;
import edu.sc.seis.fissuresUtil.time.ReduceTool;


public class PlottableDB extends AbstractHibernateDB {
    
    protected PlottableDB() {}
    
    private static PlottableDB singleton;

    public static PlottableDB getSingleton() {
        if(singleton == null) {
            singleton = new PlottableDB();
        }
        return singleton;
    }

    public List<PlottableChunk> get(MicroSecondTimeRange requestRange,
                                ChannelId channel,
                                int pixelsPerDay) {
        return get(requestRange, channel.network_id.network_code,
                   channel.station_code,
                   channel.site_code,
                   channel.channel_code,
                   pixelsPerDay);
    }


    public List<PlottableChunk> get(MicroSecondTimeRange requestRange,
                                String network,
                                String station,
                                String site,
                                String channel,
                                int pixelsPerDay) {
        Query q = getSession().createQuery("from "+PlottableChunk.class.getName()+" where "+
        " networkCode = :net and stationCode = :sta and siteCode = :site and channelCode = :chan "+
        " and pixelsPerDay = :pixelsPerDay "+
        " and ( beginTimestamp <= :end and endTimestamp >= :begin )");
        q.setString("net", network);
        q.setString("sta", station);
        q.setString("site", site);
        q.setString("chan", channel);
        q.setInteger("pixelsPerDay", pixelsPerDay);
        q.setTimestamp("end", requestRange.getEndTime().getTimestamp());
        q.setTimestamp("begin", requestRange.getBeginTime().getTimestamp());
        List<PlottableChunk> chunks = q.list();
        return chunks;
    }

    public void put(List<PlottableChunk> chunks) {
        if (chunks.size() == 0) {return;}
        MicroSecondTimeRange stuffInDB = getDroppingRange(chunks);
        List<PlottableChunk> dbChunks = get(stuffInDB,
                                        chunks.get(0).getNetworkCode(),
                                        chunks.get(0).getStationCode(),
                                        chunks.get(0).getSiteCode(),
                                        chunks.get(0).getChannelCode(),
                                        chunks.get(0).getPixelsPerDay());
        List<PlottableChunk> everything = new ArrayList<PlottableChunk>();
        everything.addAll(dbChunks);
        everything.addAll(chunks);
        // scrutinizeEverything(everything, "unmerged");
        everything = ReduceTool.merge(everything);
        // scrutinizeEverything(everything, "merged");
        everything = breakIntoDays(everything);
        // scrutinizeEverything(everything, "split into days");
        PlottableChunk first = chunks.get(0);
        int rowsDropped = drop(stuffInDB,
                               first.getNetworkCode(),
                               first.getStationCode(),
                               first.getSiteCode(),
                               first.getChannelCode(),
                               first.getPixelsPerDay());
        for (PlottableChunk plottableChunk : everything) {
            getSession().save(plottableChunk);
        }
    }
    

    public int drop(MicroSecondTimeRange requestRange,
                    String network,
                    String station,
                    String site,
                    String channel,
                    int samplesPerDay) {
        List<PlottableChunk> indb = get(requestRange, network, station, site, channel, samplesPerDay);
        for (PlottableChunk plottableChunk : indb) {
            getSession().delete(plottableChunk);
        }
        return indb.size();
    }
    
    protected PlottableChunk[] getSmallChunks(MicroSecondTimeRange requestRange,
                                              String network,
                                              String station,
                                              String site,
                                              String channel,
                                int pixelsPerDay) {
        // TODO Auto-generated method stub
        throw new RuntimeException("Not yet implemented");
    }

    private List<PlottableChunk> breakIntoDays(List<PlottableChunk> everything) {
        List<PlottableChunk> results = new ArrayList<PlottableChunk>();
        for (PlottableChunk chunk : everything) {
            results.addAll(chunk.breakIntoDays());
        }
        return results;
    }

    private static MicroSecondTimeRange getDroppingRange(List<PlottableChunk> chunks) {
        MicroSecondTimeRange stuffInDB = RangeTool.getFullTime(chunks);
        MicroSecondDate startTime = PlottableChunk.stripToDay(stuffInDB.getBeginTime());
        MicroSecondDate strippedEnd = PlottableChunk.stripToDay(stuffInDB.getEndTime());
        if(!strippedEnd.equals(stuffInDB.getEndTime())) {
            strippedEnd = strippedEnd.add(PlottableChunk.ONE_DAY);
        }
        return new MicroSecondTimeRange(startTime, strippedEnd);
    }

    protected static int MIN_CHUNK_SIZE = 100;
    
    static String configFile = "edu/sc/seis/fissuresUtil/hibernate/Plottable.hbm.xml";
    
    public static void configHibernate(Configuration config) {
        logger.debug("adding to HibernateUtil   "+configFile);
        config.addResource(configFile);
    }
    

    private static Logger logger = LoggerFactory.getLogger(PlottableDB.class);
}
