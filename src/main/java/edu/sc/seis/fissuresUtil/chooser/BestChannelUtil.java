package edu.sc.seis.fissuresUtil.chooser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.network.ChannelImpl;
import edu.sc.seis.TauP.SphericalCoords;

public class BestChannelUtil {

    public BestChannelUtil() {}
    
    public static final String[] DEFAULT_SITE_CODE_HEURISTIC = { "00", "  ", "01", "02", "10" };

    public static final String[] DEFAULT_GAIN_CODE_HEURISTIC = { "H", "L" };

    public static final String[] DEFAULT_BAND_CODE_HEURISTIC = { "B", "H", "L", "M", "S", "V", "E", "U" };

    public static final String[] DEFAULT_ORIENTATION_CODES = { "Z", "N", "E", "1", "2", "3", "U", "V", "W" };
    
    public static final float DEFAULT_MAX_DIP_OFFSET = 5.0f;

    protected String[] siteCodeHeuristic = DEFAULT_SITE_CODE_HEURISTIC;

    protected String[] gainCodeHeuristic = DEFAULT_GAIN_CODE_HEURISTIC;

    protected String[] bandCodeHeuristic = DEFAULT_BAND_CODE_HEURISTIC;

    protected String[] orientationCodeHeuristic = DEFAULT_ORIENTATION_CODES;
    
    protected float maxDipOffset = DEFAULT_MAX_DIP_OFFSET;

    public float getMaxDipOffset() {
        return maxDipOffset;
    }
    
    public void setMaxDipOffset(float maxDipOffset) {
        this.maxDipOffset = maxDipOffset;
    }


    public String[] getOrientationCodeHeuristic() {
        return orientationCodeHeuristic;
    }

    
    public void setOrientationCodeHeuristic(String[] orientationCodes) {
        this.orientationCodeHeuristic = orientationCodes;
    }

    
    public void setSiteCodeHeuristic(String[] siteCodeHeuristic) {
        this.siteCodeHeuristic = siteCodeHeuristic;
    }

    
    public void setGainCodeHeuristic(String[] gainCodeHeuristic) {
        this.gainCodeHeuristic = gainCodeHeuristic;
    }

    
    public void setBandCodeHeuristic(String[] bandCodeHeuristic) {
        this.bandCodeHeuristic = bandCodeHeuristic;
    }

    public String[] getSiteCodeHeuristic() {
        return siteCodeHeuristic;
    }

    public String[] getGainCodeHeuristic() {
        return gainCodeHeuristic;
    }

    public String[] getBandCodeHeuristic() {
        return bandCodeHeuristic;
    }

    public Channel getBestVerticalChannel(List<ChannelImpl> inChanList) {
        return getBestChannel(getAllVertical(inChanList));
    }
    
    public ChannelImpl getBestChannel(List<ChannelImpl> inChanList) {
        for (int i = 0; i < siteCodeHeuristic.length; i++) {
            List<ChannelImpl> siteChans = new ArrayList<ChannelImpl>();
            for (ChannelImpl c : inChanList) {
                if (c.getSite().get_code().equals(siteCodeHeuristic[i])) {
                    siteChans.add(c);
                }
            }
            if (siteChans.size() == 0) {continue;}
            for (int j = 0; j < bandCodeHeuristic.length; j++) {
                List<ChannelImpl> bandChans = new ArrayList<ChannelImpl>();
                for (ChannelImpl c : siteChans) {
                    if (ChannelIdUtil.getBandCode(c.getId()).equals(bandCodeHeuristic[j])) {
                        bandChans.add(c);
                    }
                }
                if (bandChans.size() == 0) {continue;}
                for (int k = 0; k < gainCodeHeuristic.length; k++) {
                    List<ChannelImpl> gainChans = new ArrayList<ChannelImpl>();
                    for (ChannelImpl c : bandChans) {
                        if (ChannelIdUtil.getGainCode(c.getId()).equals(gainCodeHeuristic[j])) {
                            gainChans.add(c);
                        }
                    }
                    if (gainChans.size() == 0) {continue;}
                    for (int m = 0; m < orientationCodeHeuristic.length; m++) {
                        for (ChannelImpl vChan : gainChans) {
                            if (ChannelIdUtil.getOrientationCode(vChan.getId()).equals(orientationCodeHeuristic[m])) {
                                return vChan;
                            }
                        }
                    }
                }
            }
        }
        if (inChanList.size() != 0) {
            // oh well, just return something
            return inChanList.get(0);
        }
        return null;
    }
    
    public ChannelImpl[] getBestMotionVector(List<ChannelImpl> inChanList) {
        for (int i = 0; i < siteCodeHeuristic.length; i++) {
            List<ChannelImpl> siteChans = new ArrayList<ChannelImpl>();
            for (ChannelImpl siteChan : inChanList) {
                if (siteChan.getSite().get_code().equals(siteCodeHeuristic[i])) {
                    siteChans.add(siteChan);
                }
            }
            if (siteChans.size() == 0) {continue;}
            for (int j = 0; j < bandCodeHeuristic.length; j++) {
                List<ChannelImpl> bandChans = new ArrayList<ChannelImpl>();
                for (ChannelImpl c : siteChans) {
                    if (ChannelIdUtil.getBandCode(c.getId()).equals(bandCodeHeuristic[j])) {
                        bandChans.add(c);
                    }
                }
                if (bandChans.size() == 0) {continue;}
                for (int k = 0; k < gainCodeHeuristic.length; k++) {
                    List<ChannelImpl> gainChans = new ArrayList<ChannelImpl>();
                    for (ChannelImpl c : bandChans) {
                        if (ChannelIdUtil.getGainCode(c.getId()).equals(gainCodeHeuristic[k])) {
                            gainChans.add(c);
                        }
                    }
                    if (gainChans.size() == 0) {continue;}
                    List<ChannelImpl> vList = getAllVertical(gainChans);
                    List<ChannelImpl> hList = getAllHorizontal(gainChans);
                    for (int m = 0; m < orientationCodeHeuristic.length; m++) {
                        for (ChannelImpl vChan : vList) {
                            if (ChannelIdUtil.getOrientationCode(vChan.getId()).equals(orientationCodeHeuristic[m])) {
                                ChannelImpl[] out = new ChannelImpl[3];
                                out[0] = vChan;
                                int found = 1;
                                for (int n = 0; n < orientationCodeHeuristic.length; n++) {
                                    for (ChannelImpl hChan : hList) {
                                        if (ChannelIdUtil.getOrientationCode(hChan.getId()).equals(orientationCodeHeuristic[n])) {                                            if (found == 2) {
                                                //might have found the third
                                                if (arePerpendicular(out[1], hChan)) {
                                                    out[found] = hChan;
                                                    return out;
                                                }
                                            } else {
                                                out[found] = hChan;
                                                found++;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    private boolean arePerpendicular(Channel channel, Channel hChan) {
        if (Math.abs(SphericalCoords.distance(channel.getOrientation().dip, channel.getOrientation().azimuth,
                                     hChan.getOrientation().dip, hChan.getOrientation().azimuth) - 90) < maxDipOffset ) {
            // with tol of being 90 deg apart
            return true;
        }
        return false;
    }
    
    /**
     * Prunes channels whose effective time does not overlap the given time.
     */
    public static List<ChannelImpl> pruneChannels(List<ChannelImpl> inChan, MicroSecondDate when) {
        List<ChannelImpl> out = new ArrayList<ChannelImpl>();
        for (ChannelImpl c : inChan) {
            if (when.before(new MicroSecondDate(c.getEndTime()))
                    && when.after(new MicroSecondDate(c.getBeginTime()))) {
                out.add(c);
            }
        }
        return out;
    }
    
    public List<ChannelImpl> getAllHorizontal(List<ChannelImpl> inChan) {
        ArrayList<ChannelImpl> onlyHorizontal = new ArrayList<ChannelImpl>();
        for (ChannelImpl channel : inChan) {
            if (Math.abs(channel.getOrientation().dip) < maxDipOffset) {
                onlyHorizontal.add(channel);
            }
        }
        return onlyHorizontal;
    }
    
    public List<ChannelImpl> getAllVertical(List<ChannelImpl> inChan) {
        ArrayList<ChannelImpl> onlyVertical = new ArrayList<ChannelImpl>();
        for (ChannelImpl c : inChan) {
            if (Math.abs(c.getOrientation().dip) > 90 - maxDipOffset) {
                onlyVertical.add(c);
            }
        }
        return onlyVertical;
    }
    
    public static List<ChannelImpl> getAllBand(List<ChannelImpl> inChan, String bandCode) {
        ArrayList<ChannelImpl> out = new ArrayList<ChannelImpl>();
        for (ChannelImpl channel : inChan) {
            if (ChannelIdUtil.getBandCode(channel.getId()).equals(bandCode)) {
                out.add(channel);
            }
        }
        return out;
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

    public  Channel[] getChannels(Channel[] staChans, String bc) {
        List results = new ArrayList();
        for (int i = 0; i < orientationCodeHeuristic.length; i++) {
            String orientation = orientationCodeHeuristic[i];
            Channel chan = getChannel(staChans, bc, orientation);
            if (chan != null) {
                results.add(chan);
                for (int j = 0; j < orientationCodeHeuristic.length; j++) {
                    String subOrient = orientationCodeHeuristic[j];
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

    public Channel getChannel(Channel[] inChan, String bandCode,
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
    public Channel getChannel(Channel[] inChan, String bandCode,
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

    public static <T> List<T> asList(T...elems){
        return Arrays.asList( elems );
    }
    
    private static Logger logger = LoggerFactory.getLogger(BestChannelUtil.class.getName());

}
