package edu.sc.seis.fissuresUtil.psn;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * PSNEventRecord.java
 *
 * @author Created by Philip Oliver-Paull
 */
public class PSNEventRecord {
    private DataInputStream dis;
    private PSNHeader fixedHeader;
    private PSNVariableHeader varHeader;

    public PSNEventRecord(DataInputStream data) throws IOException{
        this(new PSNHeader(data), data);
    }

    public PSNEventRecord(PSNHeader header, DataInputStream data) throws IOException{
        dis = data;
        fixedHeader = header;
        varHeader = new PSNVariableHeader(dis, (int)header.getVarHeadLength());
    }

    public PSNHeader getFixedHeader(){
        return fixedHeader;
    }

    public PSNVariableHeader getVariableHeader(){
        return varHeader;
    }




}

