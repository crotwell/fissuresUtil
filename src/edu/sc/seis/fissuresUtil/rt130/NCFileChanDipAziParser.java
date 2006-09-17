package edu.sc.seis.fissuresUtil.rt130;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import edu.iris.Fissures.Orientation;

public class NCFileChanDipAziParser {

    public static ChannelNameAndOrientation[] parse(String chanDipAzi) {
        if(chanDipAzi.equals("default")) {
            return new ChannelNameAndOrientation[] {new ChannelNameAndOrientation('Z',
                                                                                  0,
                                                                                  -90),
                                                    new ChannelNameAndOrientation('N',
                                                                                  0,
                                                                                  0),
                                                    new ChannelNameAndOrientation('E',
                                                                                  90,
                                                                                  0)};
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

    public static Orientation[] parseOrientations(String orientationString) {
        Matcher m = orientation.matcher(orientationString);
        if(!m.matches()) {
            throw new IllegalArgumentException("The orientation string must be either 'default' or a channel orientation specification");
        }
        if(m.group(1) != null) {
            return new Orientation[] {new Orientation(0, -90),
                                      new Orientation(0, 0),
                                      new Orientation(90, 0)};
        }
        Orientation[] orientations = new Orientation[3];
        for(int i = 0; i < orientations.length; i++) {
            orientations[i] = new Orientation(Integer.parseInt(m.group(3 + i * 2)),
                                              Integer.parseInt(m.group(2 + i * 2)));
        }
        return orientations;
    }

    private static Pattern orientation = Pattern.compile("(default)|\\d/(-?\\d+)/(-?\\d+):\\d/(-?\\d+)/(-?\\d+):\\d/(-?\\d+)/(-?\\d+)");
}
