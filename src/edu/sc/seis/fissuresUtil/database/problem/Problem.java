package edu.sc.seis.fissuresUtil.database.problem;

public class Problem {

    public Problem(String stationCode, String type, String status) {
        this.stationCode = stationCode;
        this.type = type;
        this.status = status;
    }

    public String getStationCode() {
        return stationCode;
    }

    public String getStatus() {
        return status;
    }

    public String getType() {
        return type;
    }

    public boolean equals(Object obj) {
        if(obj == this) {
            return true;
        }
        if(obj instanceof Problem) {
            Problem prob = (Problem)obj;
            if(prob.stationCode.equals(stationCode)) {
                if(prob.type != null && type != null) {
                    if(!prob.type.equals(type)) {
                        return false;
                    }
                } else if(prob.type != null || type != null) {
                    return false;
                    // one is null and the other is not
                }
                if(prob.status != null && status != null) {
                    if(!prob.status.equals(status)) {
                        return false;
                    }
                } else if(prob.status != null || status != null) {
                    return false;
                }
                return true;
            }
        }
        return false;
    }

    public String toString() {
        return "<Problem(stationCode=" + stationCode + ",type=" + type
                + ",status=" + status + ")>";
    }

    private String stationCode, type, status;
}
