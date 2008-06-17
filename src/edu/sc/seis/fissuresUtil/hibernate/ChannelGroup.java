
package edu.sc.seis.fissuresUtil.hibernate;

import org.apache.log4j.Logger;

import edu.iris.Fissures.Location;
import edu.iris.Fissures.Orientation;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.network.SiteIdUtil;
import edu.sc.seis.fissuresUtil.bag.Rotate;
import edu.sc.seis.fissuresUtil.cache.EventUtil;

public class ChannelGroup {

    public ChannelGroup(ChannelImpl[] channels) {
        this.channels = channels;
    }
    
    protected ChannelGroup() {
    }

    protected int dbid;
    
    protected void setDbid(int i) {
        dbid = i;
    }
    
    protected int getDbid() {
        return dbid;
    }
    
    public ChannelImpl[] getChannels() {
        return channels;
    }

    public boolean contains(Channel c) {
        return getIndex(c) != -1;
    }

    /**
     * Finds the vertical channel. If no channel has a dip of -90 then null is
     * returned.
     */
    public ChannelImpl getVertical() {
        for(int i = 0; i < channels.length; i++) {
            if(channels[i].getOrientation().dip == -90) {
                return channels[i];
            }
        }
        return null;
    }

    /**
     * Finds the 2 horizontal channels.
     */
    public ChannelImpl[] getHorizontal() {
        int[] indices = getHorizontalIndices();
        ChannelImpl[] out = new ChannelImpl[indices.length];
        for(int i = 0; i < indices.length; i++) {
            out[i] = channels[indices[i]];
        }
        return out;
    }

    private int[] getHorizontalIndices() {
        int first = -1;
        for(int i = 0; i < channels.length; i++) {
            if(channels[i].getOrientation().dip == 0) {
                if(first == -1) {
                    first = i;
                } else {
                    return new int[] {first, i};
                }
            }
        }
        if(first == -1) {
            return new int[0];
        } else {
            return new int[] {first};
        }
    }

    /**
     * Gets the horizontals as X and Y, so that the second channel's azimmuth is
     * the first's + 90 degrees. If this is not possible, then a zero length
     * array is returned.
     */
    public ChannelImpl[] getHorizontalXY() {
        ChannelImpl[] out = getHorizontal();
        if(out.length != 2) {
            out = new ChannelImpl[0];
        } else if((out[0].getOrientation().azimuth + 90) % 360 == out[1].getOrientation().azimuth % 360) {
            // in right order
        } else if((out[1].getOrientation().azimuth + 90) % 360 == out[0].getOrientation().azimuth % 360) {
            ChannelImpl tmp = out[0];
            out[0] = out[1];
            out[1] = tmp;
        } else {
            out = new ChannelImpl[0];
        }
        return out;
    }

    /**
     * Gets the channel that corresponds to this channelId from the
     * ChannelGroup. The Event is needed in case this channel id comes from a
     * seismogram that has been rotated to GCP, ie it has R or T as its
     * orientation code.
     */
    public ChannelImpl getChannel(ChannelId chanId, EventAccessOperations event) {
        for(int i = 0; i < channels.length; i++) {
            if(ChannelIdUtil.areEqual(chanId, channels[i].get_id())) {
                return channels[i];
            }
        }
        if(SiteIdUtil.areSameSite(chanId, channels[0].get_id())
                && chanId.channel_code.substring(0, 2)
                        .equals(channels[0].get_code().substring(0, 2))) {
            if(chanId.channel_code.endsWith("R")) {
                return getRadial(event);
            } else if(chanId.channel_code.endsWith("T")) {
                return getTransverse(event);
            }
        }
        return null;
    }

    /**
     * replaces the horizontal components with their radial and transverse
     * versions in the ChannelGroup This should only be called if the
     * seismograms that are accompanying this ChannelGroup through the vector
     * process sequence have been rotated.
     */
    public void makeTransverseAndRadial(int transverseIndex,
                                        int radialIndex,
                                        EventAccessOperations event) {
        channels[radialIndex] = getRadial(event);
        channels[transverseIndex] = getTransverse(event);
    }

    public ChannelImpl getRadial(EventAccessOperations event) {
        return getRadial(EventUtil.extractOrigin(event).getLocation());
    }

    public ChannelImpl getRadial(Location eventLoc) {
        return new ChannelImpl(Rotate.replaceChannelOrientation(channels[0].get_id(),
                                                                "R"),
                               channels[0].getName() + "Radial",
                               new Orientation((float)Rotate.getRadialAzimuth(channels[0].getSite().getLocation(),
                                                                              eventLoc),
                                               0),
                               channels[0].getSamplingInfo(),
                               channels[0].getEffectiveTime(),
                               channels[0].getSite());
    }

    public ChannelImpl getTransverse(EventAccessOperations event) {
        return getTransverse(EventUtil.extractOrigin(event).getLocation());
    }

    public ChannelImpl getTransverse(Location eventLoc) {
        return new ChannelImpl(Rotate.replaceChannelOrientation(channels[0].get_id(),
                                                                "T"),
                               channels[0].getName() + "Transverse",
                               new Orientation((float)Rotate.getTransverseAzimuth(channels[0].getSite().getLocation(),
                                                                                  eventLoc),
                                               0),
                               channels[0].getSamplingInfo(),
                               channels[0].getEffectiveTime(),
                               channels[0].getSite());
    }

    private int getIndex(Channel chan) {
        for(int i = 0; i < channels.length; i++) {
            if(channels[i].equals(chan))
                return i;
        }
        // didn't find by object equals, check for ids
        for(int i = 0; i < channels.length; i++) {
            if(ChannelIdUtil.areEqual(channels[i].get_id(), chan.get_id())) {
                return i;
            }
        }
        return -1;
    }
    
    public Channel getChannel1() {
        return getChannels()[0];
    }
    
    public Channel getChannel2() {
        return getChannels()[1];
    }
    
    public Channel getChannel3() {
        return getChannels()[2];
    }
    
    private void setChannel(int index, ChannelImpl chan) {
        if (channels == null) {
         channels = new ChannelImpl[3];   
        }
        channels[index-1] = chan;
    }
    
    protected void setChannel1(ChannelImpl chan) {
        setChannel(0, chan);
    }
    
    protected void setChannel2(ChannelImpl chan) {
        setChannel(1, chan);
    }
    
    protected void setChannel3(ChannelImpl chan) {
        setChannel(2, chan);
    }

    private ChannelImpl[] channels;

    private static final Logger logger = Logger.getLogger(ChannelGroup.class);
}