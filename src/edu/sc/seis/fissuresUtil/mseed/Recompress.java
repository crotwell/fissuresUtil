/**
 * Recompress.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.mseed;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.iris.dmc.seedcodec.SteimFrameBlock;
import edu.iris.dmc.seedcodec.Steim1;
import edu.iris.dmc.seedcodec.SteimException;
import java.util.LinkedList;
import edu.iris.Fissures.IfTimeSeries.EncodedData;
import edu.iris.dmc.seedcodec.B1000Types;
import java.io.IOException;
import edu.iris.Fissures.IfTimeSeries.TimeSeriesDataSel;



public class Recompress
{

    public static LocalSeismogramImpl steim1(LocalSeismogramImpl seis) throws SteimException, IOException {
        int[] data = seis.get_as_longs();
        LinkedList allBlocks = new LinkedList();
        SteimFrameBlock block = Steim1.encode(data, 63);
        allBlocks.addLast(block);
        while (block.getNumSamples() < data.length) {
            // not all data encoded, make a new block
            int[] tmpData = new int[data.length-block.getNumSamples()];
            System.arraycopy(data, block.getNumSamples(), tmpData, 0, tmpData.length);
            data = tmpData;
            block = Steim1.encode(data, 63);
            allBlocks.addLast(block);
        }
        EncodedData[] edata = new EncodedData[allBlocks.size()];
        for (int i = 0; i < edata.length; i++) {
            block = (SteimFrameBlock)allBlocks.get(i);
            edata[i] = new EncodedData((short)B1000Types.STEIM1,
                                       block.getEncodedData(),
                                       block.getNumSamples(),
                                       false);
        }
        TimeSeriesDataSel dataSel = new TimeSeriesDataSel();
        dataSel.encoded_values(edata);
        LocalSeismogramImpl out = new LocalSeismogramImpl(seis, dataSel);
        return out;
    }
}

