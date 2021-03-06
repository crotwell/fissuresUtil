package edu.sc.seis.fissuresUtil.rt130;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.PropertyConfigurator;

import edu.iris.Fissures.Location;
import edu.iris.Fissures.LocationType;
import edu.iris.Fissures.IfNetwork.NetworkAttr;
import edu.iris.Fissures.IfNetwork.Site;
import edu.iris.Fissures.IfNetwork.SiteId;
import edu.iris.Fissures.IfNetwork.StationId;
import edu.iris.Fissures.model.LocationUtil;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.network.SiteIdUtil;
import edu.iris.Fissures.network.SiteImpl;
import edu.iris.Fissures.network.StationImpl;
import edu.sc.seis.fissuresUtil.database.seismogram.PopulationProperties;
import edu.sc.seis.fissuresUtil.simple.Initializer;
import edu.sc.seis.fissuresUtil.time.MicroSecondTimeRange;

public class NCReader {

    public NCReader(Properties props) throws IOException {
        this(PopulationProperties.getNetworkAttr(props), XYReader.create(props));
        PropParser pp = new PropParser(props);
        load(new FileInputStream(pp.getPath(NCReader.NC_FILE_LOC)));
    }

    /**
     * All items created by this DeluxeNCFile will be part of Network net.
     * initialLocations should be a mapping between string station codes and
     * Location objects like the map produced by XYReader
     */
    public NCReader(NetworkAttr net, Map initialLocations) {
        this.net = net;
        this.locs = initialLocations;
    }

    public void load(InputStream input) throws IOException {
        load(new InputStreamReader(input));
    }

    public void load(Reader source) throws IOException {
        load(new BufferedReader(source));
    }

    public void load(BufferedReader source) throws IOException {
        String line;
        int num = 0;
        while((line = source.readLine()) != null) {
            num++;
            for(int i = 0; i < handlers.length; i++) {
                try {
                    if(handlers[i].handle(line)) {
                        break;
                    }
                } catch(FormatException fe) {
                    fe.setLine(line);
                    fe.setLineNum(num);
                    throw fe;
                }
            }
        }
    }

    public List getSites() {
        return sites;
    }

    public void dumpSites() {
        DateFormat df = new SimpleDateFormat("yyyy:DDD:HH:mm:ss");
        df.setTimeZone(TimeZone.getTimeZone("GMT"));
        Pattern instrumentationParser = Pattern.compile(NCReader.INSTRUMENT_RE);
        System.out.println("station_code das_id sensor latitude longitude elevation orientation start_time end_time moved");
        Iterator it = sites.iterator();
        MicroSecondDate now = new MicroSecondDate();
        while(it.hasNext()) {
            Site cur = (Site)it.next();
            MicroSecondDate end = new MicroSecondDate(cur.getEndTime());
            String endstr = "ongoing";
            if(!end.after(now)) {
                endstr = df.format(end);
            }
            Location loc = cur.getLocation();
            Matcher m = instrumentationParser.matcher(cur.getComment());
            if(!m.matches()) {
                throw new RT130FormatError(SiteIdUtil.toString(cur.get_id())
                        + " has a malformed instrumentation specification '"
                        + cur.getComment() + "'");
            }
            String orientations = m.group(2);
            System.out.println(cur.getStation().get_code()
                    + " "
                    + DASChannelCreator.getUnitId(cur)
                    + " "
                    + DASChannelCreator.getSensorId(cur)
                    + " "
                    + loc.latitude
                    + " "
                    + loc.longitude
                    + " "
                    + loc.elevation.value
                    + " "
                    + orientations
                    + " "
                    + df.format(new MicroSecondDate(cur.getBeginTime()))
                    + " " + endstr);
        }
    }

    public String getUnitId(MicroSecondDate startTime, String stationCode) {
        Iterator it = sites.iterator();
        while(it.hasNext()) {
            Site cur = (Site)it.next();
            if(cur.getStation().get_code().equals(stationCode)
                    && new MicroSecondTimeRange(cur.getEffectiveTime()).contains(startTime)) {
                return DASChannelCreator.getUnitId(cur);
            }
        }
        throw new IllegalArgumentException("No station of code '" + stationCode
                + "' operating at " + startTime);
    }

    private static class LineHandler {

        public boolean handle(String line) {
            return true;
        }
    }

    private static class UnmatchedGatherer extends LineHandler {

        public boolean handle(String line) {
            unhandledLines.add(line);
            System.out.println("MISSED '" + line + "'");
            return true;
        }

        List unhandledLines = new ArrayList();
    }

    public int getNumUnhandledLines() {
        return unmatched.unhandledLines.size();
    }

    public String getUnhandledLine(int index) {
        return (String)unmatched.unhandledLines.get(index);
    }

    public class REHandler extends LineHandler {

        public REHandler(String source) {
            // Any line can end with a comment
            pattern = Pattern.compile(source + commentRE);
        }

        public boolean handle(String line) {
            Matcher m = pattern.matcher(line);
            if(m.matches()) {
                operate(m);
                return true;
            }
            return false;
        }

        public void operate(Matcher m) {}

        private Pattern pattern;
    }

    public class BlankAndCommentHandler extends REHandler {

        // Gets pure whitespace and comment lines since all REHandlers allow
        // that at the end of a line
        public BlankAndCommentHandler() {
            super("");
        }
    }

    public class StartBlockHandler extends REHandler {

        public StartBlockHandler() {
            super("START (\\d{4}):(\\d{3})(:(\\d{2}))?(:(\\d{2}))?(:(\\d{2}))?");
        }

        public void operate(Matcher m) {
            if(blockStart != null) {
                throw new FormatException("Encountered a 'START' before seeing an 'END' for the previous 'START'");
            }
            scratchCal.clear();
            scratchCal.set(Calendar.YEAR, Integer.parseInt(m.group(1)));
            scratchCal.set(Calendar.DAY_OF_YEAR, Integer.parseInt(m.group(2)));
            if(m.group(4) != null) {
                scratchCal.set(Calendar.HOUR_OF_DAY,
                               Integer.parseInt(m.group(4)));
            }
            if(m.group(6) != null) {
                scratchCal.set(Calendar.MINUTE, Integer.parseInt(m.group(6)));
            }
            if(m.group(8) != null) {
                scratchCal.set(Calendar.SECOND, Integer.parseInt(m.group(8)));
            }
            blockStart = new MicroSecondDate(scratchCal.getTime());
        }
    }

    public class EndBlockHandler extends REHandler {

        public EndBlockHandler() {
            super("END");
        }

        public void operate(Matcher m) {
            if(blockStart == null) {
                throw new FormatException("Encountered an 'END' without a corresponding 'START'");
            }
            Iterator it = activeSites.iterator();
            while(it.hasNext()) {
                Site cur = (Site)it.next();
                if(!blockSites.contains(cur)) {
                    cur.getEffectiveTime().end_time = blockStart.getFissuresTime();
                    cur.getStation().getEffectiveTime().end_time = blockStart.getFissuresTime();
                    it.remove();
                }
            }
            for(int i = 0; i < blockSites.size(); i++) {
                Site ithSite = (Site)blockSites.get(i);
                for(int j = i + 1; j < blockSites.size(); j++) {
                    Site jthSite = (Site)blockSites.get(j);
                    if(ithSite.getStation().get_code()
                            .equals(jthSite.getStation().get_code())) {
                        throw new FormatException("The station code '"
                                + ithSite.getStation().get_code()
                                + "' appears twice in this block");
                    }
                    if(DASChannelCreator.getUnitId(jthSite)
                            .equals(DASChannelCreator.getUnitId(ithSite))) {
                        throw new FormatException("The DAS Id '"
                                + DASChannelCreator.getUnitId(jthSite)
                                + "' appears twice in this block");
                    }
                }
            }
            blockStart = null;
            blockSites.clear();
        }
    }

    /**
     * Matches the orientation part of a station line in an nc file. Just
     * contains 1 group, the entire string
     */
    public static final String ORIENTATION_RE = "(default|\\d/-?\\d+/-?\\d+:\\d/-?\\d+/-?\\d+:\\d/-?\\d+/-?\\d+)";

    /**
     * Matches the entire instrumentation portion of a station line. Contains
     * two groups. The Fist is the entire string and the second is the
     * orientation portion.
     */
    public static final String INSTRUMENT_RE = "(\\w+/\\d+\\s+\\w+/\\w+\\s+"
            + ORIENTATION_RE + ")";

    /**
     * Matches a LOC specifier on a station line. The entire RE is optional so
     * this will match nothing as well. There are four groups, the first is the
     * entire string so check it for null to see if this matched anything Groups
     * 2-4 are the latitude, longitude and elevation in meters respectively.
     */
    public static final String LOC_RE = "(\\s+LOC: (-?\\d+.\\d+) (-?\\d+.\\d+) (\\d+))?";

    public class StationHandler extends REHandler {

        public StationHandler() {
            super("(\\w+)\\s+" + INSTRUMENT_RE + LOC_RE);
        }

        public void operate(Matcher m) {
            if(blockStart == null) {
                throw new FormatException("Encountered a station line before hitting a 'START' line");
            }
            String code = m.group(1);
            StationId staId = new StationId(net.get_id(),
                                            code,
                                            blockStart.getFissuresTime());
            String instrumentInfo = m.group(2);
            if(!locs.containsKey(code)){
                throw new FormatException("There's an entry for station '"+code+"' in the nc file but not in the xy file");
            }
            StationImpl sta = new StationImpl(staId,
                                              code,
                                              (Location)locs.get(code),
                                              net.getOwner(),
                                              "",
                                              "",
                                              net);
            SiteId siteId = new SiteId(net.get_id(),
                                       code,
                                       "00",
                                       blockStart.getFissuresTime());
            Location siteLoc;
            String lat = m.group(5);
            String lon = m.group(6);
            String elev = m.group(7);
            if(elev != null) {// That means we matched a LOC
                siteLoc = new Location(Float.parseFloat(lat),
                                       Float.parseFloat(lon),
                                       new QuantityImpl(Float.parseFloat(elev),
                                                        UnitImpl.METER),
                                       new QuantityImpl(0.0, UnitImpl.METER),
                                       LocationType.GEOGRAPHIC);
            } else {
                siteLoc = sta.getLocation();
            }
            SiteImpl site = new SiteImpl(siteId, siteLoc, sta, instrumentInfo);
            Iterator it = activeSites.iterator();
            int siteCount = 0;
            while(it.hasNext()) {
                Site s = (Site)it.next();
                if(s.getStation().get_code().equals(code)) {
                    if(s.getComment().equals(site.getComment())
                            && LocationUtil.areEqual(siteLoc, s.getLocation())) {
                        blockSites.add(s);
                        return;
                    } else {// A new active site at this station, bump our site
                        // code
                        siteCount++;
                    }
                }
            }
            site.get_id().site_code = "0" + siteCount;
            it = sites.iterator();
            while(it.hasNext()) {
                Site cur = (Site)it.next();
                if(cur.getStation().get_code().equals(code)) {
                    site.setStation(cur.getStation());
                    break;
                }
            }
            sites.add(site);
            activeSites.add(site);
            blockSites.add(site);
        }
    }

    public class FormatException extends RuntimeException {

        public FormatException(String message) {
            super(message);
        }

        public void setLine(String line) {
            this.line = line;
        }

        public void setLineNum(int lineNum) {
            this.lineNum = lineNum;
        }

        public int getLineNum() {
            return lineNum;
        }

        public String getMessage() {
            if(line != null) {
                return super.getMessage() + "\nLine " + lineNum + ": " + line;
            }
            return super.getMessage();
        }

        private String line;

        private int lineNum;

        private static final long serialVersionUID = 1L;
    }

    private Map locs;

    private NetworkAttr net;

    private List sites = new ArrayList();

    private List activeSites = new ArrayList();

    private List blockSites = new ArrayList();

    private MicroSecondDate blockStart;

    private UnmatchedGatherer unmatched = new UnmatchedGatherer();

    private String commentRE = "\\s*(#.*)?";

    private LineHandler[] handlers = new LineHandler[] {new StationHandler(),
                                                        new BlankAndCommentHandler(),
                                                        new StartBlockHandler(),
                                                        new REHandler("name\\s+DAS/chan\\s+sensor/model\\s+chan/dip/azi"),
                                                        new EndBlockHandler(),
                                                        unmatched};

    private Calendar scratchCal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));

    public static final String NC_FILE_LOC = "NCFile";

    public static void main(String[] args) throws IOException {
        Properties props = Initializer.loadProperties(args);
        PropertyConfigurator.configure(props);
        try {
            new NCReader(props).dumpSites();
        } catch(FormatException fe) {
            System.err.println(fe.getMessage());
            System.exit(1);
        }
    }
}
