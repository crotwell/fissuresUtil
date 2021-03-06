package edu.sc.seis.fissuresUtil.display;

import java.util.List;
import java.util.TimeZone;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.EventAttr;
import edu.iris.Fissures.IfEvent.Magnitude;
import edu.iris.Fissures.IfEvent.NoPreferredOrigin;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.IfNetwork.Station;
import edu.iris.Fissures.model.ISOTime;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.TauP.Arrival;
import edu.sc.seis.TauP.NoSuchLayerException;
import edu.sc.seis.TauP.NoSuchMatPropException;
import edu.sc.seis.TauP.SphericalCoords;
import edu.sc.seis.TauP.TauModelException;
import edu.sc.seis.TauP.VelocityModel;
import edu.sc.seis.fissuresUtil.bag.TauPUtil;
import edu.sc.seis.fissuresUtil.chooser.ThreadSafeDecimalFormat;
import edu.sc.seis.fissuresUtil.chooser.ThreadSafeSimpleDateFormat;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;

/**
 * EventInfoDisplay.java
 *
 *
 * Created: Fri May 31 10:01:21 2002
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version $Id: EventInfoDisplay.java 22222 2011-03-23 18:13:42Z crotwell $
 */

public class EventInfoDisplay extends TextInfoDisplay{

    public EventInfoDisplay (){
        taup = TauPUtil.getTauPUtil();
    }

    public void displayEvent(EventAccessOperations event) {
        displayEventStation(event, null);
    }

    public void displayEventStation(EventAccessOperations event, Station[] station) {
        Document doc = textPane.getDocument();
        try {
            doc.remove(0, doc.getLength());
            appendEvent(event, doc);
            if (station != null) {
                appendEventStation(event, station, doc);
            } // end of if (station != null)

            toTop();
        } catch (BadLocationException ble) {
            System.err.println("Couldn't insert message.");
        }
    }

    public void appendEvent(EventAccessOperations event) {
        try {
            appendEvent(event, getDocument());
        } catch (BadLocationException ble) {
            System.err.println("Couldn't insert message.");
        }
    }

    public void appendEventStation(EventAccessOperations event, Station[] station) {
        try {
            Document doc = getDocument();
            appendEvent(event, doc);
            appendEventStation(event, station, getDocument());
        } catch (BadLocationException ble) {
            System.err.println("Couldn't insert message.");
        }
    }

    protected void appendEvent(EventAccessOperations event, Document doc)
        throws BadLocationException {
        if ( event != null) {
            appendEventAttr(event.get_attributes(), doc);
            try {
                appendOrigin(event.get_preferred_origin(), doc);
            } catch (NoPreferredOrigin e) {

            } // end of try-catch
        } else {
            appendLine(doc, "No earthquake to display.");
        } // end of else


    }

    protected void appendEventStation(EventAccessOperations event,
                                      Station[] station,
                                      Document doc)
        throws BadLocationException {

        edu.sc.seis.TauP.SphericalCoords sph =
            new edu.sc.seis.TauP.SphericalCoords();
        try{
            station = sortStationsByDistance(station, event, sph);
        }
        catch(Exception e){
            GlobalExceptionHandler.handle("Problem sorting stations by distance", e);
        }

        String[] stationNames = new String[station.length + 1];
        stationNames[0] = "Station Name";
        int longest = stationNames[0].length();
        for (int i = 0; i < station.length; i++) {
            stationNames[i + 1] = station[i].getName();
            if (stationNames[i + 1].length() > longest){
                longest = stationNames[i + 1].length();
            }
        }


        appendLine(doc, "");
        appendHeader(doc, "Event to Station");
        double dist = -1;
        double baz = -1;
        double az = -1;
        appendLabelValue(doc, printTextLine(' ', longest), "\t\t\t\t\tAzimuth\tAzimuth\tFirst P\tFirst P\t");
        appendLabelValue(doc,
                         stationNames[0] + printTextLine(' ', longest - stationNames[0].length()),
                         "\t Lat\tLong\tDist\t Dist\t  to\t from\ttakeoff\tray");
        appendLabelValue(doc, printTextLine(' ', longest), "\t\t\t\t\t Event\t Event\tangle\tparam");
        appendLabelValue(doc, printTextLine(' ', longest), "\t(deg)\t(deg)\t(deg)\t (km)\t (deg)\t (deg)\t(deg)\t(s/deg)");
        appendLabelValue(doc, printTextLine('-', longest), "---------------------------------------------------------------------------------");

        for (int i=0; i<station.length; i++) {
            try {
                if ( event != null) {
                    Origin origin = event.get_preferred_origin();
                    dist = SphericalCoords.distance(origin.getLocation().latitude,
                                        origin.getLocation().longitude,
                                        station[i].getLocation().latitude,
                                        station[i].getLocation().longitude);
                    baz = SphericalCoords.azimuth(station[i].getLocation().latitude,
                                      station[i].getLocation().longitude,
                                      origin.getLocation().latitude,
                                      origin.getLocation().longitude);
                    az = SphericalCoords.azimuth(origin.getLocation().latitude,
                                     origin.getLocation().longitude,
                                     station[i].getLocation().latitude,
                                     station[i].getLocation().longitude);
                    String firstPTakeoff = "";
                    String firstPRayParam = "";
                    try {
                        List<Arrival> a = taup.calcTravelTimes(station[i], origin, new String[] { "ttp" } );
                        if (a.size() > 0) {
                            firstPRayParam = twoDecimal.format((a.get(0).getRayParam()*Math.PI/180));
                            double originDepth = QuantityImpl.createQuantityImpl(origin.getLocation().depth).convertTo(UnitImpl.KILOMETER).get_value();
                            VelocityModel vmod = taup.getTauModel().getVelocityModel();
                            firstPTakeoff = twoDecimal.format((180/Math.PI)*Math.asin((a.get(0).getRayParam()*vmod.evaluateBelow(originDepth, 'P'))/(vmod.getRadiusOfEarth()-originDepth)));
                        }
                    } catch (TauModelException e) {
                        // oh well, just use blank strings
                        GlobalExceptionHandler.handle("Trouble calculating travel times for "+station[i].get_code()+" "+event.get_attributes().name, e);
                    } catch (NoSuchLayerException e) {
                        // oh well, just use blank strings
                        GlobalExceptionHandler.handle("Trouble calculating travel times for "+station[i].get_code()+" "+event.get_attributes().name, e);
                    } catch (NoSuchMatPropException e) {
                        // oh well, just use blank strings
                        GlobalExceptionHandler.handle("Trouble calculating travel times for "+station[i].get_code()+" "+event.get_attributes().name, e);

                    }
                    appendLabelValue(doc, stationNames[i+1] + printTextLine(' ', longest - stationNames[i+1].length()),
                                     '\t' + twoDecimal.format(station[i].getLocation().latitude)+'\t'+
                                         twoDecimal.format(station[i].getLocation().longitude)+ '\t'+
                                         twoDecimal.format(dist)+ '\t'+
                                         twoDecimal.format(dist*111.19)+ '\t'+
                                         twoDecimal.format(baz)+ '\t'+
                                         twoDecimal.format(az)+ '\t'+
                                         firstPTakeoff+ '\t'+
                                         firstPRayParam+ '\t'
                                    );
                } else {
                    appendLabelValue(doc, stationNames[i+1] + printTextLine(' ', longest - stationNames[i+1].length()),
                                     twoDecimal.format(station[i].getLocation().latitude)+
                                         " "+twoDecimal.format(station[i].getLocation().longitude)+
                                         "--- ,  ---");

                } // end of else

            } catch (NoPreferredOrigin e) {
                appendLabelValue(doc, station[i].get_code(),
                                 twoDecimal.format(station[i].getLocation().latitude)+
                                     " "+twoDecimal.format(station[i].getLocation().longitude)+
                                     "--- ,  ---");
            } // end of try-catch

        } // end of for (int i=0; i<station.length; i++)
        appendLine(doc, "");
    }


    protected void appendEventAttr(EventAttr attr)
        throws BadLocationException {
        appendEventAttr(attr, getDocument());
    }

    protected void appendEventAttr(EventAttr attr, Document doc)
        throws BadLocationException {
        appendHeader(doc, "Event");
        appendLabelValue(doc, "Name\t", attr.name);
        if (attr.region.number > 0) {
            appendLabelValue(doc, "Region\t", feRegions.getRegionName(attr.region)+" ("+attr.region.number+")");
        } else {
            appendLabelValue(doc, "Region\t", "Unknown ("+attr.region.number+")");
        } // end of else


        appendLine(doc, "");
    }

    protected void appendOrigin(Origin origin)
        throws BadLocationException {
        appendOrigin(origin, getDocument());
    }

    protected void appendOrigin(Origin origin, Document doc)
        throws BadLocationException {
        appendHeader(doc, "Origin");
        appendLabelValue(doc, "Location\t", "latitude="+
                             twoDecimal.format(origin.getLocation().latitude)+
                             ",  longitude="+
                             twoDecimal.format(origin.getLocation().longitude));
        MicroSecondDate oTime = new ISOTime(origin.getOriginTime().date_time).getDate();
        appendLabelValue(doc, "Time\t", dateFormat.format(oTime));
        QuantityImpl depth = (QuantityImpl)origin.getLocation().depth;
        depth = depth.convertTo(UnitImpl.KILOMETER);
        appendLabelValue(doc, "Depth\t",
                         twoDecimal.format(depth.value)+" kilometers");
        //  ((UnitImpl)depth.the_units).toString());
        //  appendLabelValue(doc, "ID", origin.get_id());
        //appendLabelValue(doc, "Catalog", origin.catalog);
        //appendLabelValue(doc, "Contributor", origin.contributor);

        appendLine(doc, "");
        for (int i=0; i<origin.getMagnitudes().length; i++) {
            appendMagnitude(origin.getMagnitudes()[i], doc);
        } // end of for (int i=0; i<origin.magnitudes.length; i++)

    }

    protected void appendMagnitude(Magnitude mag)
        throws BadLocationException {
        appendMagnitude(mag, getDocument());
    }

    protected void appendMagnitude(Magnitude mag, Document doc)
        throws BadLocationException {
        appendLabelValue(doc, "Magnitude\t", mag.value+" "+mag.type+"  "+mag.contributor);
    }


    public static Station[] sortStationsByDistance(Station[] stations,
                                                   EventAccessOperations event,
                                                   edu.sc.seis.TauP.SphericalCoords sph)
        throws NoPreferredOrigin{
        try{
            Station[] temp = new Station[stations.length];
            System.arraycopy(stations, 0, temp, 0, stations.length);

            int indexOfNextSmallest;

            for (int i = 0; i < temp.length - 1; i++) {
                indexOfNextSmallest = indexOfClosestStation(temp, i, event, sph);
                interchange(i, indexOfNextSmallest, temp);
            }

            return temp;
        }
        catch(NoPreferredOrigin n){
            logger.warn("Stations not sorted because event has no origin.");
            return stations;
        }
    }

    public static String printTextLine(char c, int length){
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < length; i++) {
            buf.append(c);
        }
        return buf.toString();
    }

    public static void interchange(int i, int j, Station[] s){
        Station temp;

        temp = s[i];
        s[i] = s[j];
        s[j] = temp;
    }

    public static int indexOfClosestStation(Station[] stations, int startIndex,
                                            EventAccessOperations event,
                                            edu.sc.seis.TauP.SphericalCoords sph)
        throws NoPreferredOrigin{

        int index = startIndex;
        double currentDistance = Double.POSITIVE_INFINITY;
        double closestDistance = Double.POSITIVE_INFINITY;
        Station currentStation;


        for (int i = startIndex; i < stations.length; i++){
            currentStation = stations[i];
            currentDistance = sph.distance(event.get_preferred_origin().getLocation().latitude,
                                           event.get_preferred_origin().getLocation().longitude,
                                           currentStation.getLocation().latitude,
                                           currentStation.getLocation().longitude);
            if (currentDistance < closestDistance){
                closestDistance = currentDistance;
                index = i;
            }
        }
        return index;
    }

    static ParseRegions feRegions = ParseRegions.getInstance();

    TauPUtil taup;
    ThreadSafeDecimalFormat twoDecimal = new ThreadSafeDecimalFormat("0.00");
    ThreadSafeSimpleDateFormat dateFormat = new ThreadSafeSimpleDateFormat("MMM dd, yyyy HH:mm:ss.S z", TimeZone.getTimeZone("GMT"));

    static Logger logger = LoggerFactory.getLogger(EventInfoDisplay.class);
}// EventInfoDisplay


