package edu.sc.seis.fissuresUtil.hibernate;

import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.sc.seis.fissuresUtil.database.plottable.PlottableChunk;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;


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
                                int i) {
        // TODO Auto-generated method stub
        return null;
    }

    public void put(PlottableChunk[] plottableChunks) {
        for(int i = 0; i < plottableChunks.length; i++) {
            getSession().save(plottableChunks[i]);
        }
    }

}
