package edu.sc.seis.fissuresUtil.database.seismogram;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.GanttRenderer;
import org.jfree.data.gantt.Task;
import org.jfree.data.gantt.TaskSeries;
import org.jfree.data.gantt.TaskSeriesCollection;
import com.lowagie.text.Document;
import com.lowagie.text.pdf.DefaultFontMapper;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.network.StationIdUtil;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;
import edu.sc.seis.fissuresUtil.time.ReduceTool;

public class RT130Report {

    /**
     * Creates a report that shows from the start of the data to its end.
     */
    public RT130Report() {
        this(null, null, null);
    }

    /**
     * Creates a report that shows from start to end. If start or end is null,
     * the start or end time of the data in the report is used
     */
    public RT130Report(Date start, Date end, Set stationCodesToDisplay) {
        this.start = start;
        this.end = end;
        this.stationCodesToDisplay = stationCodesToDisplay;
    }

    public void addRefTekSeismogram(Channel channel,
                                    MicroSecondDate beginTime,
                                    MicroSecondDate endTime) {
        if(!shouldDisplay(channel.get_id())) {
            return;
        }
        numRefTekEntries++;
        addSeismogram(channel, beginTime, endTime);
    }

    public void addSacSeismogram(Channel channel,
                                 MicroSecondDate beginTime,
                                 MicroSecondDate endTime) {
        if(!shouldDisplay(channel.get_id())) {
            return;
        }
        numSacFiles++;
        addSeismogram(channel, beginTime, endTime);
    }

    public void addMSeedSeismogram(ChannelId channelid,
                                   MicroSecondDate beginTime,
                                   MicroSecondDate endTime) {
        if(!shouldDisplay(channelid)) {
            return;
        }
        numMSeedFiles++;
        addSeismogram(channelid, beginTime, endTime);
    }

    private void addSeismogram(Channel channel,
                               MicroSecondDate beginTime,
                               MicroSecondDate endTime) {
        String channelIdString = getIdString(channel);
        channelIdStringToChannel.put(channelIdString, channel);
        channelIdStringToChannelId.put(channelIdString, channel.get_id());
        MicroSecondTimeRange timeRange = new MicroSecondTimeRange(beginTime,
                                                                  endTime);
        if(!channelIdWithTime.containsKey(channelIdString)) {
            channelIdWithTime.put(channelIdString, new ArrayList());
        }
        List list = ((List)channelIdWithTime.get(channelIdString));
        list.add(timeRange);
        if(list.size() > 1) {
            mergeTimes(list);
        }
    }

    private boolean shouldDisplay(ChannelId id) {
        return stationCodesToDisplay == null
                || stationCodesToDisplay.contains(id.station_code);
    }

    private void addSeismogram(ChannelId channelId,
                               MicroSecondDate beginTime,
                               MicroSecondDate endTime) {
        String channelIdString = getIdString(channelId);
        channelIdStringToChannelId.put(channelIdString, channelId);
        MicroSecondTimeRange timeRange = new MicroSecondTimeRange(beginTime,
                                                                  endTime);
        if(!channelIdWithTime.containsKey(channelId)) {
            channelIdWithTime.put(channelId, new ArrayList());
        }
        List list = ((List)channelIdWithTime.get(channelId));
        list.add(timeRange);
        if(list.size() > 1) {
            mergeTimes(list);
        }
    }

    private void mergeTimes(List channelTimes) {
        // Pop off the end then merge from second to end to end to keep the
        // entries in time order
        MicroSecondTimeRange latest = pop(channelTimes);
        MicroSecondTimeRange[] timeRangeArray = ReduceTool.merge(new MicroSecondTimeRange[] {pop(channelTimes),
                                                                                             latest});
        for(int i = 0; i < timeRangeArray.length; i++) {
            channelTimes.add(timeRangeArray[i]);
        }
    }

    private static MicroSecondTimeRange pop(List channelTimes) {
        return (MicroSecondTimeRange)channelTimes.remove(channelTimes.size() - 1);
    }

    public void addMSeedSeismogram() {
        this.numMSeedFiles++;
    }

    public void addSacSeismogram() {
        this.numSacFiles++;
    }

    public void outputReport(String reportName) {
        String name;
        if(reportName == null) {
            name = getCurrentDate() + "_RT130Report";
        } else {
            name = reportName;
        }
        printReport(name);
        makeReportImage(name);
    }

    private void makeReportImage(String reportName) {
        // ReportFactory is used to organize the data with station codes at the
        // top of the hierarchy, instead of channels being at the top.
        RT130ReportFactory factory = new RT130ReportFactory(channelIdWithTime,
                                                            channelIdStringToChannelId);
        int numStations = 0;
        TaskSeries taskSeries = new TaskSeries("Stations");
        List stationDataSummaryList = factory.getSortedStationDataSummaryList();
        Iterator it = stationDataSummaryList.iterator();
        while(it.hasNext()) {
            StationDataSummary stationDataSummary = (StationDataSummary)it.next();
            MicroSecondTimeRange firstTimeRange = stationDataSummary.getEncompassingTimeRange();
            Task task = new Task(stationDataSummary.getStationCode(),
                                 firstTimeRange.getBeginTime(),
                                 firstTimeRange.getEndTime());
            MicroSecondTimeRange[] recordedTimes = stationDataSummary.getRecordedTimes();
            for(int i = 0; i < recordedTimes.length; i++) {
                task.addSubtask(new Task(stationDataSummary.getStationCode(),
                                         recordedTimes[i].getBeginTime(),
                                         recordedTimes[i].getEndTime()));
            }
            numStations++;
            taskSeries.add(task);
        }
        TaskSeriesCollection dataset = new TaskSeriesCollection();
        dataset.add(taskSeries);
        DateAxis dateAxis = new DateAxis("Time");
        CategoryItemRenderer renderer = new GanttRenderer();
        CategoryPlot plot = new CategoryPlot(dataset,
                                             new CategoryAxis("Station"),
                                             dateAxis,
                                             renderer);
        plot.setOrientation(PlotOrientation.HORIZONTAL);
        JFreeChart chart = new JFreeChart(reportName,
                                          JFreeChart.DEFAULT_TITLE_FONT,
                                          plot,
                                          false);
        renderer.setSeriesPaint(0, Color.PINK);
        renderer.setOutlinePaint(Color.BLACK);
        if(numStations > 0) {// JFreeChart doesn't like setting the time on
            // an empty chart
            if(start == null) {
                start = dateAxis.getMinimumDate();
            }
            if(end == null) {
                end = dateAxis.getMaximumDate();
            }
            dateAxis.setRange(start, end);
        }
        printChartToPDF(chart, 1600, (numStations * 25) + 100, reportName
                + ".pdf");
    }

    private void printChartToPDF(JFreeChart chart,
                                 int width,
                                 int height,
                                 String fileName) {
        try {
            Document document = new Document(new com.lowagie.text.Rectangle(width,
                                                                            height));
            PdfWriter writer = PdfWriter.getInstance(document,
                                                     new FileOutputStream(fileName));
            document.addAuthor("University of South Carolina, United States of America, Geological Sciences");
            document.open();
            PdfContentByte cb = writer.getDirectContent();
            PdfTemplate tp = cb.createTemplate(width, height);
            Graphics2D g2 = tp.createGraphics(width,
                                              height,
                                              new DefaultFontMapper());
            Rectangle2D rectangle2D = new Rectangle2D.Double(0,
                                                             0,
                                                             width,
                                                             height);
            chart.draw(g2, rectangle2D);
            g2.dispose();
            cb.addTemplate(tp, 0, 0);
            document.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void printReport(String reportName) {
        FileWriter report = null;
        try {
            report = new FileWriter(reportName + ".log");
        } catch(IOException e) {
            e.printStackTrace();
        }
        PrintWriter reportStream = new PrintWriter(report);
        reportStream.println("Report");
        reportStream.println("-------");
        reportStream.println("  Number of RT130 stations read: "
                + getNumStations());
        reportStream.println("  Number of channels read: " + getNumChannels());
        reportStream.println("  Number of channels read with incontiguous data: "
                + getNumIncontiguousChannels());
        reportStream.println();
        reportStream.println("SAC Files");
        reportStream.println("----------");
        reportStream.println("  Number of files read: " + getNumSacFiles());
        reportStream.println();
        reportStream.println("MSEED Files");
        reportStream.println("------------");
        reportStream.println("  Number of files read: " + getNumMSeedFiles());
        reportStream.println();
        reportStream.println("RT130 Files");
        reportStream.println("------------");
        reportStream.println("  Number of seismogram entries: "
                + getNumRefTekEntries());
        reportStream.println();
        reportStream.println("  Days Of Coverage");
        reportStream.println("  -----------------");
        printRefTekDaysOfCoverage(reportStream);
        reportStream.println();
        reportStream.println("  Gap Description");
        reportStream.println("  ----------------");
        printRefTekGapDescription(reportStream);
        reportStream.println();
        reportStream.println("Unsupported File Exceptions");
        reportStream.println("----------------------------");
        printExceptionFiles(reportStream,
                            unsupportedFileException,
                            "No Unsupported File Exceptions.");
        reportStream.println();
        reportStream.println("File Format Exceptions");
        reportStream.println("-----------------------");
        printExceptionFiles(reportStream,
                            fileFormatException,
                            "No File Format Exceptions.");
        reportStream.println();
        reportStream.println("Malformed File Name Exceptions");
        reportStream.println("-------------------------------");
        printExceptionFiles(reportStream,
                            malformedFileNameException,
                            "No Malformed File Name Exceptions.");
        reportStream.close();
        try {
            report.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public void addFileFormatException(String fileLoc, String problemDescription) {
        fileFormatException.put(fileLoc, problemDescription);
    }

    public void addMalformedFileNameException(String fileLoc,
                                              String problemDescription) {
        malformedFileNameException.put(fileLoc, problemDescription);
    }

    public void addUnsupportedFileException(String fileLoc,
                                            String problemDescription) {
        unsupportedFileException.put(fileLoc, problemDescription);
    }

    public int getNumStations() {
        int numStations = 0;
        Set stations = new HashSet();
        Iterator it = channelIdWithTime.keySet().iterator();
        while(it.hasNext()) {
            String key = (String)it.next();
            stations.add(StationIdUtil.toString(((Channel)channelIdStringToChannel.get(key)).my_site.my_station.get_id()));
        }
        numStations = stations.size();
        return numStations;
    }

    public int getNumChannels() {
        return channelIdWithTime.keySet().size();
    }

    public int getNumIncontiguousChannels() {
        int numIncontiguousChannels = 0;
        Set incontiguousChannels = new HashSet();
        Iterator it = channelIdWithTime.keySet().iterator();
        while(it.hasNext()) {
            String key = (String)it.next();
            if(((List)channelIdWithTime.get(key)).size() > 1) {
                incontiguousChannels.add(key);
            }
        }
        numIncontiguousChannels = incontiguousChannels.size();
        return numIncontiguousChannels;
    }

    public int getNumSacFiles() {
        return numSacFiles;
    }

    public int getNumMSeedFiles() {
        return numMSeedFiles;
    }

    public int getNumRefTekEntries() {
        return numRefTekEntries;
    }

    private void printExceptionFiles(PrintWriter reportStream,
                                     Map exceptions,
                                     String emptyMessage) {
        Iterator it = exceptions.keySet().iterator();
        if(!it.hasNext()) {
            reportStream.println("  " + emptyMessage);
        }
        while(it.hasNext()) {
            String key = (String)it.next();
            reportStream.println("  " + key);
            reportStream.println("  " + exceptions.get(key));
            reportStream.println();
        }
    }

    private void printRefTekGapDescription(PrintWriter reportStream) {
        // ReportFactory is used to organize the data with station codes at the
        // top of the hierarchy, instead of channels being at the top.
        if(reportFactory == null) {
            reportFactory = new RT130ReportFactory(channelIdWithTime,
                                                   channelIdStringToChannelId);
        }
        reportFactory.printGapDescription(reportStream);
    }

    private void printRefTekDaysOfCoverage(PrintWriter reportStream) {
        // ReportFactory is used to organize the data with station codes at the
        // top of the hierarchy, instead of channels being at the top.
        if(reportFactory == null) {
            reportFactory = new RT130ReportFactory(channelIdWithTime,
                                                   channelIdStringToChannelId);
        }
        reportFactory.printDaysOfCoverage(reportStream);
    }

    private String getIdString(Channel chan) {
        if(!channelToIdString.containsKey(chan)) {
            channelToIdString.put(chan, ChannelIdUtil.toString(chan.get_id()));
        }
        return (String)channelToIdString.get(chan);
    }

    private String getIdString(ChannelId channelId) {
        if(!channelIdToIdString.containsKey(channelId)) {
            channelIdToIdString.put(channelId,
                                    ChannelIdUtil.toString(channelId));
        }
        return (String)channelIdToIdString.get(channelId);
    }

    private String getCurrentDate() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        java.util.Date date = new java.util.Date();
        return dateFormat.format(date);
    }

    private Date start, end;

    private Set stationCodesToDisplay;

    private RT130ReportFactory reportFactory;

    private int numSacFiles, numMSeedFiles, numRefTekEntries;

    private Map fileFormatException = new HashMap();

    private Map malformedFileNameException = new HashMap();

    private Map unsupportedFileException = new HashMap();

    private Map channelIdWithTime = new HashMap();

    private Map channelIdStringToChannel = new HashMap();

    private Map channelIdStringToChannelId = new HashMap();

    private Map channelToIdString = new HashMap();

    private Map channelIdToIdString = new HashMap();
}
