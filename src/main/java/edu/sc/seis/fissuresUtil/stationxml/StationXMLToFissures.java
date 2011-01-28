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
import edu.iris.Fissures.Time;
import edu.iris.Fissures.TimeRange;
import edu.iris.Fissures.IfNetwork.NetworkId;
import edu.iris.Fissures.IfNetwork.StationId;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.TimeUtils;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.network.NetworkAttrImpl;
import edu.iris.Fissures.network.StationImpl;
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
    
    public static List<StationImpl> convert(Station xml) {
        NetworkAttrImpl attr = new NetworkAttrImpl(new NetworkId(xml.getNetCode(), TimeUtils.timeUnknown), UNKNOWN, UNKNOWN, UNKNOWN);
        MicroSecondDate minStationStart = null;
        List<StationImpl> out = new ArrayList<StationImpl>();
        Iterator<StationEpoch> it = xml.getStationEpochs().iterator();
        while (it.hasNext()) {
            StationImpl sta = convert(it.next(), attr, xml.getStaCode());
            out.add(sta);
            MicroSecondDate staBegin = new MicroSecondDate(sta.getBeginTime());
            if (minStationStart == null || minStationStart.after(staBegin)) {
                minStationStart = staBegin;
            }
        }
        attr.setBeginTime(minStationStart.getFissuresTime());
        return out;
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
    
}
