package edu.sc.seis.fissuresUtil.psn;

import edu.sc.seis.fissuresUtil.psn.PSNDateTime;
import edu.sc.seis.fissuresUtil.psn.PSNEventInfo;
import edu.sc.seis.fissuresUtil.psn.PSNEventRecord;
import edu.sc.seis.fissuresUtil.psn.PSNHeader;
import edu.sc.seis.fissuresUtil.psn.PSNVariableHeader;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * PSNDataFile.java
 * See http://www.seismicnet.com/psnformat4.html
 *
 * @author Created by Philip Oliver-Paull
 */
public class PSNDataFile {

    private PSNEventRecord[] eventRecs;

    public PSNDataFile(String filename) throws FileNotFoundException, IOException{
        DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(filename)));
        readFile(dis);
        dis.close();
    }

    public void readFile(DataInputStream dis) throws IOException, FileNotFoundException{
        PSNHeader header = new PSNHeader(dis);
        if (!header.isVolumeFile()){
            eventRecs = new PSNEventRecord[]{new PSNEventRecord(header, dis)};
        }
        else{
            eventRecs = new PSNEventRecord[header.getNumRecords()];
            for (int i = 0; i < header.getNumRecords(); i++) {
                eventRecs[i] = new PSNEventRecord(dis);
            }
        }
    }

    public PSNEventRecord[] getEventRecords(){
        return eventRecs;
    }

    public static void main(String[] args){
        try{
            PSNDataFile psnData = new PSNDataFile(args[0]);
            PSNEventRecord[] records = psnData.getEventRecords();

            for (int i = 0; i < records.length; i++) {
                System.out.println("****** Event Record " + i + " ******");
                System.out.println(records[i].toString());
            }
        }
        catch(Throwable ee){
            ee.printStackTrace();
        }
    }

}

