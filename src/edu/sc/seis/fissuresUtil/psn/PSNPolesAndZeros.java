package edu.sc.seis.fissuresUtil.psn;

import edu.sc.seis.fissuresUtil.sac.SacTimeSeries;
import java.io.DataInputStream;
import java.io.IOException;

/**
 * PSNPolesAndZeros.java
 *
 * @author Created by Philip Oliver-Paull
 */
public class PSNPolesAndZeros{
    private DataInputStream dis;

    /**The important stuff**/
    short numZeros, numPoles;
    double[][] zeros, poles;

    public PSNPolesAndZeros(DataInputStream data) throws IOException{
        dis = data;

        numZeros = SacTimeSeries.swapBytes((short)dis.readUnsignedShort());
        numPoles = SacTimeSeries.swapBytes((short)dis.readUnsignedShort());

        zeros = new double[numZeros][2];
        for (int i = 0; i < numZeros; i++) {
            for (int j = 0; j < 2; j++) {
                zeros[i][j] = SacTimeSeries.swapBytes(dis.readDouble());
            }
        }

        poles = new double[numPoles][2];
        for (int i = 0; i < numZeros; i++) {
            for (int j = 0; j < 2; j++) {
                poles[i][j] = SacTimeSeries.swapBytes(dis.readDouble());
            }
        }
    }

    public short getNumZeros() {
        return numZeros;
    }

    public short getNumPoles() {
        return numPoles;
    }

    public double[][] getZeros() {
        return zeros;
    }

    public double[][] getPoles() {
        return poles;
    }
}

