package edu.sc.seis.fissuresUtil.chooser;

import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.model.*;
import edu.iris.Fissures.network.*;
import java.util.*;

/**
 * ChannelGrouperImpl.java
 *
 *
 * Created: Fri Jun 21 15:06:45 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class ChannelGrouperImpl {
    /**
     * Creates a new <code>ChannelGrouperImpl</code> instance.
     *
     */
    public ChannelGrouperImpl (){
    }

  


    /**
     * This function returns a ChannelGroup corresponding to the channelId.
     *
     * @param channels a <code>Channel[]</code> value
     * @param channelId a <code>ChannelId</code> value
     * @return a <code>ChannelId[]</code> value
     */
    public Channel[] retrieve_grouping( Channel[] channels, Channel channel) {
	ChannelId channelId = channel.get_id();
	String givenChannelStr = channelId.channel_code;
	String givenPrefixStr = givenChannelStr.substring(0, givenChannelStr.length() - 1);
	char givenOrientation = givenChannelStr.charAt(givenChannelStr.length() - 1);
	String searchString = "";
	Channel[] rtnchannels = new Channel[3];

	for(int j = 0; j < channels.length; j++) {
	    //System.out.println("THe channel Str is "+ ChannelIdUtil.toString(channels[j].get_id()));
	}
	//System.out.println("The given Channel is "+givenChannelStr+" given Orientation is "+givenOrientation);
	for(int i = 0; i < patterns.length; i++) {
	    if(patterns[i].indexOf(givenOrientation) != -1) {
		searchString = patterns[i];
		//System.out.println("The search String is "+searchString);
		searchString = searchString.replace(givenOrientation, '_');
		//System.out.println("The search string after is "+searchString);
	    }
	    else {
		return new Channel[0];
	    }
	    int count = 0;
	    rtnchannels = new Channel[3];
	    rtnchannels[count] = channel;
	    count++;
	    //System.out.println("The length of the channels is "+channels.length);
	    for(int counter = 0; counter < channels.length; counter++) {
		String channelStr = channels[counter].get_id().channel_code;
		String prefixStr = channelStr.substring(0, channelStr.length() - 1);
		char orientation = channelStr.charAt(channelStr.length() - 1);
		//System.out.println("The channelstr is "+channelStr);
		if(prefixStr.equals(givenPrefixStr) && searchString.indexOf(orientation) != -1) {
		    //System.out.println("The searchString is "+searchString);
		    searchString = searchString.replace(orientation,'_');
		    rtnchannels[count] = channels[counter];
		    count++;
		    //System.out.println("ORIENTATION "+orientation+"The matched channelStr is "+channelStr);
		}
	      
	    }
	    if( searchString.equals("___") ) {
		//System.out.println("---------------___----------> RETURNING THE CHANNELS");
		for(int counter = 0; counter < rtnchannels.length; counter++) {

		    //if(rtnchannels[counter] == null) //System.out.println(" IS NULL ");
		    // else //System.out.println(" IS NOT NULL ");
		}
		return rtnchannels;
	    }

	}
	return new Channel[0];
	
    }
   
    public ChannelId[] retrieve_grouping( ChannelId[] channelIds, ChannelId channelId) {
	String givenChannelStr = channelId.channel_code;
 	String givenPrefixStr = givenChannelStr.substring(0, givenChannelStr.length() - 1);
	 char givenOrientation = givenChannelStr.charAt(givenChannelStr.length() - 1);
	String searchString = "";
	ChannelId[] rtnchannels = new ChannelId[3];

	for(int j = 0; j < channelIds.length; j++) {
	    //System.out.println("THe channel Str is "+ ChannelIdUtil.toString(channels[j].get_id()));
	}
	//System.out.println("The given Channel is "+givenChannelStr+" given Orientation is "+givenOrientation);
	for(int i = 0; i < patterns.length; i++) {
	    if(patterns[i].indexOf(givenOrientation) != -1) {
		searchString = patterns[i];
		//System.out.println("The search String is "+searchString);
		searchString = searchString.replace(givenOrientation, '_');
		//System.out.println("The search string after is "+searchString);
	    }
	    else {
		return new ChannelId[0];
	    }
	    int count = 0;
	    rtnchannels = new ChannelId[3];
	    rtnchannels[count] = channelId;
	    count++;
	    //System.out.println("The length of the channels is "+channels.length);
 	    for(int counter = 0; counter < channelIds.length; counter++) {
	 	String channelStr = channelIds[counter].channel_code;
		String prefixStr = channelStr.substring(0, channelStr.length() - 1);
		char orientation = channelStr.charAt(channelStr.length() - 1);
		//System.out.println("The channelstr is "+channelStr);
		if(prefixStr.equals(givenPrefixStr) && searchString.indexOf(orientation) != -1) {
		    //System.out.println("The searchString is "+searchString);
		    searchString = searchString.replace(orientation,'_');
		    rtnchannels[count] = channelIds[counter];
		    count++;
		    //System.out.println("ORIENTATION "+orientation+"The matched channelStr is "+channelStr);
		}
	      
	    }
	    if( searchString.equals("___") ) {
		//System.out.println("---------------___----------> RETURNING THE CHANNELS");
		for(int counter = 0; counter < rtnchannels.length; counter++) {

		    //if(rtnchannels[counter] == null) //System.out.println(" IS NULL ");
		    // else //System.out.println(" IS NOT NULL ");
		}
		return rtnchannels;
	    }

	}
	return new ChannelId[0];
	
    }

    String[] patterns = new String[] {"NEZ",
				      "12Z",
				      "UVZ",
				      "123",
				      "UVW"};
	
     
}//ChannelGrouperImpl

/***************
	Channel[] channelGroup = new Channel[3];
	String givenChannelStr = ChannelIdUtil.toString(channelId);
	String matchedGroup  = new String();
	String keyOrientation = "";
	matchedGroup = "";
	String searchGroup = new String();
	searchGroup = "Z";
	System.out.println("The channelId String is "+givenChannelStr);
	
	for(int counter = 0; counter < channels.length; counter++) {

	    String channelStr = ChannelIdUtil.toString(channels[counter].get_id());
	    char orientation = channelStr.charAt(channelStr.length() - 1);
	    for(int subcounter = 0; subcounter < orientationHeuristic.length; subcounter++) {
		if(orientation == 'Z') {
		    searchGroup = "Z";
		} else if(orientation == '3') {
		    searchGroup = "3";
		} else if(orientation == 'W') {
		    searchGroup = "W";
		}
		if(searchGroup.indexOf(orientation) != -1) {
		    matchedGroup = matchedGroup + Character.toString(orientation);
		}
	    }
	    if(searchGroup == "Z") {
		searchGroup = "NE";
	    } else if(searchGroup  == "12"){
		searchGroup = "UV";
	    } else if(searchGroup == "UV") {
		searchGroup = null;
	    }
	}
	return null;

*****************/
