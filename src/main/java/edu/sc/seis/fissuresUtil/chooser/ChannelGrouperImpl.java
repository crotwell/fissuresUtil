package edu.sc.seis.fissuresUtil.chooser;

import java.util.Collection;
import java.util.Iterator;
import java.util.TreeMap;
import org.apache.log4j.Category;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.network.ChannelIdUtil;

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
     * Given a channel and an array of Channels, this method returns the ChannelGroup
     * as an array of Channels corresponding to channel.
     *
     * @param channels a <code>Channel[]</code> value
     * @param channel a <code>Channel</code> value
     * @return a <code>Channel[]</code> value
     */
    public Channel[] retrieve_grouping( Channel[] channels, Channel channel) {
    ChannelId channelId = channel.get_id();
    String givenChannelStr = channelId.channel_code;
    String givenPrefixStr = givenChannelStr.substring(0, givenChannelStr.length() - 1);
    char givenOrientation = givenChannelStr.charAt(givenChannelStr.length() - 1);
    String searchString = "";
    Channel[] rtnchannels = new Channel[3];

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
        logger.debug("---------------___----------> RETURNING THE CHANNELS");
        return rtnchannels;
        }

    }
    return new Channel[0];
    
    }
   
    /**
     * Given a channelId and an array of ChannelIds, this method returns the ChannelGroup
     * as an array of ChannelIds corresponding to channelId.
     *
     * @param channelIds a <code>ChannelId[]</code> value
     * @param channelId a <code>ChannelId</code> value
     * @return a <code>ChannelId[]</code> value
     */
    public ChannelId[] retrieve_grouping( ChannelId[] channelIds, ChannelId channelId) {
    String givenChannelStr = channelId.channel_code;
    String givenPrefixMatchStr = ChannelIdUtil.toString(channelId).substring(0,
                    ChannelIdUtil.toString(channelId).indexOf(givenChannelStr));
    String givenPrefixStr = givenChannelStr.substring(0, givenChannelStr.length() - 1);
     char givenOrientation = givenChannelStr.charAt(givenChannelStr.length() - 1);
    String searchString = "";
    ChannelId[] rtnchannels = new ChannelId[3];
    logger.debug("The given prefixxStr is "+givenPrefixStr);
    for(int j = 0; j < channelIds.length; j++) {
        logger.debug("THe channel Str is "+ ChannelIdUtil.toString(channelIds[j]));
    }
    logger.debug("The given Channel is "+givenChannelStr+" given Orientation is "+givenOrientation);
    for(int i = 0; i < patterns.length; i++) {
        if(patterns[i].indexOf(givenOrientation) != -1) {
        searchString = patterns[i];
        logger.debug("The search String is "+searchString);
        searchString = searchString.replace(givenOrientation, '_');
        logger.debug("The search string after is "+searchString);
        }
        else {
        return new ChannelId[0];
        }
        int count = 0;
        rtnchannels = new ChannelId[3];
        rtnchannels[count] = channelId;
        count++;
        logger.debug("The length of the channels is "+channelIds.length);
        for(int counter = 0; counter < channelIds.length; counter++) {
        String channelStr = channelIds[counter].channel_code;
        String prefixMatchStr = ChannelIdUtil.toString(channelIds[counter]).substring(0,
                    ChannelIdUtil.toString(channelIds[counter]).indexOf(channelStr));
        String prefixStr = channelStr.substring(0, channelStr.length() - 1);
        char orientation = channelStr.charAt(channelStr.length() - 1);
        
        logger.debug("The channelstr is "+channelStr);
        logger.debug("The prefixStr is "+prefixStr);
        logger.debug("orientation is "+orientation);
        //the below if will not be need once the sac is fixed
        //if(orientation == givenOrientation) rtnchannels[0] = channelIds[counter];
        if(givenPrefixMatchStr.equals(prefixMatchStr) &&
           prefixStr.equals(givenPrefixStr) &&
           searchString.indexOf(orientation) != -1) {
        //if(givenPrefixStr.indexOf(prefixStr) != -1 && searchString.indexOf(orientation) != -1) {
            logger.debug("The searchString is "+searchString);
            searchString = searchString.replace(orientation,'_');
            rtnchannels[count] = channelIds[counter];
            count++;
            logger.debug("ORIENTATION "+orientation+"The matched channelStr is "+channelStr);
        }
          
        }
        if( searchString.equals("___") ) {
           logger.debug("---------------___----------> RETURNING THE CHANNELS");
        for(int counter = 0; counter < rtnchannels.length; counter++) {

           if(rtnchannels[counter] == null) logger.debug(" "+counter+"  IS NULL ");
           else logger.debug(" "+counter+"  IS NOT NULL ");
        }
        return sortChannelGroup(rtnchannels);
        }

    }
    return new ChannelId[0];
    
    }

    /**
     * Given an array of channelIds this method returns sorted array of
     * channelIds. It sorts the channelIds based on the channel code.
     *
     * @param channelIds a <code>ChannelId[]</code> value
     * @return a <code>ChannelId[]</code> value
     */
    public ChannelId[] sortChannelGroup(ChannelId[] channelIds) {
    ChannelId[] rtnValues = new ChannelId[channelIds.length];
    TreeMap treeMap = new TreeMap();
    for(int counter = 0; counter < channelIds.length; counter++) {
        treeMap.put(channelIds[counter].channel_code, channelIds[counter]);
    }
    Collection collection = treeMap.values();
    Iterator iterator = collection.iterator();
    int counter = 0;
    while(iterator.hasNext()) {
        rtnValues[counter] = (ChannelId)iterator.next();
        counter++;
    }
    return rtnValues;
    }

    private String[] patterns = new String[] {"NEZ",
                      "12Z",
                      "UVZ",
                      "123",
                      "UVW"};
    static Category logger =
    Category.getInstance(ChannelGrouperImpl.class.getName());
    
     
}//ChannelGrouperImpl


