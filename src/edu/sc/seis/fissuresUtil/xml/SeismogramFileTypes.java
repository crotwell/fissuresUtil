package edu.sc.seis.fissuresUtil.xml;

/**
 * SeismogramFileTypes.java
 *
 *
 * Created: Tue Mar 18 15:38:13 2003
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class SeismogramFileTypes {
    private SeismogramFileTypes (String val){
	this.val = val;
    }

    public boolean equals(Object obj) {
	if(! (obj instanceof SeismogramFileTypes) ) return false;
	return ((SeismogramFileTypes)obj).getValue().equals(this.val);
    }

    public String getValue() {
	return this.val;
    }
   

    public static final SeismogramFileTypes SAC = new SeismogramFileTypes("sac");

    public static final SeismogramFileTypes MSEED = new SeismogramFileTypes("mseed");
    
    private String val;
    
}// SeismogramFileTypes
