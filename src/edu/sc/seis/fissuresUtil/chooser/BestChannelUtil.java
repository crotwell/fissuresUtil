
package edu.sc.seis.fissuresUtil.chooser;

import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.model.*;
import org.apache.log4j.*;

public class BestChannelUtil {

    protected static final String[] siteCodeHeuristic = { "00", "  ", "01" };
    protected static final String[] gainCodeHeuristic = { "H", "L" };
    protected static final String[] bandCodeHeuristic = { "L", "B", "S" };

    public static String[] getSiteCodeHeuristic() {
	return siteCodeHeuristic;
    }

    public static String[] getGainCodeHeuristic() {
	return gainCodeHeuristic;
    }

    public static String[] getBandCodeHeuristic() {
	return bandCodeHeuristic;
    }

    /** Trys to find a channel whose effect time overlaps the given time
        and which has the same network, station, site and channel codes
        as the given channel id.

        @returns a channel of there is one, or null if not
    */  
    public static Channel getActiveChannel(Channel[] inChan, 
                                           Channel current, 
                                           MicroSecondDate when) {
        for (int i=0; i<inChan.length; i++) {
	    logger.info("Test inChannel "+ChannelIdUtil.toStringNoDates(inChan[i].get_id()));
            if (ChannelIdUtil.toStringNoDates(inChan[i].get_id()).equals(ChannelIdUtil.toStringNoDates(current.get_id()))) {
                if (when.before(new MicroSecondDate(inChan[i].effective_time.end_time)) &&
                    when.after(new MicroSecondDate(inChan[i].effective_time.start_time))) {
                    return inChan[i];
                } else {
		    logger.info("Channel failed time overlap. "
				+inChan[i].effective_time.end_time.date_time+" "
				+new MicroSecondDate(inChan[i].effective_time.end_time)+" "
				+when+" "
				+new MicroSecondDate(inChan[i].effective_time.start_time));
		} // end of else
            } else {
		logger.info("Channel failed code match.");
	    } // end of else
	    
        } // end of for (int i=0; i<inChan.length; i++)
        
        // no match
        return null;
    }

    /** finds the best vertical channel for the band code. All channels are
     * assumed to come from the same station. 
     * @returns best vertical channel, or null if no vertical can be found
     */	
    public static Channel getVerticalChannel(Channel[] inChan, 
					     String bandCode) {
	return getChannel(inChan, bandCode, "Z");
    }

    /** finds the best vertical channel for the band code. All channels are
     * assumed to come from the same station. Makes sure that the 2 channels
     * have the same gain and site.
     * @returns best vertical channel, or null if no vertical can be found
     */	
    public static Channel[] getHorizontalChannels(Channel[] inChan, 
						  String bandCode) {
	// try to find N,E
	Channel north = getChannel(inChan, bandCode, "N");
	// try to get east from same site, with same gain
	Channel east = getChannel(inChan,
				  bandCode, 
				  "E",
				  north.my_site.get_code(),
				  north.get_code().substring(1,2));
	if (north != null && 
	    east != null && 
	    north.my_site.get_code().equals(east.my_site.get_code()) && 
	    north.get_code().substring(1,2).equals(east.get_code().substring(1,2))) {
	    Channel[] tmp = new Channel[2];
	    tmp[0] = north;
	    tmp[1] = east;
	    return tmp;
	}
									     
	// try to find 1,2
	north = getChannel(inChan, bandCode, "1");
	east = getChannel(inChan,
			  bandCode, 
			  "2",
			  north.my_site.get_code(),
			  north.get_code().substring(1,2));
	if (north != null && 
	    east != null && 
	    north.my_site.get_code().equals(east.my_site.get_code()) && 
	    north.get_code().substring(1,2).equals(east.get_code().substring(1,2))) {
	    Channel[] tmp = new Channel[2];
	    tmp[0] = north;
	    tmp[1] = east;
	    return tmp;
	}
									     
	return null;
    }

    public static Channel getChannel(Channel[] inChan, 
				     String bandCode,
				     String orientationCode) {
	Channel tmpChannel;
	for (int h=0; h<siteCodeHeuristic.length; h++) {
	    tmpChannel = getChannel(inChan, 
				    bandCode, 
				    orientationCode, 
				    siteCodeHeuristic[h]);
	    if (tmpChannel != null) {
		return tmpChannel;
	    } // end of if (tmpChannel != null)
	}

	// oh well, return null
	logger.debug("can't find"+ bandCode+orientationCode);
	return null;
    }

    /** Finds the best channel using a gain heuristic, ie H is preferred over L,
     *  and otherwise just find a channel. Pretty simple, but in many cases it 
     *  is suffucient. 
     */
    public static  Channel getChannel(Channel[] inChan, 
				      String bandCode,
				      String orientationCode,
				      String siteCode) {
	for (int i=0; i< gainCodeHeuristic.length; i++) {
	    Channel tmp = getChannel(inChan, 
				     bandCode, 
				     orientationCode, 
				     siteCode, 
				     gainCodeHeuristic[i]);
	    if (tmp != null) {
		return tmp;
	    } // end of if (tmp != null)
	} // end of for (int i=0; i< gainHeuristic.length; i++)

	// can't find one by gain hueristic, just find one
	for (int chanNum=0; chanNum<inChan.length; chanNum++) {
	    if (inChan[chanNum].get_code().endsWith(orientationCode)
		&& inChan[chanNum].get_code().startsWith(bandCode)) {
		return inChan[chanNum];
	    }
	}

	
	// oh well, return null
	logger.debug("can't find"+ bandCode+orientationCode+siteCode);
	return null;
    }

    public static  Channel getChannel(Channel[] inChan, 
				      String bandCode,
				      String orientationCode,
				      String siteCode,
				      String gainCode) {
	for (int chanNum=0; chanNum<inChan.length; chanNum++) {
	    logger.debug("trying "+inChan[chanNum].my_site.get_code()+" "+inChan[chanNum].get_code()+" "+siteCode);
	    if (inChan[chanNum].my_site.get_code().equals(siteCode) 
		&& inChan[chanNum].get_code().endsWith(orientationCode)
		&& inChan[chanNum].get_code().startsWith(bandCode)
		&& inChan[chanNum].get_code().substring(1,2).equals(gainCode)) {
		logger.debug("returning "+inChan[chanNum].my_site.get_code()+" "+inChan[chanNum].get_code()+" "+siteCode);
		return inChan[chanNum];
	    }
	}

	// oh well, return null
	logger.debug("can't find"+ bandCode+orientationCode+siteCode+gainCode);
	return null;
    }

    static Category logger = Category.getInstance(BestChannelUtil.class.getName());

}
