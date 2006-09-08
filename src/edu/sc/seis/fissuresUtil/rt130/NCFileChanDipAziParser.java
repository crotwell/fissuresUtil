package edu.sc.seis.fissuresUtil.rt130;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class NCFileChanDipAziParser {

    public static ChannelNameAndOrientation[] parse(String chanDipAzi) {
        if(chanDipAzi.equals("default")) {
            ChannelNameAndOrientation[] array = new ChannelNameAndOrientation[3];
            array[0] = new ChannelNameAndOrientation('Z', 0, -90);
            array[1] = new ChannelNameAndOrientation('N', 0, 0);
            array[2] = new ChannelNameAndOrientation('E', 90, 0);
            return array;
        } else {
            List tokens = new ArrayList();
            StringTokenizer t = new StringTokenizer(chanDipAzi, "/:");
            while(t.hasMoreTokens()) {
                tokens.add(t.nextToken());
            }
            ChannelNameAndOrientation[] array = new ChannelNameAndOrientation[3];
            array[0] = new ChannelNameAndOrientation(((String)tokens.get(0)).charAt(0),
                                                     Integer.parseInt((String)tokens.get(2)),
                                                     Integer.parseInt((String)tokens.get(1)));
            array[1] = new ChannelNameAndOrientation(((String)tokens.get(3)).charAt(0),
                                                     Integer.parseInt((String)tokens.get(5)),
                                                     Integer.parseInt((String)tokens.get(4)));
            array[2] = new ChannelNameAndOrientation(((String)tokens.get(6)).charAt(0),
                                                     Integer.parseInt((String)tokens.get(8)),
                                                     Integer.parseInt((String)tokens.get(7)));
            return array;
        }
    }
}
