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
import edu.iris.dmc.seedcodec.DecompressedData;
import edu.iris.dmc.seedcodec.Codec;
import edu.iris.dmc.seedcodec.CodecException;

public class Recompress {

    public static LocalSeismogramImpl steim1(LocalSeismogramImpl seis)
        throws SteimException, IOException, CodecException, IOException {
        return steim1(seis, false);
    }

    public static LocalSeismogramImpl steim1(LocalSeismogramImpl seis, boolean preserveBlocking)
        throws SteimException, IOException, CodecException {
        LinkedList allBlocks = new LinkedList();
        if (preserveBlocking && seis.is_encoded()) {
            // preserve existing edata blocking
            EncodedData[] data = seis.get_as_encoded();
            Codec codec = new Codec();
            for (int i = 0; i < data.length; i++) {
                DecompressedData decomp = codec.decompress(data[i].compression,
                                                           data[i].values,
                                                           data[i].num_points,
                                                           data[i].byte_order);


                allBlocks.addAll(steim1(decomp.getAsInt()));
            }
        } else {
            int[] data = seis.get_as_longs();
            allBlocks.addAll(steim1(data));
        }
        EncodedData[] edata = new EncodedData[allBlocks.size()];
        for (int i = 0; i < edata.length; i++) {
            SteimFrameBlock block = (SteimFrameBlock)allBlocks.get(i);
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

    public static LinkedList steim1(int[] data) throws SteimException {
        LinkedList allBlocks = new LinkedList();
        SteimFrameBlock block;
        block = Steim1.encode(data, 63);
        allBlocks.addLast(block);
        while (block.getNumSamples() < data.length) {
            // not all data encoded, make a new block
            int[] tmpData = new int[data.length-block.getNumSamples()];
            System.arraycopy(data, block.getNumSamples(), tmpData, 0, tmpData.length);
            data = tmpData;
            block = Steim1.encode(data, 63);
            allBlocks.addLast(block);
        }
        return allBlocks;
    }
}

