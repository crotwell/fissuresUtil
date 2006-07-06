package edu.sc.seis.fissuresUtil.database.seismogram;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.data.gantt.Task;
import org.jfree.data.gantt.TaskSeries;
import org.jfree.data.gantt.TaskSeriesCollection;
import com.lowagie.text.Document;
import com.lowagie.text.pdf.DefaultFontMapper;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.network.StationIdUtil;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;
import edu.sc.seis.fissuresUtil.time.ReduceTool;

public class RT130Report {

    public void addRefTekSeismogram(Channel channel,
                                    MicroSecondDate beginTime,
                                    MicroSecondDate endTime) {
        String channelId = getIdString(channel);
        channelIdToChannel.put(channelId, channel);
        MicroSecondTimeRange timeRange = new MicroSecondTimeRange(beginTime,
                                                                  endTime);
        if(channelIdWithTime.containsKey(channelId)) {
            List list = ((List)channelIdWithTime.get(channelId));
            list.add(timeRange);
            MicroSecondTimeRange[] timeRangeArray = ReduceTool.merge((MicroSecondTimeRange[])list.toArray(new MicroSecondTimeRange[0]));
            List newList = new LinkedList();
            for(int i = 0; i < timeRangeArray.length; i++) {
                newList.add(timeRangeArray[i]);
            }
            channelIdWithTime.put(channelId, newList);
        } else {
            List list = new LinkedList();
            list.add(timeRange);
            channelIdWithTime.put(channelId, list);
        }
    }
    
    private void mergeTimes(){
        Iterator it = channelIdWithTime.keySet().iterator();
        while(it.hasNext()) {
            String key = (String)it.next();
            List list = ((List)channelIdWithTime.get(key));
            MicroSecondTimeRange[] timeRangeArray = ReduceTool.merge((MicroSecondTimeRange[])list.toArray(new MicroSecondTimeRange[0]));
            List newList = new LinkedList();
            for(int i = 0; i < timeRangeArray.length; i++) {
                newList.add(timeRangeArray[i]);
            }
            channelIdWithTime.put(key, newList);
        }
    }

    public void addMSeedSeismogram() {
        this.numMSeedFiles++;
    }

    public void addSacSeismogram() {
        this.numSacFiles++;
    }

    public void makeReportImage() {
        mergeTimes();
        // ReportFactory is used to organize the data with station codes at the
        // top of the hierarchy, instead of channels being at the top.
        RT130ReportFactory reportFactory = new RT130ReportFactory(channelIdWithTime,
                                                                  channelIdToChannel);
        TaskSeries taskSeries = new TaskSeries("Stations");
        List stationDataSummaryList = reportFactory.getSortedStationDataSummaryList();
        Iterator it = stationDataSummaryList.iterator();
        Task task = null;
        for(int i = 0; it.hasNext(); i++) {
            StationDataSummary stationDataSummary = (StationDataSummary)it.next();
            Map channelsToTimeRanges = stationDataSummary.getChannelsWithTimeRanges();
            String channelCodeWithMostGaps = stationDataSummary.getChannelCodeWithMostGaps();
            List timeRangeList = (List)channelsToTimeRanges.get(channelCodeWithMostGaps);
            Iterator ik = timeRangeList.iterator();
            MicroSecondTimeRange firstTimeRange = stationDataSummary.getEncompassingTimeRange();
            task = new Task(stationDataSummary.getStationCode(),
                            firstTimeRange.getBeginTime(),
                            firstTimeRange.getEndTime());
            while(ik.hasNext()) {
                MicroSecondTimeRange allOtherTimeRanges = (MicroSecondTimeRange)ik.next();
                task.addSubtask(new Task(stationDataSummary.getStationCode(),
                                         allOtherTimeRanges.getBeginTime(),
                                         allOtherTimeRanges.getEndTime()));
            }
            taskSeries.add(task);
        }
        TaskSeriesCollection dataset = new TaskSeriesCollection();
        dataset.add(taskSeries);
        JFreeChart chart = ChartFactory.createGanttChart("RT130 Report",
                                                         "Station",
                                                         "Time",
                                                         dataset,
                                                         false,
                                                         false,
                                                         false);
        final CategoryPlot plot = (CategoryPlot)chart.getPlot();
        final CategoryItemRenderer renderer = plot.getRenderer();
        renderer.setSeriesPaint(0, Color.PINK);
        renderer.setOutlinePaint(Color.BLACK);
        printChartToPDF(chart, 1024, 768, "RT130Report.pdf");
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

    public void printReport() {
        mergeTimes();
        FileWriter report = null;
        try {
            report = new FileWriter("RT130Report.txt");
        } catch(IOException e) {
            e.printStackTrace();
        }
        PrintWriter reportStream = new PrintWriter(report);
        reportStream.println("Report");
        reportStream.println("-------");
        reportStream.println("  Number of stations read: " + getNumStations());
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
        reportStream.println("  Days Of Coverage");
        reportStream.println("  -----------------");
        printRefTekDaysOfCoverage(reportStream);
        reportStream.println("  Gap Description");
        reportStream.println("  ----------------");
        printRefTekGapDescription(reportStream);
        reportStream.println();
        reportStream.println("Problem Files");
        reportStream.println("--------------");
        printProblemFiles(reportStream);
        reportStream.close();
        try {
            report.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public void addProblemFile(String fileLoc, String problemDescription) {
        problemFiles.put(fileLoc, problemDescription);
    }

    public int getNumStations() {
        int numStations = 0;
        Set stations = new HashSet();
        Iterator it = channelIdWithTime.keySet().iterator();
        while(it.hasNext()) {
            String key = (String)it.next();
            stations.add(StationIdUtil.toString(((Channel)channelIdToChannel.get(key)).my_site.my_station.get_id()));
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

    private void printProblemFiles(PrintWriter reportStream) {
        Iterator it = problemFiles.keySet().iterator();
        if(!it.hasNext()) {
            reportStream.println("No problem files.");
        }
        while(it.hasNext()) {
            String key = (String)it.next();
            reportStream.println("  " + key);
            reportStream.println("  " + problemFiles.get(key));
            reportStream.println();
        }
    }

    private void printRefTekGapDescription(PrintWriter reportStream) {
        // ReportFactory is used to organize the data with station codes at the
        // top of the hierarchy, instead of channels being at the top.
        if(reportFactory == null) {
            reportFactory = new RT130ReportFactory(channelIdWithTime,
                                                   channelIdToChannel);
        }
        reportFactory.printGapDescription(reportStream);
    }

    private void printRefTekDaysOfCoverage(PrintWriter reportStream) {
        // ReportFactory is used to organize the data with station codes at the
        // top of the hierarchy, instead of channels being at the top.
        if(reportFactory == null) {
            reportFactory = new RT130ReportFactory(channelIdWithTime,
                                                   channelIdToChannel);
        }
        reportFactory.printDaysOfCoverage(reportStream);
    }

    private String getIdString(Channel chan) {
        if(!channelToIDString.containsKey(chan)) {
            channelToIDString.put(chan, ChannelIdUtil.toString(chan.get_id()));
        }
        return (String)channelToIDString.get(chan);
    }

    private RT130ReportFactory reportFactory;

    private int numSacFiles, numMSeedFiles;

    private Map problemFiles = new HashMap();

    private Map channelIdWithTime = new HashMap();

    private Map channelIdToChannel = new HashMap();

    private Map channelToIDString = new HashMap();
}
