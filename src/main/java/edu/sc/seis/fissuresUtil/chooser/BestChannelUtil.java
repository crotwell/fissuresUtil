package edu.sc.seis.fissuresUtil.chooser;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.network.ChannelIdUtil;

public class BestChannelUtil {

    protected static final String[] siteCodeHeuristic = { "00", "  ", "01" };

    protected static final String[] gainCodeHeuristic = { "H", "L" };

    protected static final String[] bandCodeHeuristic = { "B", "H", "L", "M", "S", "V", "E", "U" };

    private static final String[] orientationCodes = { "Z", "N", "E", "1", "2", "3", "U", "V", "W" };

    public static String[] getSiteCodeHeuristic() {
        return siteCodeHeuristic;
    }

    public static String[] getGainCodeHeuristic() {
        return gainCodeHeuristic;
    }

    public static String[] getBandCodeHeuristic() {
        return bandCodeHeuristic;
    }

    public static Channel[] getBestMotionVector(Channel[] allChannels) {
        for (int i = 0; i < bandCodeHeuristic.length; i++) {
            Channel[] out = getBestMotionVector(allChannels, bandCodeHeuristic[i]);
            if (out != null) {
                return out;
            }
        }
        return null;
    }
    
    public static  Channel[] getBestMotionVector(Channel[] allChannels,
                                                 String bandCode) {
        Channel[] tmpH = BestChannelUtil.getHorizontalChannels(allChannels,
                                                               bandCode);
        Channel tmpV = null;
        if(tmpH != null && tmpH.length != 0) {
            // look for channel with same band, site and gain,
            // but with orientation code Z
            tmpV = BestChannelUtil.getChannel(allChannels,
                                              bandCode,
                                              "Z",
                                              tmpH[0].getSite().get_code(),
                                              tmpH[0].get_code()
                                                      .substring(1, 2));
            if(tmpV != null) {
                return new Channel[] {tmpH[0], tmpH[1], tmpV};
            }
        }
        return null;
    }
    
    /**
     * Prunes channels whose effective time does not overlap the given time.
     */
    public static Channel[] pruneChannels(Channel[] inChan, MicroSecondDate when) {
        LinkedList out = new LinkedList();
        for (int i = 0; i < inChan.length; i++) {
            if (when.before(new MicroSecondDate(
                    inChan[i].getEndTime()))
                    && when.after(new MicroSecondDate(
                            inChan[i].getBeginTime()))) {
                out.add(inChan[i]);
            }
        }
        return (Channel[]) out.toArray(new Channel[0]);
    }

    /**
     * Trys to find a channel whose effect time overlaps the given time and
     * which has the same network, station, site and channel codes as the given
     * channel id.
     * 
     * @returns a channel of there is one, or null if not
     */
    public static Channel getActiveChannel(Channel[] inChan, Channel current,
            MicroSecondDate when) {
        for (int i = 0; i < inChan.length; i++) {
            if (ChannelIdUtil.toStringNoDates(inChan[i].get_id()).equals(
                    ChannelIdUtil.toStringNoDates(current.get_id()))) {
                if (when.before(new MicroSecondDate(
                        inChan[i].getEndTime()))
                        && when.after(new MicroSecondDate(
                                inChan[i].getBeginTime()))) { return inChan[i]; }
            }
        } // end of for (int i=0; i<inChan.length; i++)

        // no match
        return null;
    }

    /**
     * finds the best vertical channel for the band code. All channels are
     * assumed to come from the same station.
     * 
     * @returns best vertical channel, or null if no vertical can be found
     */
    public static Channel getVerticalChannel(Channel[] inChan, String bandCode) {
        return getChannel(inChan, bandCode, "Z");
    }

    /**
     * finds the best horizontal channels for the band code. All channels are
     * assumed to come from the same station. Makes sure that the 2 channels
     * have the same gain and site.
     * 
     * @returns best horizontal channels, or null if no horizontals can be found
     */
    public static Channel[] getHorizontalChannels(Channel[] inChan,
            String bandCode) {
        for (int h = 0; h < siteCodeHeuristic.length; h++) {
            // try to find N,E
            Channel north = getChannel(inChan, bandCode, "N",
                    siteCodeHeuristic[h]);
            Channel east;
            if (north != null) {
                east = getChannelForOrientation(inChan, "E", north);
                if (east != null) { return new Channel[] { north, east }; }
            } // end of if ()

            // try to find 1,2
            north = getChannel(inChan, bandCode, "1");
            if (north != null) {
                east = getChannelForOrientation(inChan, "2", north);
                if (east != null) { return new Channel[] { north, east }; }
            } // end of if ()
        }

        return null;
    }

    private static Channel getChannelForOrientation(Channel[] group,
            String orientation, Channel matchThis) {
        return getChannel(group, getBand(matchThis), orientation,
                matchThis.getSite().get_code(), getGain(matchThis));
    }

    public static String getBand(Channel chan) {
        return getBand(chan.get_code());
    }

    public static String getBand(String channelCode) {
        return channelCode.substring(0, 1);
    }

    public static String getGain(Channel chan) {
        return getGain(chan.get_code());
    }

    public static String getGain(String channelCode) {
        return channelCode.substring(1, 2);
    }

    public static String getOrientation(Channel chan) {
        return getOrientation(chan.get_code());
    }

    public static String getOrientation(String channelCode) {
        return channelCode.substring(2, 3);
    }

    public static Channel[] getChannels(Channel[] staChans, String bc) {
        List results = new ArrayList();
        for (int i = 0; i < orientationCodes.length; i++) {
            String orientation = orientationCodes[i];
            Channel chan = getChannel(staChans, bc, orientation);
            if (chan != null) {
                results.add(chan);
                for (int j = 0; j < orientationCodes.length; j++) {
                    String subOrient = orientationCodes[j];
                    if (!subOrient.equals(orientation)) {
                        Channel additional = getChannelForOrientation(staChans, subOrient, chan);
                        if(additional != null) {
                            results.add(additional);
                        }
                    }
                }
                break;
            }
        }
        return (Channel[])results.toArray(new Channel[0]);
    }

    public static Channel getChannel(Channel[] inChan, String bandCode,
            String orientationCode) {
        Channel tmpChannel;
        for (int h = 0; h < siteCodeHeuristic.length; h++) {
            tmpChannel = getChannel(inChan, bandCode, orientationCode,
                    siteCodeHeuristic[h]);
            if (tmpChannel != null) { return tmpChannel; } // end of if
            // (tmpChannel !=
            // null)
        }

        // oh well, return null
        return null;
    }

    /**
     * Finds the best channel using a gain heuristic, ie H is preferred over L,
     * and otherwise just find a channel. Pretty simple, but in many cases it is
     * suffucient.
     */
    public static Channel getChannel(Channel[] inChan, String bandCode,
            String orientationCode, String siteCode) {
        for (int i = 0; i < gainCodeHeuristic.length; i++) {
            Channel tmp = getChannel(inChan, bandCode, orientationCode,
                    siteCode, gainCodeHeuristic[i]);
            if (tmp != null) { return tmp; } // end of if (tmp != null)
        } // end of for (int i=0; i< gainHeuristic.length; i++)

        // can't find one by gain hueristic, just find one
        for (int chanNum = 0; chanNum < inChan.length; chanNum++) {
            if (inChan[chanNum].get_id().site_code.equals(siteCode)
                    && inChan[chanNum].get_code().endsWith(orientationCode)
                    && inChan[chanNum].get_code().startsWith(bandCode)) { return inChan[chanNum]; }
        }

        // oh well, return null
        return null;
    }

    public static Channel getChannel(Channel[] inChan, String bandCode,
            String orientationCode, String siteCode, String gainCode) {
        String desiredChannelCode = bandCode + gainCode + orientationCode;
        for (int chanNum = 0; chanNum < inChan.length; chanNum++) {
            if (inChan[chanNum].getSite().get_code().equals(siteCode)
                    && inChan[chanNum].get_code().equals(desiredChannelCode)) { return inChan[chanNum]; }
        }
        // oh well, return null
        return null;
    }

    private static Logger logger = LoggerFactory.getLogger(BestChannelUtil.class.getName());

}
