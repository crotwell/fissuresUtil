package edu.sc.seis.fissuresUtil.mseed;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import junit.framework.TestCase;
import edu.iris.Fissures.FissuresException;
import edu.iris.Fissures.model.ISOTime;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.SamplingImpl;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.iris.dmc.seedcodec.CodecException;
import edu.sc.seis.fissuresUtil.display.SimplePlotUtil;
import edu.sc.seis.fissuresUtil.hibernate.PlottableChunk;
import edu.sc.seis.fissuresUtil.mockFissures.IfSeismogramDC.MockSeismogram;
import edu.sc.seis.fissuresUtil.time.ReduceTool;
import edu.sc.seis.seisFile.mseed.Btime;
import edu.sc.seis.seisFile.mseed.DataRecord;
import edu.sc.seis.seisFile.mseed.SeedFormatException;
import edu.sc.seis.seisFile.mseed.SeedRecord;

public class TestFissuresConvert extends TestCase {

    public void testSamplingToMSeed() {
        TimeInterval sec = new TimeInterval(1, UnitImpl.SECOND);
        SamplingImpl samp = new SamplingImpl(1, sec);
        short[] out = FissuresConvert.calcSeedMultipilerFactor(samp);
        assertEquals(samp + " factor", (short)32766, out[0]);
        assertEquals(samp + " multi", (short)-32766, out[1]);
        samp = new SamplingImpl(1, (TimeInterval)sec.multiplyBy(1800));
        SamplingImpl[] sampleList = new SamplingImpl[] {new SamplingImpl(1,
                                                                         (TimeInterval)sec.multiplyBy(1800)),
                                                        new SamplingImpl(20,
                                                                         (TimeInterval)sec.multiplyBy(1)),
                                                        new SamplingImpl(1,
                                                                         (TimeInterval)sec.multiplyBy(0.05)),
                                                        new SamplingImpl(10,
                                                                         (TimeInterval)sec.multiplyBy(1)),
                                                        new SamplingImpl(1,
                                                                         (TimeInterval)sec.multiplyBy(0.05001)),
                                                        new SamplingImpl(100,
                                                                         (TimeInterval)sec.multiplyBy(1)),
                                                        new SamplingImpl(1,
                                                                         (TimeInterval)sec.multiplyBy(100))};
        for(int i = 0; i < sampleList.length; i++) {
            out = FissuresConvert.calcSeedMultipilerFactor(sampleList[i]);
            SamplingImpl converted = FissuresConvert.convertSampleRate(out[1],
                                                                       out[0]);
            assertEquals(sampleList[i] + " sampling converted=" + converted,
                         sampleList[i].getPeriod().getValue(UnitImpl.SECOND),
                         converted.getPeriod().getValue(UnitImpl.SECOND),
                         0.00001);
        }
        samp = new SamplingImpl(1, (TimeInterval)sec.multiplyBy(1800));
        out = FissuresConvert.calcSeedMultipilerFactor(samp);
        assertEquals(samp + " factor", (short)-32400, out[0]);
        assertEquals(samp + " multi", (short)18, out[1]);
        SamplingImpl converted = FissuresConvert.convertSampleRate(out[1], out[0]);
        assertEquals(samp + " sampling converted=" + converted,
                     samp.getPeriod().getValue(UnitImpl.SECOND),
                     converted.getPeriod().getValue(UnitImpl.SECOND),
                     0.00001);
    }
    
    public void testPlottableToMSeed() throws CodecException, IOException, SeedFormatException, FissuresException {
        LocalSeismogramImpl[] seis = new LocalSeismogramImpl[] {MockSeismogram.createDelta()};
        List<PlottableChunk> chunk = SimplePlotUtil.makePlottables(seis, 6000);
        List<DataRecord> drList = FissuresConvert.toMSeed(chunk);
        List<PlottableChunk> outChunk = FissuresConvert.toPlottable(drList);
        int numOut = 0;
        int numIn = 0;
        for (PlottableChunk c : chunk) {
            numIn += c.getData().y_coor.length;
        }
        for (PlottableChunk c : outChunk) {
            numOut+= c.getData().y_coor.length;
        }
        assertEquals("num plottable data", numIn, numOut);
        int[] inY = new int[numIn];
        int[] outY = new int[numOut];
        int i=0;
        for (PlottableChunk c : chunk) {
            for (int j = 0; j < c.getData().y_coor.length; j++) {
                inY[i] = c.getData().y_coor[j];
            }
        }
        i=0;
        for (PlottableChunk c : outChunk) {
            for (int j = 0; j < c.getData().y_coor.length; j++) {
                outY[i] = c.getData().y_coor[j];
                assertEquals(""+i, inY[i], outY[i]);
            }
        }
    }
    
    public void testCovertBtime() {
        Btime startStruct = new Btime(2011, 60, 1, 2, 3, 4567);
        MicroSecondDate d1 = FissuresConvert.getMicroSecondTime(startStruct);
        ISOTime iso = new ISOTime(startStruct.year,
                                  startStruct.jday,
                                  startStruct.hour,
                                  startStruct.min,
                                  startStruct.sec);
        MicroSecondDate d = iso.getDate().add(new TimeInterval(startStruct.tenthMilli, UnitImpl.TENTHMILLISECOND));
        assertEquals(d, d1);
    }
    
    public void testCovertBtime2007() {
        Btime startStruct = new Btime(2007, 227, 23, 59, 59, 1000);
        MicroSecondDate d1 = FissuresConvert.getMicroSecondTime(startStruct);
        ISOTime iso = new ISOTime(startStruct.year,
                                  startStruct.jday,
                                  startStruct.hour,
                                  startStruct.min,
                                  startStruct.sec);
        MicroSecondDate d = iso.getDate().add(new TimeInterval(startStruct.tenthMilli, UnitImpl.TENTHMILLISECOND));
        assertEquals(d, d1);
        Btime roundTrip = FissuresConvert.getBtime(d1);
        assertEquals("year", startStruct.year, roundTrip.year);
        assertEquals("jday", startStruct.jday, roundTrip.jday);
        assertEquals("hour", startStruct.hour, roundTrip.hour);
        assertEquals("min", startStruct.min, roundTrip.min);
        assertEquals("sec", startStruct.sec, roundTrip.sec);
        assertEquals("tenthMilli", startStruct.tenthMilli, roundTrip.tenthMilli);
    }
    
    @Test
    public void testBadLeapSecMSeed() throws SeedFormatException, IOException, FissuresException {
        String[] mseedFilenames = new String[] {
                                                "fdsnws-dataselect_2013-08-02T18_55_55.mseed"
          //                    "KZ.BRVK..BHZ_2007_227_23.mseed",
         //                     "KZ.BRVK..BHZ_2007_228_00.mseed"
        };
        List<DataRecord> drList = new ArrayList<DataRecord>();
        for (int i = 0; i < mseedFilenames.length; i++) {
        DataInputStream in = new DataInputStream(this.getClass()
                                                 .getClassLoader()
                                                 .getResourceAsStream("edu/sc/seis/fissuresUtil/mseed/"+mseedFilenames[i]));
        try {
            while(true) {
            DataRecord dr = (DataRecord)SeedRecord.read(in);
            System.out.println(dr.getHeader().getStartTime()+" "+dr.getHeader().getStartBtime().tenthMilli);
            drList.add(dr);
            }
        } catch(EOFException e) {}
        
        }
        List<LocalSeismogramImpl> seisList = FissuresConvert.toFissures(drList);
        for (LocalSeismogramImpl seis : seisList) {
            System.out.println(seis.getBeginTime()+" "+seis.getEndTime());
        }
        LocalSeismogramImpl[] merged = ReduceTool.merge(seisList.toArray(new LocalSeismogramImpl[0]));
        for (int i = 0; i < merged.length; i++) {
            System.out.println("merge "+merged[i].getBeginTime()+" "+merged[i].getEndTime());
        }
    }

    @Test
    public void testFromIris() throws Exception {
        
    }

}
