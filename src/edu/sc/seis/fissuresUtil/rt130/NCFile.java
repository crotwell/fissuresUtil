package edu.sc.seis.fissuresUtil.rt130;

import java.io.BufferedInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;
import edu.iris.Fissures.model.ISOTime;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.sc.seis.fissuresUtil.time.SortTool;

public class NCFile {

    public NCFile(String ncFileLocation) throws IOException {
        file = new File(ncFileLocation);
        FileInputStream fis = new FileInputStream(file);
        BufferedInputStream bis = new BufferedInputStream(fis);
        DataInputStream dis = new DataInputStream(bis);
        this.readFile(dis);
    }

    private void readFile(DataInput in) throws IOException {
        timeAndDataMapIdToName = new HashMap();
        timeAndDataMapNameToId = new HashMap();
        timeAndDataMapIdToChanDipAzi = new HashMap();
        StringBuffer data = new StringBuffer();
        String line;
        while((line = in.readLine()) != null) {
            
            data.append(line);
            data.append('\n');
        }
        StringTokenizer st = new StringTokenizer(data.toString());
        while(st.hasMoreTokens()) {
            String token = st.nextToken();
            if(token.equals("START")) {
                MicroSecondDate time = stringToMicroSecondDate(st.nextToken());
                if(network_begin_time == null) {
                    network_begin_time = time;
                }
                Map idToName = new HashMap();
                Map nameToId = new HashMap();
                Map idToChanDipAzi = new HashMap();
                while(!st.nextToken().startsWith("chan/dip/azi")) {
                    // Skip to relevant data
                }
                String temp = st.nextToken();
                while(!temp.startsWith("END")) {
                    String unitName = temp;
                    String unitId = st.nextToken().substring(0, 4);
                    st.nextToken();
                    String chanDipAzi = st.nextToken();
                    idToName.put(unitId, unitName);
                    nameToId.put(unitName, unitId);
                    idToChanDipAzi.put(unitId, chanDipAzi);
                    temp = st.nextToken();
                    if(temp.startsWith("LOC")){
                        st.nextToken();
                        st.nextToken();
                        st.nextToken();
                        temp = st.nextToken();
                    }
                }
                timeAndDataMapIdToName.put(time, idToName);
                timeAndDataMapNameToId.put(time, nameToId);
                timeAndDataMapIdToChanDipAzi.put(time, idToChanDipAzi);
            }
        }
        Set keySet1 = timeAndDataMapIdToName.keySet();
        MicroSecondDate[] keyArray1 = (MicroSecondDate[])keySet1.toArray(new MicroSecondDate[0]);
        keyListForIdToName = new ArrayList(keyArray1.length);
        for(int i = 0; i < keyArray1.length; i++) {
            keyListForIdToName.add(keyArray1[i]);
        }
        Collections.sort(keyListForIdToName, new SortTool.AscendingTimeSorter());
        Set keySet2 = timeAndDataMapNameToId.keySet();
        MicroSecondDate[] keyArray2 = (MicroSecondDate[])keySet2.toArray(new MicroSecondDate[0]);
        keyListForNameToId = new ArrayList(keyArray2.length);
        for(int i = 0; i < keyArray2.length; i++) {
            keyListForNameToId.add(keyArray2[i]);
        }
        Collections.sort(keyListForNameToId, new SortTool.AscendingTimeSorter());
        Set keySet3 = timeAndDataMapIdToChanDipAzi.keySet();
        MicroSecondDate[] keyArray3 = (MicroSecondDate[])keySet3.toArray(new MicroSecondDate[0]);
        keyListForIdToChanDipAzi = new ArrayList(keyArray3.length);
        for(int i = 0; i < keyArray3.length; i++) {
            keyListForIdToChanDipAzi.add(keyArray3[i]);
        }
        Collections.sort(keyListForIdToChanDipAzi,
                         new SortTool.AscendingTimeSorter());
    }

    private MicroSecondDate stringToMicroSecondDate(String timeString) {
        StringTokenizer st = new StringTokenizer(timeString, ":");
        int yearInt = Integer.parseInt(st.nextToken());
        int daysOfYearInt = Integer.parseInt(st.nextToken());
        int hoursInt = Integer.parseInt(st.nextToken());
        ISOTime isoTime = new ISOTime(yearInt, daysOfYearInt, hoursInt, 0, 0);
        return isoTime.getDate();
    }

    public String getChannelNamesAndOrientations(MicroSecondDate time,
                                                 String unitId) {
        MicroSecondDate[] keyArray = (MicroSecondDate[])keyListForIdToChanDipAzi.toArray(new MicroSecondDate[0]);
        for(int i = 0; i < keyArray.length; i++) {
            if(time.after(keyArray[i]) || time.equals(keyArray[i])) {
                Map idToChanDipAzi = (Map)timeAndDataMapIdToChanDipAzi.get(keyArray[i]);
                if(idToChanDipAzi.containsKey(unitId)) {
                    return (String)idToChanDipAzi.get(unitId);
                }
            }
        }
        logger.warn("Channel names and orientations for DAS unit number "
                + unitId + " was not found in the NC file.");
        logger.warn("The defaults will be used instead.");
        logger.warn("The time requested was: " + time);
        return "default";
    }

    public String getUnitName(MicroSecondDate time, String unitId) {
        MicroSecondDate[] keyArray = (MicroSecondDate[])keyListForIdToName.toArray(new MicroSecondDate[0]);
        for(int i = 0; i < keyArray.length; i++) {
            if(time.after(keyArray[i]) || time.equals(keyArray[i])) {
                Map idToName = (Map)timeAndDataMapIdToName.get(keyArray[i]);
                if(idToName.containsKey(unitId)) {
                    return (String)idToName.get(unitId);
                }
            }
        }
        logger.warn("Unit name for DAS unit number " + unitId
                + " was not found in the NC file.");
        logger.warn("The name \"" + unitId + "\" will be used instead.");
        logger.warn("The time requested was: " + time.toString());
        return unitId;
    }

    public String getUnitId(MicroSecondDate time, String unitName) {
        MicroSecondDate[] keyArray = (MicroSecondDate[])keyListForNameToId.toArray(new MicroSecondDate[0]);
        for(int i = 0; i < keyArray.length; i++) {
            if(time.after(keyArray[i]) || time.equals(keyArray[i])) {
                Map nameToId = (Map)timeAndDataMapNameToId.get(keyArray[i]);
                if(nameToId.containsKey(unitName)) {
                    return (String)nameToId.get(unitName);
                }
            }
        }
        logger.warn("Unit number for DAS unit name " + unitName
                + " was not found in the NC file.");
        logger.warn("The number \"" + unitName + "\" will be used instead.");
        logger.warn("The time requested was: " + time);
        return unitName;
    }

    public MicroSecondDate getStationBeginTime(String unitId) {
        MicroSecondDate[] keyArray = (MicroSecondDate[])keyListForIdToName.toArray(new MicroSecondDate[0]);
        for(int i = 0; i < keyArray.length; i++) {
            Map idToName = (Map)timeAndDataMapIdToName.get(keyArray[i]);
            if(idToName.containsKey(unitId)) {
                return keyArray[i];
            }
        }
        keyArray = (MicroSecondDate[])keyListForNameToId.toArray(new MicroSecondDate[0]);
        for(int i = 0; i < keyArray.length; i++) {
            Map nameToId = (Map)timeAndDataMapNameToId.get(keyArray[i]);
            if(nameToId.containsKey(unitId)) {
                return keyArray[i];
            }
        }
        logger.warn("Unit number or DAS unit name " + unitId
                + " was not found in the NC file.");
        logger.warn("The station begin time returned will be the network begin time.");
        return network_begin_time;
    }

    public String getCanonicalPath() throws IOException {
        return file.getCanonicalPath();
    }

    public static final String NC_FILE_LOC = "NCFile";

    public MicroSecondDate network_begin_time;

    private List keyListForIdToName, keyListForNameToId,
            keyListForIdToChanDipAzi;

    private Map timeAndDataMapIdToName, timeAndDataMapNameToId,
            timeAndDataMapIdToChanDipAzi;

    private File file;

    private static final Logger logger = Logger.getLogger(NCFile.class);

    public static void main(String[] args) throws IOException {
        NCFile nc = new NCFile(args[0]);
        System.out.println(nc.getCanonicalPath());
        System.out.println(nc.getStationBeginTime("SNP85"));
    }
}
