package edu.sc.seis.fissuresUtil.hibernate;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.cfg.Configuration;

import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.sc.seis.fissuresUtil.database.plottable.PlottableChunk;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;
import edu.sc.seis.fissuresUtil.time.RangeTool;
import edu.sc.seis.fissuresUtil.time.ReduceTool;


public class PlottableDB extends AbstractHibernateDB {
    
    private static PlottableDB singleton;

    public static PlottableDB getSingleton() {
        if(singleton == null) {
            singleton = new PlottableDB();
        }
        return singleton;
    }

    public PlottableChunk[] get(MicroSecondTimeRange requestRange,
                                ChannelId chanId,
                                int pixelsPerDay) {
        // TODO Auto-generated method stub
        throw new RuntimeException("Not yet implemented");
    }

    public void put(PlottableChunk[] chunks) {
        MicroSecondTimeRange stuffInDB = getDroppingRange(chunks);
        PlottableChunk[] dbChunks = get(stuffInDB,
                                        chunks[0].getChannel(),
                                        chunks[0].getPixelsPerDay());
        PlottableChunk[] everything = new PlottableChunk[chunks.length
                + dbChunks.length];
        System.arraycopy(dbChunks, 0, everything, 0, dbChunks.length);
        System.arraycopy(chunks, 0, everything, dbChunks.length, chunks.length);
        // scrutinizeEverything(everything, "unmerged");
        everything = ReduceTool.merge(everything);
        // scrutinizeEverything(everything, "merged");
        everything = breakIntoDays(everything);
        // scrutinizeEverything(everything, "split into days");
        int rowsDropped = drop(stuffInDB,
                               chunks[0].getChannel(),
                               chunks[0].getPixelsPerDay());
        for(int i = 0; i < everything.length; i++) {
            getSession().save(everything[i]);
        }
    }
    

    public int drop(MicroSecondTimeRange requestRange,
                    ChannelId id,
                    int samplesPerDay) {
        // TODO Auto-generated method stub
        throw new RuntimeException("Not yet implemented");
        
    }
    
    protected PlottableChunk[] getSmallChunks(MicroSecondTimeRange requestRange,
                                ChannelId chanId,
                                int pixelsPerDay) {
        // TODO Auto-generated method stub
        throw new RuntimeException("Not yet implemented");
    }

    private PlottableChunk[] breakIntoDays(PlottableChunk[] everything) {
        List results = new ArrayList();
        for(int i = 0; i < everything.length; i++) {
            PlottableChunk[] days = everything[i].breakIntoDays();
            for(int j = 0; j < days.length; j++) {
                results.add(days[j]);
            }
        }
        return (PlottableChunk[])results.toArray(new PlottableChunk[0]);
    }

    private static MicroSecondTimeRange getDroppingRange(PlottableChunk[] chunks) {
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
    

    private static Logger logger = Logger.getLogger(PlottableDB.class);
}
