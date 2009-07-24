package edu.sc.seis.fissuresUtil.hibernate;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.Query;

import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfSeismogramDC.LocalSeismogram;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.iris.Fissures.seismogramDC.SeismogramAttrImpl;
import edu.sc.seis.fissuresUtil.bag.EncodedCut;
import edu.sc.seis.fissuresUtil.database.NotFound;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.fissuresUtil.rt130.FileNameParser;
import edu.sc.seis.fissuresUtil.rt130.LeapSecondApplier;
import edu.sc.seis.fissuresUtil.rt130.PacketType;
import edu.sc.seis.fissuresUtil.rt130.RT130FileReader;
import edu.sc.seis.fissuresUtil.rt130.RT130FormatException;
import edu.sc.seis.fissuresUtil.rt130.RT130ToLocalSeismogram;
import edu.sc.seis.fissuresUtil.time.ReduceTool;
import edu.sc.seis.fissuresUtil.xml.SeismogramFileTypes;
import edu.sc.seis.fissuresUtil.xml.URLDataSetSeismogram;

public class SeismogramFileRefDB extends AbstractHibernateDB {

    public void saveSeismogramToDatabase(ChannelImpl channel,
                                         SeismogramAttrImpl seis,
                                         String fileLocation,
                                         SeismogramFileTypes filetype) {
        saveSeismogramToDatabase(new SeismogramFileReference(channel, seis, fileLocation, filetype));
    }
    
    public void saveSeismogramToDatabase(SeismogramFileReference seisRef) {
        getSession().save(seisRef);
    }

    public RequestFilter[] findMatchingSeismograms(RequestFilter[] requestArray,
                                                   boolean ignoreNetworkTimes) {
        List results = queryDatabaseForSeismograms(requestArray,
                                                   false,
                                                   ignoreNetworkTimes);
        RequestFilter[] request = (RequestFilter[])results.toArray(new RequestFilter[results.size()]);
        RequestFilter[] reduced = ReduceTool.merge(request);
        return reduced;
    }

    public LocalSeismogramImpl[] getMatchingSeismograms(RequestFilter[] requestArray,
                                                    boolean ignoreNetworkTimes) {
        List results = queryDatabaseForSeismograms(requestArray,
                                                   true,
                                                   ignoreNetworkTimes);
        LocalSeismogramImpl[] seis = (LocalSeismogramImpl[])results.toArray(new LocalSeismogramImpl[0]);
        LocalSeismogramImpl[] reduced = ReduceTool.merge(seis);
        return reduced;
    }

    public List queryDatabaseForSeismograms(RequestFilter[] request,
                                            boolean returnSeismograms,
                                            boolean ignoreNetworkTimes) {
        RequestFilter[] minimalRequest = ReduceTool.merge(request);
        List resultCollector = new ArrayList();
        for(int i = 0; i < minimalRequest.length; i++) {
            queryDatabaseForSeismogram(resultCollector,
                                       minimalRequest[i],
                                       returnSeismograms,
                                       ignoreNetworkTimes);
        }
        return resultCollector;
    }

    private void queryDatabaseForSeismogram(List resultCollector,
                                            RequestFilter request,
                                            boolean returnSeismograms,
                                            boolean ignoreNetworkTimes) {
        // Retrieve channel ID, begin time, and end time from the request
        // and place the times into a time table while
        // buffering the query by one second on each end.
        ChannelImpl chanId;
        EncodedCut cutter = new EncodedCut(request);
        try {
            if(ignoreNetworkTimes) {
                chanId = chanTable.getChannel(request.channel_id.network_id.network_code,
                                              request.channel_id.station_code,
                                              request.channel_id.site_code,
                                              request.channel_id.channel_code,
                                              new MicroSecondDate(request.start_time));
            } else {
                chanId = chanTable.getChannel(request.channel_id);
            }
        } catch(NotFound e) {
            logger.warn("Can not find channel ID in database.");
            return;
        }
        MicroSecondDate adjustedBeginTime = new MicroSecondDate(request.start_time).subtract(ONE_SECOND);
        MicroSecondDate adjustedEndTime = new MicroSecondDate(request.end_time).add(ONE_SECOND);
        String query = "from "
                + SeismogramFileReference.class.getName()
                + " where netCode = :netCode and staCode = :staCode and siteCode = :siteCode and chanCode = :chanCode "
                + " and beginTime < :end and endTime >= :begin";
        // Populate databaseResults with all of the matching seismograms
        // from the database.
        Query q = getSession().createQuery(query);
        ChannelId chanIdxxx = chanId.getId();
        q.setString("netCode", chanIdxxx.network_id.network_code);
        q.setString("staCode", chanIdxxx.station_code);
        q.setString("siteCode", chanIdxxx.site_code);
        q.setString("chanCode", chanIdxxx.channel_code);
        q.setTimestamp("end", adjustedEndTime.getTimestamp());
        q.setTimestamp("begin", adjustedBeginTime.getTimestamp());
        List<SeismogramFileReference> databaseResults = q.list();
        if(returnSeismograms) {
            for(SeismogramFileReference seisRef : databaseResults) {
                try {
                    File seismogramFile = new File(seisRef.getFilePath());
                    SeismogramFileTypes filetype = SeismogramFileTypes.fromInt(seisRef.getFileType());
                    LocalSeismogramImpl[] curSeis;
                    if(filetype.equals(SeismogramFileTypes.RT_130)) {
                        List refTekSeis = getMatchingSeismogramsFromRefTek(seismogramFile.getCanonicalPath(),
                                                                           request.channel_id,
                                                                           adjustedBeginTime,
                                                                           adjustedEndTime);
                        curSeis = (LocalSeismogramImpl[])refTekSeis.toArray(new LocalSeismogramImpl[refTekSeis.size()]);
                    } else {
                        URLDataSetSeismogram urlSeis = new URLDataSetSeismogram(seismogramFile.toURL(),
                                                                                filetype);
                        curSeis = urlSeis.getSeismograms();
                    }
                    for(int j = 0; j < curSeis.length; j++) {
                        LocalSeismogramImpl seis = cutter.apply(curSeis[j]);
                        if(seis != null) {
                            resultCollector.add(seis);
                        }
                    }
                } catch(Exception e) {
                    GlobalExceptionHandler.handle("Problem occured while returning seismograms from the database."
                                                          + "\n"
                                                          + "The problem file is located at "
                                                          + seisRef.getFilePath(),
                                                  e);
                }
            }
        } else {
            for(SeismogramFileReference seisRef : databaseResults) {
                RequestFilter req = new RequestFilter(chanIdxxx,
                                                      new MicroSecondDate(seisRef.getBeginTime()).getFissuresTime(),
                                                      new MicroSecondDate(seisRef.getEndTime()).getFissuresTime());
                req = cutter.apply(req);
                if(req != null) {
                    resultCollector.add(req);
                }
            }
        }
    }

    private List getMatchingSeismogramsFromRefTek(String seismogramFile,
                                                  ChannelId chanId,
                                                  MicroSecondDate beginTime,
                                                  MicroSecondDate endTime)
            throws IOException, RT130FormatException {
        RT130FileReader toSeismogramDataPacket = new RT130FileReader();
        File file = new File(seismogramFile);
        String fileName = file.getName();
        String unitIdNumber = file.getParentFile().getParentFile().getName();
        String yearAndDay = file.getParentFile()
                .getParentFile()
                .getParentFile()
                .getName();
        MicroSecondDate fileBeginTime = FileNameParser.getBeginTime(yearAndDay,
                                                                    fileName);
        fileBeginTime = LeapSecondApplier.applyLeapSecondCorrection(unitIdNumber,
                                                                    fileBeginTime);
        // fileTimeWindow is calculated using the begin time from the file name
        // and end time from the request. This is not the typical, or an ideal
        // method. The typical method involves obtaining the begin time of the
        // file from the file name, and the end time by adding the nominal
        // length of data from the props file. This method is not used to
        // allow this object to keep its ignorance of the props file.
        PacketType[] seismogramDataPacketArray = toSeismogramDataPacket.processRT130Data(seismogramFile,
                                                                                         true,
                                                                                         new MicroSecondTimeRange(fileBeginTime,
                                                                                                                  endTime));
        // Use default constructor
        RT130ToLocalSeismogram toSeismogram = new RT130ToLocalSeismogram();
        LocalSeismogramImpl[] seis = toSeismogram.convert(seismogramDataPacketArray);
        List matchingSeis = new ArrayList();
        for(int i = 0; i < seis.length; i++) {
            // Check to make sure the seismograms returned fall within the
            // requested time window, and compare the channel code of the
            // channel requested with the channel code of the dummy channel
            // created during the above file reading. If the codes are equal,
            // set the dummy channel equal to the channel requested (real
            // channel).
            if(seis[i].channel_id.channel_code.equals(chanId.channel_code)
                    && seis[i].getBeginTime().before(endTime)
                    && seis[i].getEndTime().after(beginTime)) {
                seis[i].channel_id = chanId;
                matchingSeis.add(seis[i]);
            }
        }
        return matchingSeis;
    }


    public int removeSeismogramFromDatabase(String seisFile) {
        String query = "delete  "
            + SeismogramFileReference.class.getName()
            + " where filePath = "+seisFile;
        return getSession().createQuery(query).executeUpdate();
    }
    
    private static SeismogramFileRefDB singleton;

    public static SeismogramFileRefDB getSingleton() {
        if(singleton == null) {
            singleton = new SeismogramFileRefDB();
        }
        return singleton;
    }

    protected NetworkDB chanTable = NetworkDB.getSingleton();

    private static final TimeInterval ONE_SECOND = new TimeInterval(1,
                                                                    UnitImpl.SECOND);

    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(SeismogramFileRefDB.class);
}
