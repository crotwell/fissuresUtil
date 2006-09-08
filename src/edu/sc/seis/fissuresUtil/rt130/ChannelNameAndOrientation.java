package edu.sc.seis.fissuresUtil.rt130;

import edu.iris.Fissures.Orientation;


public class ChannelNameAndOrientation {
    
    
    public ChannelNameAndOrientation(char name, int azi , int dip){
        channelName = name;
        orientation = new Orientation(azi, dip);
    }
    
    public ChannelNameAndOrientation(char name, Orientation orientation){
        channelName = name;
        this.orientation = orientation;
    }
    
    public char getChannelName(){
        return channelName;
    }
    
    public Orientation getOrientation(){
        return orientation;
    }
    
    char channelName;
    
    Orientation orientation;
}
