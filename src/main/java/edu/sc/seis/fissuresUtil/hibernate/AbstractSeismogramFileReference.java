package edu.sc.seis.fissuresUtil.hibernate;

import java.io.File;
import java.net.MalformedURLException;
import java.sql.Timestamp;

import edu.sc.seis.fissuresUtil.xml.DataSet;
import edu.sc.seis.fissuresUtil.xml.SeismogramFileTypes;
import edu.sc.seis.fissuresUtil.xml.URLDataSetSeismogram;
import edu.sc.seis.fissuresUtil.xml.UnsupportedFileTypeException;

public abstract class AbstractSeismogramFileReference {

    protected int dbid;

    public AbstractSeismogramFileReference(String netCode,
                                           String staCode,
                                           String siteCode,
                                           String chanCode,
                                           Timestamp beginTime,
                                           Timestamp endTime,
                                           String filePath,
                                           int fileType) {
        super();
        this.netCode = netCode;
        this.staCode = staCode;
        this.siteCode = siteCode;
        this.chanCode = chanCode;
        this.beginTime = beginTime;
        this.endTime = endTime;
        this.filePath = filePath;
        this.fileType = fileType;
    }

    public URLDataSetSeismogram getDataSetSeismogram(DataSet ds) {
        try {
            return new URLDataSetSeismogram(new File(getFilePath()).toURI().toURL(), 
                                            SeismogramFileTypes.fromInt(getFileType()),
                                            ds,
                                            getNetworkCode()+"."+getStationCode()+"."+getSiteCode()+"."+getChannelCode());
        } catch(MalformedURLException e) {
            throw new RuntimeException("should not happen as URL from file.", e);
        } catch(UnsupportedFileTypeException e) {
            throw new RuntimeException("should not happen, type from database: "+getFileType());
        }
    }
    public String getNetworkCode() {
        return netCode;
    }

    public String getStationCode() {
        return staCode;
    }

    public String getSiteCode() {
        return siteCode;
    }

    public String getChannelCode() {
        return chanCode;
    }

    public Timestamp getBeginTime() {
        return beginTime;
    }

    public Timestamp getEndTime() {
        return endTime;
    }

    public String getFilePath() {
        return filePath;
    }

    public int getFileType() {
        return fileType;
    }

    protected void setNetworkCode(String netCode) {
        this.netCode = netCode;
    }

    protected void setStationCode(String staCode) {
        this.staCode = staCode;
    }

    protected void setSiteCode(String siteCode) {
        this.siteCode = siteCode;
    }

    protected void setChannelCode(String chanCode) {
        this.chanCode = chanCode;
    }

    protected void setBeginTime(Timestamp beginTime) {
        this.beginTime = beginTime;
    }

    protected void setEndTime(Timestamp endTime) {
        this.endTime = endTime;
    }

    protected void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    protected void setFileType(int fileType) {
        this.fileType = fileType;
    }

    public int getDbid() {
        return dbid;
    }

    protected void setDbid(int dbid) {
        this.dbid = dbid;
    }

    protected String netCode;
    protected String staCode;
    protected String siteCode;
    protected String chanCode;
    protected Timestamp beginTime;
    protected Timestamp endTime;
    protected String filePath;
    protected int fileType;
}