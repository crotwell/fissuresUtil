package edu.sc.seis.fissuresUtil.database.seismogram;

import java.awt.Color;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.data.gantt.Task;
import org.jfree.data.gantt.TaskSeries;
import org.jfree.data.gantt.TaskSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.network.StationIdUtil;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;
import edu.sc.seis.fissuresUtil.time.ReduceTool;

public class DatabasePopulationReport extends ApplicationFrame {

    public DatabasePopulationReport() {
        super("Database Population Report");
        this.numSacFiles = 0;
        this.numMSeedFiles = 0;
        this.problemFiles = new HashMap();
        this.channelIdWithTime = new HashMap();
        this.channelIdToChannel = new HashMap();
    }

    public void addRefTekSeismogram(Channel channel,
                                    MicroSecondDate beginTime,
                                    MicroSecondDate endTime) {
        String channelId = ChannelIdUtil.toString(channel.get_id());
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
            list.add(new MicroSecondTimeRange(beginTime, endTime));
            channelIdWithTime.put(channelId, list);
        }
    }

    public void addMSeedSeismogram() {
        this.numMSeedFiles++;
    }

    public void addSacSeismogram() {
        this.numSacFiles++;
    }

    public void makeReportImage() {
        // ReportFactory is used to organize the data with station codes at the
        // top of the hierarchy, instead of channels being at the top.
        ReportFactory reportFactory = new ReportFactory(channelIdWithTime, channelIdToChannel);
        TaskSeries taskSeries = new TaskSeries("Stations");
        List stationDataSummaryList = reportFactory.getSortedStationDataSummaryList();
        Iterator it = stationDataSummaryList.iterator();
        Task task = null;
        for(int i = 0; it.hasNext(); i++) {
            StationDataSummary stationDataSummary = (StationDataSummary)it.next();
            Map channelsToTimeRanges = stationDataSummary.getChannelsWithTimeRanges();
            Set keySet = channelsToTimeRanges.keySet();
            Iterator ij = keySet.iterator();
            List timeRangeList = null;
            // Try to use high bandwidth channel.
            while(ij.hasNext()){
                String channelCode = (String)ij.next();
                if(channelCode.startsWith("B")){
                    timeRangeList = (List)channelsToTimeRanges.get(channelCode);
                    break;
                }
            }
            if(timeRangeList == null){
                Iterator il = keySet.iterator();
                timeRangeList = (List)channelsToTimeRanges.get((String)il.next());
            }
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
        JFreeChart chart = ChartFactory.createGanttChart("Database Population Report",
                                                         "Station",
                                                         "Time",
                                                         dataset,
                                                         false,
                                                         false,
                                                         false);
        final CategoryPlot plot = (CategoryPlot)chart.getPlot();
        final CategoryItemRenderer renderer = plot.getRenderer();
        renderer.setSeriesPaint(0, Color.PINK);
        final ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(800, 600));
        setContentPane(chartPanel);
        pack();
        RefineryUtilities.centerFrameOnScreen(this);
        setVisible(true);
    }

    public void printReport() {
        System.out.println("Report");
        System.out.println("-------");
        printRefTekImportSummary();
        System.out.println("Number of stations read: " + getNumStations());
        System.out.println("Number of channels read: " + getNumChannels());
        System.out.println("Number of channels read with incontiguous data: "
                + getNumIncontiguousChannels());
        System.out.println();
        System.out.println("SAC Files");
        System.out.println("----------");
        System.out.println("Number of files read: " + getNumSacFiles());
        System.out.println();
        System.out.println("MSEED Files");
        System.out.println("------------");
        System.out.println("Number of files read: " + getNumMSeedFiles());
        System.out.println();
        System.out.println("Problem Files");
        System.out.println("--------------");
        printProblemFiles();
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

    private void printProblemFiles() {
        Iterator it = problemFiles.keySet().iterator();
        if(!it.hasNext()) {
            System.out.println("No problem files.");
        }
        while(it.hasNext()) {
            String key = (String)it.next();
            System.out.println(key);
            System.out.println(problemFiles.get(key));
            System.out.println();
        }
    }

    private void printRefTekImportSummary() {
        // ReportFactory is used to organize the data with station codes at the
        // top of the hierarchy, instead of channels being at the top.
        ReportFactory reportFactory = new ReportFactory(channelIdWithTime, channelIdToChannel);
        reportFactory.print();
    }

    private int numSacFiles, numMSeedFiles;

    private Map problemFiles;

    private Map channelIdWithTime;
    
    private Map channelIdToChannel;
}
