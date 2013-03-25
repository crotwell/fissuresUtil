package edu.sc.seis.fissuresUtil.bag;

import edu.iris.Fissures.IfTimeSeries.TimeSeriesDataSel;
import edu.iris.Fissures.model.SamplingImpl;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;


public class Upsample implements LocalSeismogramFunction {

    public Upsample(int factor) {
        this.factor = factor;
    }

    public LocalSeismogramImpl apply(LocalSeismogramImpl seis) throws Exception {
        LocalSeismogramImpl outSeis;
        TimeSeriesDataSel outData = new TimeSeriesDataSel();
        int numPts = (int)Math.ceil(1.0f*seis.num_points * factor);
        if(seis.can_convert_to_short()) {
            short[] inS = seis.get_as_shorts();
            short[] outS = new short[numPts];
            for(int i = 0; i < outS.length; i++) {
                outS[i * factor] = inS[i];
            }
            outData.sht_values(outS);
        } else if(seis.can_convert_to_long()) {
            int[] outI = new int[numPts];
            int[] inI = seis.get_as_longs();
            for(int i = 0; i < outI.length; i++) {
                outI[i * factor] = inI[i];
            }
            outData.int_values(outI);
        } else if(seis.can_convert_to_float()) {
            float[] outF = new float[numPts];
            float[] inF = seis.get_as_floats();
            for(int i = 0; i < outF.length; i++) {
                outF[i * factor] = inF[i];
            }
            outData.flt_values(outF);
        } else {
            double[] outD = new double[numPts];
            double[] inD = seis.get_as_doubles();
            for(int i = 0; i < outD.length; i++) {
                outD[i * factor] = inD[i];
            }
            outData.dbl_values(outD);
        } // end of else
        outSeis = new LocalSeismogramImpl(seis.get_id(),
                                          seis.properties,
                                          seis.begin_time,
                                          numPts,
                                          new SamplingImpl(seis.getSampling().getNumPoints()*factor,
                                                           (TimeInterval)seis.getSampling().getTimeInterval()),
                                          seis.y_unit,
                                          seis.channel_id,
                                          seis.parm_ids,
                                          seis.time_corrections,
                                          seis.sample_rate_history,
                                          outData);
        return outSeis;
    }

    private int factor;

    public int getFactor() {
        return factor;
    }
}
