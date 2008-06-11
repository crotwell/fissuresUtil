package edu.sc.seis.fissuresUtil.hibernate;

import java.sql.Timestamp;

public class SeismogramFileReference {

    public SeismogramFileReference(String netCode,
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

    public String getNetCode() {
        return netCode;
    }

    public String getStaCode() {
        return staCode;
    }

    public String getSiteCode() {
        return siteCode;
    }

    public String getChanCode() {
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

    protected void setNetCode(String netCode) {
        this.netCode = netCode;
    }

    protected void setStaCode(String staCode) {
        this.staCode = staCode;
    }

    protected void setSiteCode(String siteCode) {
        this.siteCode = siteCode;
    }

    protected void setChanCode(String chanCode) {
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

    protected int dbid;

    protected String netCode;

    protected String staCode;

    protected String siteCode;

    protected String chanCode;

    protected Timestamp beginTime;

    protected Timestamp endTime;

    protected String filePath;

    protected int fileType;
}
