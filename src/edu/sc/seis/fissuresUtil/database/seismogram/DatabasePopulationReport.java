package edu.sc.seis.fissuresUtil.database.seismogram;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.network.StationIdUtil;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;
import edu.sc.seis.fissuresUtil.time.ReduceTool;

public class DatabasePopulationReport {

    public DatabasePopulationReport() {
        this.numSacFiles = 0;
        this.numMSeedFiles = 0;
        this.problemFiles = new HashMap();
        this.channelWithTime = new HashMap();
    }

    public void addRefTekSeismogram(Channel channel,
                                    MicroSecondDate beginTime,
                                    MicroSecondDate endTime) {
        MicroSecondTimeRange timeRange = new MicroSecondTimeRange(beginTime,
                                                                  endTime);
        if(channelWithTime.containsKey(channel)) {
            List list = ((List)channelWithTime.get(channel));
            list.add(timeRange);
            MicroSecondTimeRange[] timeRangeArray = ReduceTool.merge((MicroSecondTimeRange[])list.toArray(new MicroSecondTimeRange[0]));
            List newList = new LinkedList();
            for(int i = 0; i < timeRangeArray.length; i++) {
                newList.add(timeRangeArray[i]);
            }
            channelWithTime.put(channel, newList);
        } else {
            List list = new LinkedList();
            list.add(new MicroSecondTimeRange(beginTime, endTime));
            channelWithTime.put(channel, list);
        }
    }

    public void addMSeedSeismogram() {
        this.numMSeedFiles++;
    }

    public void addSacSeismogram() {
        this.numSacFiles++;
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
        Iterator it = channelWithTime.keySet().iterator();
        while(it.hasNext()) {
            Channel key = (Channel)it.next();
            stations.add(StationIdUtil.toString(key.my_site.my_station.get_id()));
        }
        numStations = stations.size();
        return numStations;
    }

    public int getNumChannels() {
        return channelWithTime.keySet().size();
    }

    public int getNumIncontiguousChannels() {
        int numIncontiguousChannels = 0;
        Set incontiguousChannels = new HashSet();
        Iterator it = channelWithTime.keySet().iterator();
        while(it.hasNext()) {
            Channel key = (Channel)it.next();
            if(((List)channelWithTime.get(key)).size() <= 1) {
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
        ReportFactory reportFactory = new ReportFactory(channelWithTime);
        reportFactory.print();
    }

    private int numSacFiles, numMSeedFiles;

    private Map problemFiles;

    private Map channelWithTime;
}
