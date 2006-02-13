package edu.sc.seis.fissuresUtil.rt130;

import java.io.BufferedInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.EOFException;
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

    public void readFile(DataInput in) throws IOException {
        timeAndDataHashMap = new HashMap();
        boolean done = false;
        String data = "";
        String line = "";
        while(!done) {
            try {
                line = in.readLine();
                if(line == null) {
                    done = true;
                } else {
                    if(line.startsWith("#")) {
                        // Do nothing
                    } else {
                        data = data.concat(line);
                        data = data + "\n";
                    }
                }
            } catch(EOFException e) {
                done = true;
            }
        }
        StringTokenizer st = new StringTokenizer(data);
        String token = "";
        while(st.hasMoreTokens()) {
            token = st.nextToken();
            if(token.equals("START")) {
                MicroSecondDate time = stringToMicroSecondDate(st.nextToken());
                if(network_begin_time == null) {
                    network_begin_time = time;
                }
                Map dataHashMap = new HashMap();
                while(!st.nextToken().startsWith("chan/dip/azi")) {
                    // Skip to relevant data
                }
                String temp = st.nextToken();
                while(!temp.startsWith("END")) {
                    String unitName = temp;
                    String unitId = st.nextToken().substring(0, 4);
                    dataHashMap.put(unitId, unitName);
                    st.nextToken();
                    st.nextToken();
                    temp = st.nextToken();
                }
                timeAndDataHashMap.put(time, dataHashMap);
            }
        }
        Set keySet = timeAndDataHashMap.keySet();
        MicroSecondDate[] keyArray = (MicroSecondDate[])keySet.toArray(new MicroSecondDate[0]);
        keyList = new ArrayList(keyArray.length);
        for(int i = 0; i < keyArray.length; i++) {
            keyList.add(keyArray[i]);
        }
        Collections.sort(keyList, new SortTool.AscendingTimeSorter());
    }

    private MicroSecondDate stringToMicroSecondDate(String timeString) {
        StringTokenizer st = new StringTokenizer(timeString, ":");
        int yearInt = Integer.parseInt(st.nextToken());
        int daysOfYearInt = Integer.parseInt(st.nextToken());
        int hoursInt = Integer.parseInt(st.nextToken());
        ISOTime isoTime = new ISOTime(yearInt, daysOfYearInt, hoursInt, 0, 0);
        return isoTime.getDate();
    }

    public String getUnitName(MicroSecondDate time, String unitId) {
        MicroSecondDate[] keyArray = (MicroSecondDate[])keyList.toArray(new MicroSecondDate[0]);
        for(int i = 0; i < keyArray.length; i++) {
            if(time.before(keyArray[i])) {
                Map dataHashMap = (Map)timeAndDataHashMap.get(keyArray[i - 1]);
                String unitName = (String)dataHashMap.get(unitId);
                return unitName;
            }
        }
        System.err.println("/-------------------------");
        System.err.println("| Unit name for DAS unit number " + unitId
                + " was not found in the NC file.");
        System.err.println("| The name \"" + unitId
                + "\" will be used instead.");
        System.err.println("| To correct this entry in the database, please run UnitNameUpdater.");
        System.err.println("| ");
        System.err.println("| The time requested was: " + time.toString());
        System.err.println("\\-------------------------");
        return unitId;
    }

    public String getCanonicalPath() throws IOException {
        return file.getCanonicalPath();
    }

    public MicroSecondDate network_begin_time;

    private List keyList;

    private Map timeAndDataHashMap;

    private File file;
}
