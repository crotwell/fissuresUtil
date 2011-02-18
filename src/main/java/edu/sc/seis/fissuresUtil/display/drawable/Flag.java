package edu.sc.seis.fissuresUtil.display.drawable;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.iris.Fissures.Time;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfNetwork.Station;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.TauP.Arrival;
import edu.sc.seis.TauP.TauModelException;
import edu.sc.seis.fissuresUtil.bag.TauPUtil;
import edu.sc.seis.fissuresUtil.cache.EventUtil;
import edu.sc.seis.fissuresUtil.display.DisplayUtils;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;
import edu.sc.seis.fissuresUtil.display.SeismogramDisplay;
import edu.sc.seis.fissuresUtil.display.TextTable;
import edu.sc.seis.fissuresUtil.display.UnitDisplayUtil;
import edu.sc.seis.fissuresUtil.display.registrar.AmpEvent;
import edu.sc.seis.fissuresUtil.display.registrar.TimeEvent;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;
import edu.sc.seis.fissuresUtil.xml.StdAuxillaryDataNames;
import edu.sc.seis.fissuresUtil.xml.XMLDataSet;

/**
 * FlagPlotter.java Created: Wed Jul 3 11:50:13 2002
 * 
 * @author <a href="mailto:">Charlie Groves </a>
 * @version
 */
public class Flag implements Drawable {

    public Flag(MicroSecondDate flagTime, String name) {
        this(flagTime, name, null);
    }

    public Flag(MicroSecondDate flagTime, String name, DrawableSeismogram seis) {
        this.flagTime = flagTime;
        this.name = name;
        this.seis = seis;
    }

    public void draw(Graphics2D canvas,
                     Dimension size,
                     TimeEvent timeEvent,
                     AmpEvent ampEvent) {
        if(visible) {
            MicroSecondTimeRange timeRange = timeEvent.getTime();
            if(seis != null) {
                if(timeEvent.contains(seis.getSeismogram())) {
                    timeRange = timeEvent.getTime(seis.getSeismogram());
                } else {
                    DataSetSeismogram[] seismo = {seis.getSeismogram()};
                    seis.getParent().getTimeConfig().add(seismo);
                    seis.getParent().repaint();
                    return;
                }
            }
            if(flagTime.before(timeRange.getBeginTime())
                    || flagTime.after(timeRange.getEndTime()))
                return;
            canvas.setFont(DisplayUtils.BOLD_FONT);
            int location = getFlagLocation(size, timeRange);
            Rectangle2D.Float stringBounds = new Rectangle2D.Float();
            stringBounds.setRect(canvas.getFontMetrics()
                    .getStringBounds(name, canvas));
            if(flag == null || prevDrawHeight != size.height) {
                synchronized(this) {
                    Area pole = new Area(new Rectangle(location,
                                                       0,
                                                       1,
                                                       size.height));
                    flag = new Area(new Rectangle(location,
                                                  0,
                                                  (int)(stringBounds.width + PADDING),
                                                  (int)(stringBounds.height + PADDING)));
                    flag.add(pole);
                    prevLocation = location;
                    prevDrawHeight = size.height;
                }
            } else {
                synchronized(flag) {
                    double xShift = location - prevLocation;
                    flag.transform(AffineTransform.getTranslateInstance(xShift,
                                                                        0));
                    prevLocation = location;
                }
            }
            canvas.setColor(color);
            canvas.fill(flag);
            canvas.setColor(Color.BLACK);
            canvas.setStroke(DisplayUtils.ONE_PIXEL_STROKE);
            canvas.draw(flag);
            if(SeismogramDisplay.PRINTING) {
                canvas.setColor(Color.WHITE);
            }
            canvas.drawString(name, location + PADDING / 2, stringBounds.height
                    - PADDING / 2);
        }
    }

    private int prevDrawHeight = 0;

    public int getFlagLocation(Dimension size, MicroSecondTimeRange timeRange) {
        double offset = flagTime.difference(timeRange.getBeginTime())
                .getValue()
                / timeRange.getInterval().getValue();
        int loc = (int)(offset * size.width);
        return loc;
    }

    public static Flag getFlagFromElement(Element el) {
        String name = el.getAttribute("name");
        logger.debug("Flag name: " + name);
        logger.debug("Flag time from element: " + el.getAttribute("time"));
        MicroSecondDate time = new MicroSecondDate(new Time(el.getAttribute("time"),
                                                            0));
        logger.debug("Flag time: " + time.getFissuresTime().date_time);
        return new Flag(time, name);
    }

    public static Element createFlagElement(String name, MicroSecondDate time)
            throws ParserConfigurationException {
        Document doc = XMLDataSet.getDocumentBuilder().newDocument();
        Element el = doc.createElement("pickFlag");
        el.setAttribute("name", name);
        el.setAttribute("time", time.getFissuresTime().date_time);
        return el;
    }

    public static TextTable getFlagData(DataSetSeismogram dss,
                                        EventAccessOperations event,
                                        String[] template) {
        List<Arrival> arrivals = null;
        TauPUtil taup = TauPUtil.getTauPUtil();
        arrivals = getArrivals(taup, dss, event);
        String[] header = getFlagDataHeader(template);
        TextTable table = new TextTable(header.length, true);
        Iterator it = dss.getAuxillaryDataKeys().iterator();
        while(it.hasNext()) {
            String cur = (String)it.next();
            List dataCells = new ArrayList();
            if(cur.startsWith(StdAuxillaryDataNames.PICK_FLAG)) {
                Flag flag = getFlagFromElement((Element)dss.getAuxillaryData(cur));
                for(int i = 0; i < template.length; i++) {
                    if(template[i].equals(NAME)) { //Flag Name
                        dataCells.add(flag.getName());
                    } else if(template[i].equals(TIME)) { //Flag Time
                        dataCells.add(formatTime(flag.getFlagTime()));
                    } else if(template[i].equals(CHANNEL)) { //Channel Id
                        ChannelId chanId = dss.getRequestFilter().channel_id;
                        dataCells.add(chanId.network_id.network_code + '.'
                                + chanId.station_code + '.' + chanId.site_code
                                + '.' + chanId.channel_code);
                    } else if(template[i].equals(EVENT_NAME)) { //Event Name
                        dataCells.add(EventUtil.getEventInfo(event,
                                                             EventUtil.LOC));
                    } else if(template[i].equals(EVENT_MAG)) { //Event
                        // Magnitude
                        dataCells.add(EventUtil.getEventInfo(event,
                                                             EventUtil.MAG));
                    } else if(template[i].equals(EVENT_ORIG)) { //Event Origin
                        // Time
                        dataCells.add(EventUtil.getEventInfo(event,
                                                             EventUtil.TIME));
                    } else if(template[i].equals(EVENT_DEPTH)) { //Event Depth
                        dataCells.add(EventUtil.getEventInfo(event,
                                                             EventUtil.DEPTH
                                                                     + ' '
                                                                     + EventUtil.DEPTH_UNIT));
                    } else if(template[i].equals(EVENT_LAT)) { //Event Latitude
                        dataCells.add(EventUtil.getEventInfo(event,
                                                             EventUtil.LAT));
                    } else if(template[i].equals(EVENT_LON)) { //Event
                        // Longitude
                        dataCells.add(EventUtil.getEventInfo(event,
                                                             EventUtil.LON));
                    } else if(template[i].equals(ORIGIN_DIFF)) { //flagTime-originTime
                        TimeInterval interval = getTimeDifferenceFromOrigin(flag,
                                                                            event);
                        QuantityImpl timeInSeconds = interval.convertTo(UnitImpl.SECOND);
                        dataCells.add(twoDecimal.format(timeInSeconds.get_value()));
                    } else if(template[i].equals(DISTANCE_FROM_ORIG)) { //Distance
                        // from
                        // Origin,
                        // if
                        // that
                        // wasn't
                        // obvious
                        QuantityImpl distance = DisplayUtils.calculateDistance(dss);
                        dataCells.add(UnitDisplayUtil.formatQuantityImpl(distance));
                    } else if(template[i].equals(BACK_AZIMUTH)) {
                        QuantityImpl backAz = DisplayUtils.calculateBackAzimuth(dss);
                        dataCells.add(twoDecimal.format(backAz.get_value()));
                    } else if(template[i].equals(TAUP_P)) {
                        if(arrivals != null && arrivals.size() > 0) {
                            dataCells.add(twoDecimal.format(getFirstPWaveInSeconds(arrivals).get_value()));
                        } else {
                            dataCells.add("...");
                        }
                    } else if(template[i].equals(TIME_DIFF_ORIG_P)) {
                        if(arrivals != null && arrivals.size() > 0) {
                            TimeInterval timeDiff = getTimeDifferenceFromOrigin(flag,
                                                                                event);
                            TimeInterval timeDiffTauPDiff = timeDiff.subtract(getFirstPWaveInSeconds(arrivals));
                            QuantityImpl timeDiffTauPDiffConverted = timeDiffTauPDiff.convertTo(UnitImpl.SECOND);
                            dataCells.add(twoDecimal.format(timeDiffTauPDiffConverted.get_value()));
                        } else {
                            dataCells.add("...");
                        }
                    }
                }
                table.addRow((String[])dataCells.toArray(new String[0]));
            }
        }
        return table;
    }

    static DecimalFormat twoDecimal = new DecimalFormat("0.00");

    private static List<Arrival> getArrivals(TauPUtil taup,
                                         DataSetSeismogram dss,
                                         EventAccessOperations event) {
        Station station = dss.getDataSet()
                .getChannel(dss.getRequestFilter().channel_id).getSite().getStation();
        Origin origin = EventUtil.extractOrigin(event);
        try {
            List<Arrival> arrivals = taup.calcTravelTimes(station,
                                                      origin,
                                                      new String[] {"ttp"});
            return arrivals;
        } catch(TauModelException e) {
            GlobalExceptionHandler.handle("problem calculating travel times", e);
        }
        return null;
    }

    private static TimeInterval getFirstPWaveInSeconds(List<Arrival> arrivals) {
        return new TimeInterval(arrivals.get(0).getTime(), UnitImpl.SECOND);
    }

    private static String formatTime(MicroSecondDate msd) {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:sss z");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        return sdf.format(msd);
    }

    //There definitely won't be a great need for this method once
    //possible data options are implemented, but until then, this
    //is here to weed out the weirdness.
    public static String[] getFlagDataHeader(String[] template) {
        List dataCells = new ArrayList();
        for(int i = 0; i < template.length; i++) {
            if(template[i].equals(NAME)) { //Flag Name
                dataCells.add(NAME);
            } else if(template[i].equals(TIME)) { //Flag Time
                dataCells.add(TIME);
            } else if(template[i].equals(CHANNEL)) { //Channel Id
                dataCells.add(CHANNEL);
            } else if(template[i].equals(EVENT_NAME)) { //Event Name
                dataCells.add(EVENT_NAME);
            } else if(template[i].equals(EVENT_MAG)) { //Event Magnitude
                dataCells.add(EVENT_MAG);
            } else if(template[i].equals(EVENT_ORIG)) { //Event Origin Time
                dataCells.add(EVENT_ORIG);
            } else if(template[i].equals(EVENT_DEPTH)) { //Event Depth
                dataCells.add(EVENT_DEPTH);
            } else if(template[i].equals(EVENT_LAT)) { //Event Latitude
                dataCells.add(EVENT_LAT);
            } else if(template[i].equals(EVENT_LON)) { //Event Longitude
                dataCells.add(EVENT_LON);
            } else if(template[i].equals(ORIGIN_DIFF)) { //flagTime-originTime
                dataCells.add(ORIGIN_DIFF);
            } else if(template[i].equals(DISTANCE_FROM_ORIG)) { //Distance from
                // Origin, if
                // that wasn't
                // obvious
                dataCells.add(DISTANCE_FROM_ORIG);
            } else if(template[i].equals(BACK_AZIMUTH)) {
                dataCells.add(BACK_AZIMUTH);
            } else if(template[i].equals(TAUP_P)) {
                dataCells.add(TAUP_P);
            } else if(template[i].equals(TIME_DIFF_ORIG_P)) {
                dataCells.add(TIME_DIFF_ORIG_P);
            }
        }
        return (String[])dataCells.toArray(new String[0]);
    }

    public static TimeInterval getTimeDifferenceFromOrigin(Flag flag,
                                                           EventAccessOperations event) {
        Origin origin = EventUtil.extractOrigin(event);
        MicroSecondDate originTime = new MicroSecondDate(origin.getOriginTime());
        MicroSecondDate flagTime = flag.getFlagTime();
        return originTime.difference(flagTime);
    }

    public String getName() {
        return name;
    }

    private Area flag;

    private int prevLocation;

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public void setVisibility(boolean b) {
        visible = b;
    }

    public MicroSecondDate getFlagTime() {
        return flagTime;
    }

    public void setFlagTime(MicroSecondDate flagTime) {
        this.flagTime = flagTime;
    }

    private Color color = Color.RED;

    private boolean visible = true;

    private MicroSecondDate flagTime;

    private String name;

    private DrawableSeismogram seis;

    //pixels of space of flag around the font
    private static final int PADDING = 4;

    private static Logger logger = LoggerFactory.getLogger(Flag.class.getName());

    //names for the data template
    public static final String NAME = "Flag Name";

    public static final String ORIGIN_DIFF = "Time from Origin (s)"; //flag

    // time
    // minus
    // origin
    // time
    public static final String TAUP_P = "TauP P Wave (s)";

    public static final String TIME_DIFF_ORIG_P = "Prediction Difference (s)";

    public static final String DISTANCE_FROM_ORIG = "Distance From Origin (deg)";

    public static final String BACK_AZIMUTH = "Back Azimuth (deg)";

    public static final String CHANNEL = "Channel";

    public static final String EVENT_NAME = "Event Name";

    public static final String EVENT_LAT = "Event Latitude";

    public static final String EVENT_LON = "Event Longitude";

    public static final String EVENT_DEPTH = "Event Depth (km)";

    public static final String EVENT_MAG = "Magnitude";

    public static final String EVENT_ORIG = "Origin Time";

    public static final String TIME = "Flag Time";
}// FlagPlotter
