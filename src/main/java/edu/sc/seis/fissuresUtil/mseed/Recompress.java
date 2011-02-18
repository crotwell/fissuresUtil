/**
 * Recompress.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.mseed;

import java.io.IOException;
import java.util.LinkedList;

import edu.iris.Fissures.FissuresException;
import edu.iris.Fissures.IfTimeSeries.EncodedData;
import edu.iris.Fissures.IfTimeSeries.TimeSeriesDataSel;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.iris.dmc.seedcodec.B1000Types;
import edu.iris.dmc.seedcodec.Codec;
import edu.iris.dmc.seedcodec.CodecException;
import edu.iris.dmc.seedcodec.DecompressedData;
import edu.iris.dmc.seedcodec.Steim1;
import edu.iris.dmc.seedcodec.SteimException;
import edu.iris.dmc.seedcodec.SteimFrameBlock;

public class Recompress {

    public static LocalSeismogramImpl steim1(LocalSeismogramImpl seis)
        throws SteimException, IOException, CodecException, IOException, FissuresException  {
        return steim1(seis, false);
    }

    public static LocalSeismogramImpl steim1(LocalSeismogramImpl seis, boolean preserveBlocking)
        throws SteimException, IOException, CodecException, FissuresException  {
        return steim1(seis, preserveBlocking, 63);
    }

    public static LocalSeismogramImpl steim1(LocalSeismogramImpl seis, boolean preserveBlocking, int maxFrames)
        throws SteimException, IOException, CodecException, FissuresException  {
        LinkedList allBlocks = new LinkedList();
        if (preserveBlocking && seis.is_encoded()) {
            // preserve existing edata blocking
            EncodedData[] data = seis.get_as_encoded();
            EncodedData[] recomp = steim1(data, preserveBlocking, maxFrames);
            TimeSeriesDataSel dataSel = new TimeSeriesDataSel();
            dataSel.encoded_values(recomp);
            LocalSeismogramImpl out = new LocalSeismogramImpl(seis, dataSel);
            return out;
        } else {
            int[] data = seis.get_as_longs();
            allBlocks.addAll(steim1(data, maxFrames));
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
    }

    public static EncodedData[] steim1(EncodedData[] data, boolean preserveBlocking, int maxFrames) throws CodecException, IOException {
        Codec codec = new Codec();
        LinkedList allBlocks = new LinkedList();
        for (int i = 0; i < data.length; i++) {
            DecompressedData decomp = codec.decompress(data[i].compression,
                                                       data[i].values,
                                                       data[i].num_points,
                                                       data[i].byte_order);

            LinkedList list = steim1(decomp.getAsInt(), maxFrames);
            allBlocks.addAll(list);
        }
        EncodedData[] edata = new EncodedData[allBlocks.size()];
        for (int i = 0; i < edata.length; i++) {
            SteimFrameBlock block = (SteimFrameBlock)allBlocks.get(i);
            edata[i] = new EncodedData((short)B1000Types.STEIM1,
                                       block.getEncodedData(),
                                       block.getNumSamples(),
                                       false);
        }
        return edata;
    }

    public static LinkedList steim1(int[] data) throws SteimException {
        return steim1(data, 63);
    }

    public static LinkedList steim1(int[] data, int maxFrames) throws SteimException {
        LinkedList allBlocks = new LinkedList();
        SteimFrameBlock block;
        block = Steim1.encode(data, maxFrames);
        allBlocks.addLast(block);
        while (block.getNumSamples() < data.length) {
            // not all data encoded, make a new block
            int[] tmpData = new int[data.length-block.getNumSamples()];
            System.arraycopy(data, block.getNumSamples(), tmpData, 0, tmpData.length);
            data = tmpData;
            block = Steim1.encode(data, maxFrames);
            allBlocks.addLast(block);
        }
        return allBlocks;
    }
}

