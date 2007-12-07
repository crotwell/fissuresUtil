package edu.sc.seis.fissuresUtil.hibernate;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.exception.ConstraintViolationException;

import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfNetwork.NetworkAttr;
import edu.iris.Fissures.IfNetwork.NetworkId;
import edu.iris.Fissures.IfNetwork.StationId;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.network.NetworkAttrImpl;
import edu.iris.Fissures.network.NetworkIdUtil;
import edu.iris.Fissures.network.StationImpl;
import edu.sc.seis.fissuresUtil.cache.CacheNetworkAccess;
import edu.sc.seis.fissuresUtil.cache.LazyNetworkAccess;
import edu.sc.seis.fissuresUtil.cache.ProxyNetworkDC;
import edu.sc.seis.fissuresUtil.database.NotFound;

public class NetworkDB extends AbstractHibernateDB {

    public int put(NetworkAttrImpl net) {
        Session session = getSession();
        try {
            return ((Integer)session.save(net)).intValue();
        } catch(ConstraintViolationException e) {
            // likely networkAttr is already in db, so update
            Iterator fromDB = getNetworkByCode(net.get_code()).iterator();
            if(fromDB.hasNext()) {
                if(NetworkIdUtil.isTemporary(net.get_code())) {
                    while(fromDB.hasNext()) {
                        NetworkAttrImpl indb = (NetworkAttrImpl)fromDB.next();
                        if(net.get_code() == indb.get_code()
                                && NetworkIdUtil.getTwoCharYear(net.get_id()) == NetworkIdUtil.getTwoCharYear(indb.get_id())) {
                            net.associateInDB(indb);
                            getSession().evict(indb);
                            getSession().saveOrUpdate(net);
                            return net.getDbid();
                        }
                        // shouldn't happen...
                        throw new RuntimeException("can't find by code/date after ConstraintViolationException",
                                                   e);
                    }
                } else {
                    // use first and only net
                    NetworkAttrImpl inDb = (NetworkAttrImpl)fromDB.next();
                    net.associateInDB(inDb);
                    getSession().saveOrUpdate(net);
                    return net.getDbid();
                }
            }
            // shouldn't happen...
            throw new RuntimeException("can't find by code after ConstraintViolationException",
                                       e);
        }
    }

    public int put(StationImpl sta) {
        Integer dbid;
        if(((NetworkAttrImpl)sta.my_network).getDbid() == 0) {
            // assume network info is already put, attach net
            try {
                sta.my_network = getNetworkById(sta.my_network.get_id());
            } catch(NotFound ee) {
                // must not have been added yet
                put((NetworkAttrImpl)sta.my_network);
            }
        }
        internUnit(sta);
        if(sta.getDbid() != 0) {
            getSession().saveOrUpdate(sta);
            return sta.getDbid();
        }
        try {
            // maybe station is already in db, so update
            StationImpl indb = getStationById(sta.get_id());
            sta.associateInDB(indb);
            getSession().evict(indb);
            getSession().saveOrUpdate(sta);
            return sta.getDbid();
        } catch(NotFound e) {
            dbid = (Integer)getSession().save(sta);
            return dbid.intValue();
        }
    }

    public int put(ChannelImpl chan) {
        Integer dbid;
        internUnit(chan);
        if(((StationImpl)chan.my_site.my_station).getDbid() == 0) {
            try {
                chan.my_site.my_station = getStationById(chan.my_site.my_station.get_id());
            } catch(NotFound e) {
                int staDbid = put((StationImpl)chan.my_site.my_station);
            }
        }
        try {
            dbid = (Integer)getSession().save(chan);
            return dbid.intValue();
        } catch(ConstraintViolationException e) {
            // likely station is already in db, so update
            try {
                ChannelImpl indb = getChannel(chan.get_id());
                chan.associateInDB(indb);
                getSession().saveOrUpdate(chan);
                return chan.getDbid();
            } catch(NotFound nf) {
                throw new RuntimeException("Shouldn't happen", e);
            }
        }
    }

    public StationImpl[] getStationByCodes(String netCode, String staCode) {
        Query query = getSession().createQuery(getStationByCodes);
        query.setEntity("netCode", netCode);
        query.setEntity("staCode", staCode);
        List result = query.list();
        return (StationImpl[])result.toArray(new StationImpl[0]);
    }

    public List getAllStationsByCode(String staCode) {
        Query query = getSession().createQuery(getAllStationsByCode);
        query.setEntity("staCode", staCode);
        return query.list();
    }

    private StationImpl getStationById(StationId staId) throws NotFound {
        Query query = getSession().createQuery(getStationByIdString);
        query.setString("netCode", staId.network_id.network_code);
        query.setString("staCode", staId.station_code);
        query.setTimestamp("staBegin",
                           new MicroSecondDate(staId.begin_time).getTimestamp());
        StationImpl out = (StationImpl)query.uniqueResult();
        if(out != null) {
            return out;
        }
        throw new NotFound();
    }

    public List getNetworkByCode(String netCode) {
        Query query = getSession().createQuery(getNetworkByCodeString);
        query.setString("netCode", netCode);
        return query.list();
    }

    private NetworkAttr getNetworkById(NetworkId netId) throws NotFound {
        List result = getNetworkByCode(netId.network_code);
        if(NetworkIdUtil.isTemporary(netId)) {
            Iterator it = result.iterator();
            while(it.hasNext()) {
                NetworkAttr n = (NetworkAttr)it.next();
                if(NetworkIdUtil.areEqual(netId, n.get_id())) {
                    return n;
                }
            }
            throw new NotFound();
        } else {
            if(result.size() > 0) {
                NetworkAttr out = (NetworkAttr)result.get(0);
                return out;
            }
            throw new NotFound();
        }
    }

    public StationImpl getStation(int dbid) throws NotFound {
        StationImpl out = (StationImpl)getSession().get(StationImpl.class,
                                                        new Integer(dbid));
        if(out == null) {
            throw new NotFound();
        }
        return out;
    }

    public StationImpl[] getAllStations() {
        Query query = getSession().createQuery(getAllStationsString);
        List result = query.list();
        return (StationImpl[])result.toArray(new StationImpl[0]);
    }

    public StationImpl[] getStationForNet(NetworkAttrImpl attr) {
        Query query = getSession().createQuery(getStationForNetwork);
        query.setEntity("netAttr", attr);
        List result = query.list();
        return (StationImpl[])result.toArray(new StationImpl[0]);
    }

    public ChannelImpl getChannel(int dbid) throws NotFound {
        ChannelImpl out = (ChannelImpl)getSession().get(ChannelImpl.class,
                                                        new Integer(dbid));
        if(out == null) {
            throw new NotFound();
        }
        return out;
    }

    public CacheNetworkAccess[] getAllNets(ProxyNetworkDC networkDC) {
        Query query = getSession().createQuery(getAllNetsString);
        List result = query.list();
        List out = new ArrayList();
        Iterator it = result.iterator();
        while(it.hasNext()) {
            NetworkAttrImpl attr = (NetworkAttrImpl)it.next();
            CacheNetworkAccess cnet = new LazyNetworkAccess(attr, networkDC);
            out.add(cnet);
        }
        return (CacheNetworkAccess[])out.toArray(new CacheNetworkAccess[0]);
    }

    public ChannelImpl[] getChannelsForStation(StationImpl station) {
        Query query = getSession().createQuery(getChannelForStation);
        query.setEntity("station", station);
        List result = query.list();
        return (ChannelImpl[])result.toArray(new ChannelImpl[0]);
    }

    public ChannelImpl[] getChannelsForStation(StationImpl station,
                                               MicroSecondDate when) {
        Query query = getSession().createQuery(getChannelForStationAtTime);
        query.setEntity("station", station);
        query.setTimestamp("when", when.getTimestamp());
        List result = query.list();
        return (ChannelImpl[])result.toArray(new ChannelImpl[0]);
    }

    public ChannelImpl getChannel(String net,
                                  String sta,
                                  String site,
                                  String chan,
                                  MicroSecondDate when) throws NotFound {
        return getChannel(net, sta, site, chan, when, getChannelByCode);
    }

    public ChannelImpl getChannel(ChannelId id) throws NotFound {
        return getChannel(id.network_id.network_code,
                          id.station_code,
                          id.site_code,
                          id.channel_code,
                          new MicroSecondDate(id.begin_time),
                          getChannelById);
    }

    protected ChannelImpl getChannel(String net,
                                     String sta,
                                     String site,
                                     String chan,
                                     MicroSecondDate when,
                                     String queryString) throws NotFound {
        Query query = getSession().createQuery(queryString);
        query.setString("netCode", net);
        query.setString("stationCode", sta);
        query.setString("siteCode", site);
        query.setString("channelCode", chan);
        query.setTimestamp("when", when.getTimestamp());
        query.setMaxResults(1);
        List result = query.list();
        if(result.size() == 0) {
            throw new NotFound();
        }
        return (ChannelImpl)result.get(0);
    }

    public void internUnit(StationImpl sta) {
        internUnit(sta.getLocation());
    }

    /**
     * assumes station has aready been interned as this needs to happen to avoid
     * dup stations.
     */
    public void internUnit(ChannelImpl chan) {
        internUnit(chan.getSite().getLocation());
        internUnit(chan.getSite().getStation());
        internUnit(chan.getSamplingInfo().interval);
    }

    private static NetworkDB singleton;

    public static NetworkDB getSingleton() {
        if(singleton == null) {
            singleton = new NetworkDB();
        }
        return singleton;
    }

    static String getStationByCodes = "SELECT s From "
            + StationImpl.class.getName()
            + " s WHERE s.networkAttr.id.network_code = :netCode AND s.id.station_code = :staCode";

    static String getAllStationsByCode = "SELECT s From "
            + StationImpl.class.getName()
            + " s WHERE s.id.station_code = :staCode";

    static String getStationByIdString = getStationByCodes
            + " AND sta_begin_time = :staBegin";

    static String getStationForNetwork = "From " + StationImpl.class.getName()
            + " s WHERE s.networkAttr = :netAttr";

    static String getStationForNetworkStation = getStationForNetwork
            + " and s.code = :staCode";

    static String getChannelForStation = "From " + ChannelImpl.class.getName()
            + " c WHERE c.site.station = :station";

    static String getChannelForStationAtTime = getChannelForStation
            + " and :when between c.beginTime and c.endTime";

    static String getChannelByCode = "From "
            + ChannelImpl.class.getName()
            + " c WHERE c.code = :chanCode AND c.site.code = :siteCode AND c.site.station.code = :stationCode AND c.site.station.networkAttr.id.network_code = :netCode AND :when between c.begin and c.end";

    static String getChannelById = "From "
            + ChannelImpl.class.getName()
            + " c WHERE c.code = :chanCode AND c.site.code = :siteCode AND c.site.station.code = :stationCode AND c.site.station.networkAttr.id.network_code = :netCode AND :when = c.begin";

    static String getAllStationsString = "From edu.iris.Fissures.network.StationImpl s";

    static String getAllNetsString = "From edu.iris.Fissures.network.NetworkAttrImpl n";

    static String getNetworkByCodeString = getAllNetsString
            + " WHERE network_code = :netCode";
}
