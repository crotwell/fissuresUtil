package edu.sc.seis.fissuresUtil.psn;

import edu.sc.seis.fissuresUtil.sac.SacTimeSeries;
import java.io.DataInputStream;
import java.io.IOException;

/**
 * PSNSensorAmpAtoD.java
 *
 * @author Created by Philip Oliver-Paull
 */
public class PSNSensorAmpAtoD{
    private DataInputStream dis;

    /**the important stuff**/
    private double sensorOutputVoltage, amplifierGain, aToDInputVoltage;

    public PSNSensorAmpAtoD(DataInputStream data) throws IOException{
        dis = data;

        sensorOutputVoltage = SacTimeSeries.swapBytes(dis.readDouble());
        amplifierGain = SacTimeSeries.swapBytes(dis.readDouble());
        aToDInputVoltage = SacTimeSeries.swapBytes(dis.readDouble());
    }

    public double getSensorOutputVoltage() {
        return sensorOutputVoltage;
    }

    public double getAmplifierGain() {
        return amplifierGain;
    }

    public double getAToDInputVoltage() {
        return aToDInputVoltage;
    }

}

