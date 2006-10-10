package edu.sc.seis.fissuresUtil.database.network;

import edu.iris.Fissures.Orientation;
import edu.iris.Fissures.Sampling;
import edu.iris.Fissures.TimeRange;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfNetwork.Site;
import edu.iris.Fissures.network.ChannelImpl;

public class DBChannel extends ChannelImpl {

    public DBChannel(ChannelId id,
                     String name,
                     Orientation an_orientation,
                     Sampling sampling_info,
                     TimeRange effective_time,
                     Site my_site,
                     int dbId) {
        super(id, name, an_orientation, sampling_info, effective_time, my_site);
        this.dbId = dbId;
    }

    public int getDbId() {
        return dbId;
    }

    private int dbId;
}
