package edu.sc.seis.fissuresUtil.rt130.packetTypes;

import java.io.DataInput;
import java.io.IOException;
import java.util.StringTokenizer;

import edu.sc.seis.fissuresUtil.rt130.PacketType;
import edu.sc.seis.fissuresUtil.rt130.RT130FormatException;

/**
 * @author fenner Created on Jun 14, 2005
 */
public class StateOfHealthPacket extends PacketType {

    public StateOfHealthPacket(StateOfHealthPacket original) {}

    public StateOfHealthPacket(DataInput in) throws IOException,
            RT130FormatException {
        this.read(in);
    }

    private void read(DataInput in) throws IOException, RT130FormatException {
        String entirePacket = new String(this.readBytes(in, 1008));
        // Skip first GPS_MARKER
        int i = entirePacket.indexOf(GPS_MARKER);
        if(i > 0) {
            // Grab the next 35 characters.
            // One or two extra characters are processed to allow
            // for two or three digit lat/long degree values.
            CharSequence locationCharSeq = entirePacket.subSequence(i
                    + GPS_MARKER.length(), i + GPS_MARKER.length() + 35);
            String locationString = locationCharSeq.toString();
            StringTokenizer st = new StringTokenizer(locationString);
            String latitudeString = st.nextToken();
            String longitudeString = st.nextToken();
            String elevationString = st.nextToken();
            char latDirection = latitudeString.charAt(0);
            latitudeString = latitudeString.subSequence(1,
                                                        latitudeString.length())
                    .toString();
            int signCorrection = 1;
            if(latDirection == 'N') {
                signCorrection = 1;
            } else if(latDirection == 'S') {
                signCorrection = -1;
            } else {
                throw new RT130FormatException();
            }
            st = new StringTokenizer(latitudeString, ":");
            float latDeg = Float.valueOf(st.nextToken()).floatValue();
            float latMin = Float.valueOf(st.nextToken()).floatValue();
            float latSec = Float.valueOf(st.nextToken()).floatValue();
            this.latitude = (latDeg + (latMin / 60) + (latSec / 3600))
                    * signCorrection;
            char longDirection = longitudeString.charAt(0);
            longitudeString = longitudeString.subSequence(1,
                                                          longitudeString.length())
                    .toString();
            signCorrection = -1;
            if(longDirection == 'E') {
                signCorrection = 1;
            } else if(longDirection == 'W') {
                signCorrection = -1;
            } else {
                throw new RT130FormatException();
            }
            st = new StringTokenizer(longitudeString, ":");
            float longDeg = Float.valueOf(st.nextToken()).floatValue();
            float longMin = Float.valueOf(st.nextToken()).floatValue();
            float longSec = Float.valueOf(st.nextToken()).floatValue();
            this.longitude = (longDeg + (longMin / 60) + (longSec / 3600))
                    * signCorrection;
            char elevationDirection = elevationString.charAt(0);
            char elevationUnit = elevationString.charAt(elevationString.length() - 1);
            elevationString = elevationString.subSequence(1,
                                                          (elevationString.length() - 1))
                    .toString();
            signCorrection = 1;
            if(elevationDirection == '+') {
                signCorrection = 1;
            } else if(elevationDirection == '-') {
                signCorrection = -1;
            } else {
                throw new RT130FormatException();
            }
            if(elevationUnit == 'M') {
                double elevationDouble = Double.valueOf(elevationString).doubleValue();
                this.elevation = elevationDouble * signCorrection;
            } else {
                throw new RT130FormatException("Unit for elevation from SOH file expected as M, got "
                        + elevationUnit);
            }
        }
    }

    private byte[] readBytes(DataInput in, int numBytes) throws IOException {
        byte[] seqBytes = new byte[numBytes];
        in.readFully(seqBytes);
        return seqBytes;
    }

    public float latitude, longitude;
    
    public double elevation;

    private final String GPS_MARKER = "GPS: POSITION: ";
}
