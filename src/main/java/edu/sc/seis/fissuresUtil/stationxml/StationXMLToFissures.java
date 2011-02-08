package edu.sc.seis.fissuresUtil.stationxml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import edu.iris.Fissures.Location;
import edu.iris.Fissures.LocationType;
import edu.iris.Fissures.Orientation;
import edu.iris.Fissures.Time;
import edu.iris.Fissures.TimeRange;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfNetwork.NetworkId;
import edu.iris.Fissures.IfNetwork.SiteId;
import edu.iris.Fissures.IfNetwork.StationId;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.SamplingImpl;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.TimeUtils;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.network.NetworkAttrImpl;
import edu.iris.Fissures.network.NetworkIdUtil;
import edu.iris.Fissures.network.SiteImpl;
import edu.iris.Fissures.network.StationImpl;
import edu.sc.seis.seisFile.stationxml.Channel;
import edu.sc.seis.seisFile.stationxml.Epoch;
import edu.sc.seis.seisFile.stationxml.StaMessage;
import edu.sc.seis.seisFile.stationxml.Station;
import edu.sc.seis.seisFile.stationxml.StationEpoch;
import edu.sc.seis.seisFile.stationxml.StationIterator;
import edu.sc.seis.seisFile.stationxml.StationXMLException;


public class StationXMLToFissures {
    
    
    public static StationImpl convert(StationEpoch xml, NetworkAttrImpl netAttr, String staCode) {
        TimeRange effectiveTime = new TimeRange(new Time(xml.getStartDate(), -1), 
                                                new Time(xml.getEndDate(), -1));
        return new StationImpl(new StationId(netAttr.getId(), staCode, effectiveTime.start_time),
                                          xml.getSite().getCountry(),
                                          new Location(xml.getLat(), xml.getLon(),
                                                       new QuantityImpl(xml.getElevation(), UnitImpl.METER),
                                                       new QuantityImpl(0, UnitImpl.METER),
                                                       LocationType.GEOGRAPHIC),
                                          UNKNOWN, UNKNOWN, UNKNOWN,
                                          netAttr);
    }
    
    public static List<StationImpl> convert(Station xml) throws StationXMLException {
        StationEpoch firstEpoch = xml.getStationEpochs().get(0);
        MicroSecondDate staBegin = new MicroSecondDate(firstEpoch.getStartDate());
        NetworkAttrImpl attr = new NetworkAttrImpl(new NetworkId(xml.getNetCode(), staBegin.getFissuresTime()), UNKNOWN, UNKNOWN, UNKNOWN);
        List<NetworkAttrImpl> nets = new ArrayList<NetworkAttrImpl>();
        nets.add(attr);
        List<StationChannelBundle> bundles = convert(xml, nets, false);
        List<StationImpl> out = new ArrayList<StationImpl>();
        for (StationChannelBundle b : bundles) {
            out.add(b.getStation());
        }
        return out;
    }
    
    public static List<StationChannelBundle> convert(Station xml, List<NetworkAttrImpl> knownNets, boolean extractChannels) throws StationXMLException {
        System.out.println("convert: "+xml.getNetCode()+"."+xml.getStaCode()+"  knownNets.size="+knownNets.size());
        NetworkAttrImpl attr = null;
        for (NetworkAttrImpl net : knownNets) {
            if (xml.getNetCode().equals(net.get_code()))  {
                if (! NetworkIdUtil.isTemporary(net.get_id())) {
                    // found it
                    attr = net;
                    break;
                }  else {
                    StationEpoch firstEpoch = xml.getStationEpochs().get(0);
                    MicroSecondDate staBegin = new MicroSecondDate(firstEpoch.getStartDate());
                    MicroSecondDate netBegin = new MicroSecondDate(net.getBeginTime());
                    if (staBegin.after(netBegin) && staBegin.before(netBegin.add(new TimeInterval(2, UnitImpl.GREGORIAN_YEAR)))) {
                        // close enough
                        attr = net;
                        break;
                    }
                }
            }
        }
        if (attr == null) {
            // didn't find it
            attr = new NetworkAttrImpl(new NetworkId(xml.getNetCode(), TimeUtils.timeUnknown), UNKNOWN, UNKNOWN, UNKNOWN);
            throw new StationXMLException("Can't find network for "+xml.getNetCode());
        }
        MicroSecondDate minStationStart = null;
        if ( ! TimeUtils.areEqual(attr.getBeginTime(), TimeUtils.timeUnknown)) {
            minStationStart = new MicroSecondDate(attr.getBeginTime());
        }
        List<StationChannelBundle> out = new ArrayList<StationChannelBundle>();
        Iterator<StationEpoch> it = xml.getStationEpochs().iterator();
        while (it.hasNext()) {
            StationEpoch staEpoch = it.next();
            StationImpl sta = convert(staEpoch, attr, xml.getStaCode());
            StationChannelBundle bundle = new StationChannelBundle(sta);
            out.add(bundle);
            MicroSecondDate staBegin = new MicroSecondDate(sta.getBeginTime());
            if (minStationStart == null || minStationStart.after(staBegin)) {
                minStationStart = staBegin;
            }
            if (extractChannels) {
                for (Channel xmlChan : staEpoch.getChannelList()) {
                    List<ChannelSensitivityBundle> chans = convert(xmlChan, sta);
                    bundle.getChanList().addAll(chans);
                    System.out.println("Convert: "+ChannelIdUtil.toStringNoDates(chans.get(0).getChan()));
                    if ( ! chans.get(0).getChan().getStationImpl().getNetworkAttrImpl().get_code().equals(xml.getNetCode()) ||
                            !  chans.get(0).getChan().getStationImpl().get_code().equals(xml.getStaCode())) {
                        throw new StationXMLException("Chan doesn't match station or net: "+ChannelIdUtil.toStringNoDates(chans.get(0).getChan())+"  "+xml.getNetCode()+"."+xml.getStaCode()+"  attr:"+NetworkIdUtil.toStringNoDates(attr));
                    }
                }
            }
        }
        attr.setBeginTime(minStationStart.getFissuresTime());
        return out;
    }
    
    public static List<ChannelSensitivityBundle> convert(Channel xml, StationImpl station) throws StationXMLException {
        List<ChannelSensitivityBundle> out = new ArrayList<ChannelSensitivityBundle>();
        for (Epoch chanEpoch : xml.getChanEpochList()) {
            SamplingImpl samp;
            if (Math.abs(Math.round(chanEpoch.getSampleRate()) - chanEpoch.getSampleRate()) < 0.0001f) {
                // looks like an int samples per second
                samp = new SamplingImpl(Math.round(chanEpoch.getSampleRate()), ONE_SECOND);
            } else {
                samp = new SamplingImpl(1, new TimeInterval(1/chanEpoch.getSampleRate(), UnitImpl.SECOND));
            }
            TimeRange chanTimeRange = new TimeRange(convertTime(chanEpoch.getStartDate()),
                                                    convertTime(chanEpoch.getEndDate()));
            ChannelImpl chan = new ChannelImpl(new ChannelId(station.getId().network_id,
                                                             station.get_code(),
                                                             xml.getLocCode(),
                                                             xml.getChanCode(),
                                                             convertTime(chanEpoch.getStartDate())),
                                                UNKNOWN,
                                                new Orientation(chanEpoch.getAzimuth(), chanEpoch.getDip()),
                                                samp,
                                                chanTimeRange,
                                                new SiteImpl(new SiteId(station.getId().network_id,
                                                                        station.get_code(),
                                                                        xml.getLocCode(),
                                                                        convertTime(chanEpoch.getEndDate())),
                                                             new Location(chanEpoch.getLat(),
                                                                          chanEpoch.getLon(),
                                                                          new QuantityImpl(chanEpoch.getElevation(), UnitImpl.METER),
                                                                          new QuantityImpl(chanEpoch.getDepth(), UnitImpl.METER),
                                                                          LocationType.GEOGRAPHIC),
                                                             chanTimeRange,
                                                             station, UNKNOWN));
            QuantityImpl sensitivity = null;
            if ( chanEpoch.getInstrumentSensitivity() != null) {
            
                sensitivity = new QuantityImpl(chanEpoch.getInstrumentSensitivity().getSensitivityValue(),
                                                            convertUnit(chanEpoch.getInstrumentSensitivity().getSensitivityUnits()) );
            } else {
                logger.warn("No sensitivity for "+ChannelIdUtil.toStringFormatDates(chan.getId()));
            }
            out.add(new ChannelSensitivityBundle(chan, sensitivity));
        }
        return out;
    }
    
    public static UnitImpl convertUnit(String xml) throws StationXMLException {
        String unitString;
        if (xml.indexOf(" - ") == -1) {
            unitString = xml.substring(0, xml.indexOf(" - ")).trim();
        } else {
            unitString = xml.trim(); // probably won't work, but might as well try
        }
        if (unitString.equalsIgnoreCase("M")) {
            return UnitImpl.METER;
        } else if (unitString.equalsIgnoreCase("M/S")) {
            return UnitImpl.METER_PER_SECOND;
        } else if (unitString.equalsIgnoreCase("M/S/S")) {
            return UnitImpl.METER_PER_SECOND_PER_SECOND;
        } else {
            throw new StationXMLException("Unknown unit: "+xml);
        }
    }
    
    public static Time convertTime(String xml) {
        return new MicroSecondDate(xml).getFissuresTime();
    }
    
    public static void main(String[] args) throws FileNotFoundException, XMLStreamException, StationXMLException {
        if (args.length == 0) {
            System.out.println("Usage: styxprint filename.xml");
            return;
        }
        String filename = args[0];
        XMLInputFactory factory = XMLInputFactory.newInstance();
        XMLEventReader r = factory.createXMLEventReader(filename,
                                                        new FileInputStream(filename));
        XMLEvent e = r.peek();
        while(! e.isStartElement()) {
            System.out.println(e);
            e = r.nextEvent(); // eat this one
            e = r.peek();  // peek at the next
        }
        System.out.println("StaMessage");
        StaMessage staMessage = new StaMessage(r);
        System.out.println("Source: "+staMessage.getSource());
        System.out.println("Sender: "+staMessage.getSender());
        System.out.println("Module: "+staMessage.getModule());
        System.out.println("SentDate: "+staMessage.getSentDate());
        StationIterator it = staMessage.getStations();
        while(it.hasNext()) {
            Station s = it.next();
            System.out.println("XML Station: "+s.getNetCode()+"."+s.getStaCode()+" "+s.getStationEpochs().size());
            List<StationImpl> sta = convert(s);
            for (StationImpl stationImpl : sta) {
                System.out.println("Station: "+stationImpl);
            }
        }
        System.out.println("Done station iterate");
    }
    
    public static final String UNKNOWN = "";
    
    public static final TimeInterval ONE_SECOND = new TimeInterval(1, UnitImpl.SECOND);
    
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(StationXMLToFissures.class);
}
