package edu.sc.seis.fissuresUtil.database;

import java.util.ArrayList;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.network.ChannelIdUtil;

/**
 * RequestFilterUtil.java
 *
 *
 * Created: Fri Feb 14 09:36:28 2003
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class RequestFilterUtil {
    public RequestFilterUtil (){
	
    }

    public static RequestFilter[] leftOuterJoin(RequestFilter[] filterSeqOne,
						RequestFilter[] filterSeqTwo) {
	//System.out.println("In left Outer Join method");
	ArrayList arrayList = new ArrayList();
	for(int counter = 0; counter < filterSeqOne.length; counter++) {

	    RequestFilter[] values = isAvailable(filterSeqTwo, filterSeqOne[counter]);
	    for(int i = 0; i < values.length; i++) {
		arrayList.add(values[i]);
		
	    }
	}
	RequestFilter[] rtnValues = new RequestFilter[arrayList.size()];
	rtnValues = (RequestFilter[]) arrayList.toArray(rtnValues);
	return rtnValues;
    }

    public static RequestFilter[] rightOuterJoin(RequestFilter[] filterSeqOne,
						 RequestFilter[] filterSeqTwo) {
	return leftOuterJoin(filterSeqTwo, filterSeqOne);
    }

    public static RequestFilter[] leftInnerJoin(RequestFilter[] filterSeqOne,
						RequestFilter[] filterSeqTwo) {
	return new RequestFilter[0];
    }

    public static RequestFilter[] rightInnerJoin(RequestFilter[] filterSeqOne, 
						 RequestFilter[] filterSeqTwo) {
	return leftInnerJoin(filterSeqTwo, filterSeqOne);
    }

    public static RequestFilter[] union(RequestFilter[] filterSeqOne, 
					RequestFilter[] filterSeqTwo) {
	return new RequestFilter[0];
    }

    public static RequestFilter[] intersection(RequestFilter[] filterSeqOne,
					       RequestFilter[] filterSeqTwo) {
	return new RequestFilter[0];
    }
    /**
     * This function assumes that the RequestFilter array passed 
     * is sorted by start_time.
     
     */

    public static  RequestFilter[] isAvailable(RequestFilter[]  sequence,  RequestFilter rf) {
	ArrayList arrayList = new ArrayList();
	MicroSecondDate rfBeginDate = new MicroSecondDate(rf.start_time);
	MicroSecondDate rfEndDate = new MicroSecondDate(rf.end_time);
	for(int counter = 0; counter < sequence.length; counter++) {
	    
	    if(ChannelIdUtil.toString(sequence[counter].channel_id).equals(ChannelIdUtil.toString(rf.channel_id))) 
	    {
		MicroSecondDate seqBeginDate = new MicroSecondDate(sequence[counter].start_time);
		MicroSecondDate seqEndDate = new MicroSecondDate(sequence[counter].end_time);
		if( ((seqBeginDate.before(rfBeginDate) || seqBeginDate.equals(rfBeginDate)) &&
		     (seqEndDate.before(rfBeginDate) || seqEndDate.equals(rfBeginDate))) ||
		    ((seqBeginDate.after(rfEndDate) || seqBeginDate.equals(rfEndDate)) &&
		     (seqEndDate.after(rfEndDate) || seqEndDate.equals(rfEndDate))) ) {
		    continue;
		}
		if(rfBeginDate.before(seqBeginDate)) {
		    RequestFilter temprf = new RequestFilter(rf.channel_id,
							     rfBeginDate.getFissuresTime(),
							     seqBeginDate.getFissuresTime());
		    
		    arrayList.add(temprf);
		} else {
		    rfBeginDate = seqEndDate;
		}

		if(rfEndDate.after(seqEndDate)) {

		    rfBeginDate = seqEndDate;
		}

	    }
									   
	}
	if(rfBeginDate.before(rfEndDate)) {
	    RequestFilter temprf = new RequestFilter(rf.channel_id,
						     rfBeginDate.getFissuresTime(),
						     rfEndDate.getFissuresTime());
	    
	    arrayList.add(temprf);
	}
	RequestFilter[] rtnValues = new RequestFilter[arrayList.size()];
	rtnValues = (RequestFilter[]) arrayList.toArray(rtnValues);
	return rtnValues;
    }
    

    public static  temp[] isAvailable(temp[]  sequence,  temp rf) {
	ArrayList arrayList = new ArrayList();
	MicroSecondDate rfBeginDate = new MicroSecondDate(rf.start_time);
	MicroSecondDate rfEndDate = new MicroSecondDate(rf.end_time);
	for(int counter = 0; counter < sequence.length; counter++) {
	    
	    //if(ChannelIdUtil.toString(sequence[counter].channel_id).equals(ChannelIdUtil.toString(rf.channel_id))) 
	    {
		MicroSecondDate seqBeginDate = new MicroSecondDate(sequence[counter].start_time);
		MicroSecondDate seqEndDate = new MicroSecondDate(sequence[counter].end_time);
		if( ((seqBeginDate.before(rfBeginDate) || seqBeginDate.equals(rfBeginDate)) &&
		     (seqEndDate.before(rfBeginDate) || seqEndDate.equals(rfBeginDate))) ||
		    ((seqBeginDate.after(rfEndDate) || seqBeginDate.equals(rfEndDate)) &&
		     (seqEndDate.after(rfEndDate) || seqEndDate.equals(rfEndDate))) ) {
		    continue;
		}
		if(rfBeginDate.before(seqBeginDate)) {
		    /*RequestFilter temp = new RequestFilter(rf.channel_id,
							   rfBeginDate.getFissuresTime(),
							   seqBeginDate.getFissuresTime());*/
		    temp t = new temp(rfBeginDate.getFissuresTime(),
				      seqBeginDate.getFissuresTime());

		    arrayList.add(t);
		} else {
		    rfBeginDate = seqEndDate;
		}

		if(rfEndDate.after(seqEndDate)) {

		    rfBeginDate = seqEndDate;
		}

	    }
									   
	}
	if(rfBeginDate.before(rfEndDate)) {
	    temp  t =  new temp(rfBeginDate.getFissuresTime(),
				rfEndDate.getFissuresTime());
	    arrayList.add(t);
	}
	temp[] rtnValues = new temp[arrayList.size()];
	rtnValues = (temp[]) arrayList.toArray(rtnValues);
	return rtnValues;
    }

    public static void main(String[] args) {

	String[] bstr = new String[] {//"20001209T09:00:00.000",
				      /*"20001209T10:00:00.000",
				      "20001209T11:15:00.000",
				      "20001209T11:35:00.000",
				      "20001209T12:10:00.000",
				      "20001209T12:40:00.000",
				      "20001209T13:00:00.000",
				      "20001209T13:20:00.000",
				      "20001209T13:40:00.000",*/
				      "20001209T13:55:00.000",
				      //"20001209T11:30:00.000",
				      "20001209T8:30:00.000"};

	String[] estr = new String[] {//"20001209T10:30:00.000",
	    /*"20001209T10:30:00.000",
				      "20001209T11:25:00.000",
				      "20001209T12:00:00.000",
				      "20001209T12:40:00.000",
				      "20001209T12:50:00.000",
				      "20001209T13:20:00.000",
				      "20001209T13:30:00.000",
				      "20001209T13:50:00.000",*/
				      "20001209T14:10:00.000",
				      "20001209T08:40:00.000",
	};
	
	temp params[] = new temp[bstr.length - 1];
	edu.iris.Fissures.Time bTimes[] = new edu.iris.Fissures.Time[bstr.length];
	edu.iris.Fissures.Time eTimes[] = new edu.iris.Fissures.Time[estr.length];
	for(int i = 0; i < bstr.length - 1; i++) {
	    
	    bTimes[i] = new edu.iris.Fissures.Time(bstr[i], 0);
	    eTimes[i] = new edu.iris.Fissures.Time(estr[i], 0);
	    params[i] = new temp(bTimes[i], eTimes[i]);
	}
	edu.iris.Fissures.Time bTime = new edu.iris.Fissures.Time(bstr[bstr.length - 1], 0);
	edu.iris.Fissures.Time eTime = new edu.iris.Fissures.Time(estr[estr.length - 1], 0);
	temp param = new temp(bTime, eTime);
	temp[] rtnValues = RequestFilterUtil.isAvailable(params, param);
	for(int counter = 0; counter < rtnValues.length; counter++) {

	    // System.out.print("The Begin Time is "+new MicroSecondDate(rtnValues[counter].start_time));
	    //System.out.println("  The End Time is "+new MicroSecondDate(rtnValues[counter].end_time));
	}
	
	
    }

    
    
}// RequestFilterUtil

class temp{
	temp(edu.iris.Fissures.Time beginTime, 
	     edu.iris.Fissures.Time endTime) {

	    this.start_time = beginTime;
	    this.end_time = endTime;
	}

    public boolean equals(temp t) {
	MicroSecondDate ps = new MicroSecondDate(t.start_time);
	MicroSecondDate pe = new MicroSecondDate(t.end_time);
	MicroSecondDate as = new MicroSecondDate(this.start_time);
	MicroSecondDate ae = new MicroSecondDate(this.end_time);
	if (  ps.equals(as) && pe.equals(ae)) return true;
	return false;
    }

	public edu.iris.Fissures.Time start_time;

	public edu.iris.Fissures.Time end_time;
	
    }
       
