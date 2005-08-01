package edu.sc.seis.fissuresUtil.bag;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.freq.Cmplx;
import edu.sc.seis.fissuresUtil.sac.SacPoleZero;
import edu.sc.seis.fissuresUtil.sac.SacToFissures;
import edu.sc.seis.seisFile.sac.SacTimeSeries;
import junit.framework.TestCase;
import junitx.framework.ArrayAssert;

/**
 * @author crotwell Created on Jul 27, 2005
 */
public class TransferTest extends TestCase {

    public TransferTest(String name) {
        super(name);
    }

    public void testTaper() throws Exception {
        assertEquals(Transfer.freqTaper(0, 1, 2, 3, 4), 0, 0.00001);
        assertEquals(Transfer.freqTaper(1, 1, 2, 3, 4), 0, 0.00001);
        assertEquals(Transfer.freqTaper(2, 1, 2, 3, 4), 1, 0.00001);
        assertEquals(Transfer.freqTaper(1.5, 1, 2, 3, 4), .5, 0.00001);
        assertEquals(Transfer.freqTaper(3, 1, 2, 3, 4), 1, 0.00001);
        assertEquals(Transfer.freqTaper(4, 1, 2, 3, 4), 0, 0.00001);
        assertEquals(Transfer.freqTaper(5, 1, 2, 3, 4), 0, 0.00001);
    }
    
    public void testTaperVsSac() throws Exception {
        double[][] sacout = { { 0, 0.000610352, 0 },
                              { 0.000610352, 0.000610352, 0 },
                              { 0.0012207, 0.000610352, 0 },
                              { 0.00183105, 0.000610352, 0 },
                              { 0.00244141, 0.000610352, 0 },
                              { 0.00305176, 0.000610352, 0 },
                              { 0.00366211, 0.000610352, 0 },
                              { 0.00427246, 0.000610352, 0 },
                              { 0.00488281, 0.000610352, 0 },
                              { 0.00549316, 0.000610352, 1.4534e-05 },
                              { 0.00610352, 0.000610352, 7.04641e-05 },
                              { 0.00671387, 0.000610352, 0.000160492 },
                              { 0.00732422, 0.000610352, 0.000271539 },
                              { 0.00793457, 0.000610352, 0.000387472 },
                              { 0.00854492, 0.000610352, 0.00049145 },
                              { 0.00915527, 0.000610352, 0.000568367 },
                              { 0.00976562, 0.000610352, 0.000607048 },
                              { 0.010376, 0.000610352, 0.000610352 },
                              { 0.0109863, 0.000610352, 0.000610352 },
                              { 0.0115967, 0.000610352, 0.000610352 } };
        for(int i = 0; i < sacout.length; i++) {
            assertEquals("taper "+i, sacout[i][2], sacout[0][1]*Transfer.freqTaper(sacout[i][0], .005f, .01f, 1e5f, 1e6f), 0.00001);
            
        }
    }

    public void testEvalPoleZero() {
        // array is freq, real, imag from sac genran.c
        double[][] sacout = new double[][] { {0, 0, 0},
                                            {0.000610352, -63356.3, -165446},
                                            {0.0012207, -897853, -986468},
                                            {0.00183105,
                                             -3.56033e+06,
                                             -1.78889e+06},
                                            {0.00244141,
                                             -7.78585e+06,
                                             -1.05324e+06},
                                            {0.00305176,
                                             -1.21507e+07,
                                             1.70486e+06},
                                            {0.00366211,
                                             -1.56945e+07,
                                             5.80921e+06},
                                            {0.00427246,
                                             -1.82753e+07,
                                             1.04878e+07},
                                            {0.00488281,
                                             -2.00925e+07,
                                             1.52973e+07},
                                            {0.00549316,
                                             -2.13745e+07,
                                             2.00495e+07},
                                            {0.00610352,
                                             -2.22939e+07,
                                             2.46831e+07},
                                            {0.00671387,
                                             -2.29672e+07,
                                             2.91893e+07},
                                            {0.00732422,
                                             -2.34707e+07,
                                             3.35782e+07},
                                            {0.00793457,
                                             -2.38545e+07,
                                             3.78652e+07},
                                            {0.00854492,
                                             -2.4152e+07,
                                             4.20655e+07},
                                            {0.00915527,
                                             -2.43859e+07,
                                             4.61927e+07},
                                            {5.04028, 1.48193e+10, 1.47267e+10},
                                            {5.04089, 1.48211e+10, 1.47267e+10},
                                            {5.0415, 1.48229e+10, 1.47267e+10},
                                            {5.04211, 1.48246e+10, 1.47267e+10},
                                            {5.04272, 1.48264e+10, 1.47267e+10},
                                            {5.04333, 1.48282e+10, 1.47266e+10},
                                            {5.04395, 1.483e+10, 1.47266e+10},
                                            {5.04456, 1.48318e+10, 1.47266e+10},
                                            {5.04517, 1.48336e+10, 1.47266e+10},
                                            {5.04578, 1.48353e+10, 1.47266e+10},
                                            {5.04639, 1.48371e+10, 1.47266e+10},
                                            {5.047, 1.48389e+10, 1.47266e+10},
                                            {5.04761, 1.48407e+10, 1.47265e+10},
                                            {5.04822, 1.48425e+10, 1.47265e+10},
                                            {5.04883, 1.48442e+10, 1.47265e+10},
                                            {5.04944, 1.4846e+10, 1.47265e+10},
                                            {5.05005, 1.48478e+10, 1.47265e+10},
                                            {9.99084, 2.35288e+10, 1.17883e+10},
                                            {9.99145, 2.35294e+10, 1.17878e+10},
                                            {9.99207, 2.353e+10, 1.17874e+10},
                                            {9.99268, 2.35306e+10, 1.1787e+10},
                                            {9.99329, 2.35311e+10, 1.17865e+10},
                                            {9.9939, 2.35317e+10, 1.17861e+10},
                                            {9.99451, 2.35323e+10, 1.17857e+10},
                                            {9.99512, 2.35329e+10, 1.17852e+10},
                                            {9.99573, 2.35334e+10, 1.17848e+10},
                                            {9.99634, 2.3534e+10, 1.17844e+10},
                                            {9.99695, 2.35346e+10, 1.17839e+10},
                                            {9.99756, 2.35352e+10, 1.17835e+10},
                                            {9.99817, 2.35357e+10, 1.17831e+10},
                                            {9.99878, 2.35363e+10, 1.17826e+10},
                                            {9.99939, 2.35369e+10, 1.17822e+10},
                                            {10, 2.35375e+10, 1.17818e+10}};
        // IU.HRV.BHE response
        Cmplx[] zeros = new Cmplx[] {new Cmplx(0, 0),
                                     new Cmplx(0, 0),
                                     new Cmplx(0, 0)};
        Cmplx[] poles = new Cmplx[] {new Cmplx(-0.0139, 0.0100),
                                     new Cmplx(-0.0139, -0.0100),
                                     new Cmplx(-31.4160, 0.0000)};
        SacPoleZero pz = new SacPoleZero(poles, zeros, 2.94283674E10f);
        for(int i = 1; i < sacout.length; i++) {
            Cmplx dhi = Transfer.evalPoleZeroInverse(pz, (float)sacout[i][0]);
            dhi = Cmplx.div(new Cmplx(1, 0), dhi);
            assertEquals("real " + i, 1, sacout[i][1] / dhi.real(), 0.00001);
            // sac fft is opposite sign in imag, so want ratio to be -1
            assertEquals("imag " + i, -1, sacout[i][2] / dhi.imag(), 0.00001);
        }
    }

    public void testPoleZeroTaper() throws Exception {
        SacTimeSeries sac = new SacTimeSeries();
        sac.read(new DataInputStream(new BufferedInputStream(this.getClass()
                .getClassLoader()
                .getResourceAsStream("edu/sc/seis/fissuresUtil/bag/IU.HRV.__.BHE.SAC.0"))));
        LocalSeismogramImpl seis = SacToFissures.getSeismogram(sac);
        float[] data = seis.get_as_floats();
        double samprate = seis.getSampling().getFrequency().getValue(UnitImpl.HERTZ);
        for(int i = 0; i < data.length; i++) {
            data[i] /= samprate;
        }
        Cmplx[] out = Cmplx.fft(data);
        SacPoleZero poleZero = SacPoleZero.read(new BufferedReader(new InputStreamReader(this.getClass()
                .getClassLoader()
                .getResourceAsStream("edu/sc/seis/fissuresUtil/bag/hrv.bhe.sacpz"))));
        double[][] sacout = {  {0, 0, 0},
                               {0.000610352, -0, 0},
                               {0.0012207, -0, 0},
                               {0.00183105, -0, 0},
                               {0.00244141, -0, 0},
                               {0.00305176, -0, -0},
                               {0.00366211, -0, -0},
                               {0.00427246, -0, -0},
                               {0.00488281, -0, -0},
                               {0.00549316, -3.61712e-13, -3.39289e-13},
                               {0.00610352, -1.42001e-12, -1.57219e-12},
                               {0.00671387, -2.67201e-12, -3.39588e-12},
                               {0.00732422, -3.79726e-12, -5.43252e-12},
                               {0.00793457, -4.615e-12, -7.32556e-12},
                               {0.00854492, -5.04479e-12, -8.7865e-12},
                               {0.00915527, -5.07988e-12, -9.62251e-12},
                               {0.00976562, -4.7661e-12, -9.74839e-12},
                               {0.010376, -4.24248e-12, -9.31375e-12},
                               {0.0109863, -3.78195e-12, -8.86666e-12},
                               {0.0115967, -3.3922e-12, -8.4564e-12}};
        float lowCut = .005f;
        float lowPass = 0.01f;
        float highPass = 1e5f;
        float highCut = 1e6f;
        double deltaF = samprate / out.length;
        double freq;
        Cmplx respAtS;
        for(int i = 0; i < sacout.length; i++) {
            freq = i * deltaF;
            assertEquals("deltaF "+i, sacout[i][0], freq, 0.00001);
            respAtS = Transfer.evalPoleZeroInverse(poleZero, freq);
            respAtS = Cmplx.mul(respAtS, deltaF*Transfer.freqTaper(freq,
                                                   lowCut,
                                                   lowPass,
                                                   highPass,
                                                   highCut));

            if(sacout[i][0] == 0 || respAtS.real() == 0) {
                assertEquals("real " + i + " " + respAtS.real()+"   "+sacout[i][1],
                             sacout[i][1],
                             respAtS.real() ,
                             0.00001);
            } else {
                assertEquals("real " + i + " " + respAtS.real()+"   "+sacout[i][1], 1, sacout[i][1]
                        / respAtS.real(), 0.00001);
            }
            if(sacout[i][1] == 0 || respAtS.imag() == 0) {
                assertEquals("imag " + i + " " + respAtS.imag(),
                             sacout[i][2],
                             respAtS.imag() ,
                             0.00001);
            } else {
                assertEquals("imag " + i + " " + respAtS.imag(),
                             -1,
                             sacout[i][2] / respAtS.imag() ,
                             0.00001);
            }
            
        }
    }

    public void testFFT() throws Exception {
        SacTimeSeries sac = new SacTimeSeries();
        sac.read(new DataInputStream(new BufferedInputStream(this.getClass()
                .getClassLoader()
                .getResourceAsStream("edu/sc/seis/fissuresUtil/bag/IU.HRV.__.BHE.SAC.0"))));
        LocalSeismogramImpl seis = SacToFissures.getSeismogram(sac);
        double samprate = seis.getSampling()
        .getFrequency()
        .getValue(UnitImpl.HERTZ);
        float[] data = seis.get_as_floats();
        /* sac premultiplies the data by the sample period before doing the fft. Later it
         * seems to be cancled out by premultiplying the pole zeros by a similar factor.
         * I don't understand why they do this, but am reporducing it in order to be 
         * compatible.
         */
        for(int i = 0; i < data.length; i++) {
            data[i] /= (float)samprate;
        }
        Cmplx[] out = Cmplx.fft(data);
        double[][] sacout = new double[][] { {695917, 0},
                                            {-34640.4, 7593.43},
                                            {-28626.7, -34529.8},
                                            {-28644.3, -18493.2},
                                            {-17856.8, -14744.9},
                                            {-26180.4, -13016},
                                            {-35773.7, -28250.8},
                                            {-3204.24, -39020.9},
                                            {-6523.97, -9036.16},
                                            {-9328.12, -28816.7},
                                            {-4191.56, -4618.8},
                                            {-25816.1, -37862.5},
                                            {24457.3, -40734.5},
                                            {33569.6, 6327.69},
                                            {-35207.2, 24178.2},
                                            {-16313.6, -81431.5},
                                            {77269.7, -3612.97},
                                            {-5407.14, 32410.2},
                                            {-11010.8, 4728.02},
                                            {-15558.3, -24774.9}};
        assertEquals("real " + 0 + " " + out[0].real(), 1, sacout[0][0]
                / out[0].real() , 0.00001);
        assertEquals("imag " + 0 + " " + out[0].imag(),
                     sacout[0][1],
                     -out[0].imag() ,
                     0.00001);
        for(int i = 1; i < sacout.length; i++) {
            assertEquals("real " + i + " " + out[i].real(), 1, sacout[i][0]
                    / out[i].real(), 0.00001);
            // sac fft is opposite sign imag, so ratio is -1
            assertEquals("imag " + i + " " + out[i].imag(), -1, sacout[i][1]
                    / out[i].imag(), 0.00001);
        }
    }

    public void testCombine() throws Exception {
        SacTimeSeries sac = new SacTimeSeries();
        sac.read(new DataInputStream(new BufferedInputStream(this.getClass()
                .getClassLoader()
                .getResourceAsStream("edu/sc/seis/fissuresUtil/bag/IU.HRV.__.BHE.SAC.0"))));
        LocalSeismogramImpl seis = SacToFissures.getSeismogram(sac);
        double samprate = seis.getSampling()
                .getFrequency()
                .getValue(UnitImpl.HERTZ);
        SacPoleZero pz = SacPoleZero.read(new BufferedReader(new InputStreamReader(this.getClass()
                .getClassLoader()
                .getResourceAsStream("edu/sc/seis/fissuresUtil/bag/hrv.bhe.sacpz"))));
        float[] data = seis.get_as_floats();
        for(int i = 0; i < data.length; i++) {
            data[i] /= samprate;
        }
        
        
        Cmplx[] out = Cmplx.fft(data);
        assertEquals("nfft", 32768, out.length);
        assertEquals("delfrq ", 0.000610352, samprate/out.length, 0.00001);
        out = Transfer.combine(out, samprate, pz, 0.005f, 0.01f, 1e5f, 1e6f);
        double[][] sacout = { {0, 0},
                             {0, -0},
                             {0, 0},
                             {0, 0},
                             {0, 0},
                             {0, 0},
                             {0, 0},
                             {0, 0},
                             {0, 0},
                             {-6.40312e-09, 1.35883e-08},
                             {-1.30956e-09, 1.31487e-08},
                             {-5.95957e-08, 1.88837e-07},
                             {-3.14161e-07, 2.18147e-08},
                             {-1.0857e-07, -2.75118e-07},
                             {3.90054e-07, 1.87374e-07},
                             {-7.00704e-07, 5.70641e-07},
                             {-4.03496e-07, -7.36036e-07},
                             {3.24801e-07, -8.71389e-08},
                             {8.35641e-08, 7.97482e-08},
                             {-1.5673e-07, 2.15609e-07}};

        for(int i = 0; i < sacout.length; i++) {
            if(sacout[i][0] == 0 || out[i].real() == 0) {
                assertEquals("real " + i + " " + out[i].real()+"  "+sacout[i][0],
                             sacout[i][0],
                             out[i].real() ,
                             0.00001);
            } else {
                assertEquals("real " + i + " " + out[i].real()+"  "+sacout[i][0], 1, sacout[i][0]
                        / out[i].real(), 0.00001);
            }
            if(sacout[i][1] == 0 || out[i].imag() == 0) {
                assertEquals("imag " + i + " " + out[i].imag()+"  "+sacout[i][1],
                             sacout[i][1],
                             out[i].imag() ,
                             0.00001);
            } else {
                assertEquals("imag " + i + " " + out[i].imag()+"  "+sacout[i][1],
                             -1,
                             sacout[i][1] / out[i].imag(),
                             0.00001);
            }
        }
    }

    /*
     r IU.HRV.__.BHE.SAC.0 
     transfer from polezero subtype hrv.bhe.sacpz to none freqlimits 0.005 0.01 1e5 1e6
     */
    public void testHRV() throws Exception {
        SacTimeSeries sac = new SacTimeSeries();
        sac.read(new DataInputStream(new BufferedInputStream(this.getClass()
                .getClassLoader()
                .getResourceAsStream("edu/sc/seis/fissuresUtil/bag/transfer.sac"))));
        LocalSeismogramImpl sactfr = SacToFissures.getSeismogram(sac);
        sac.read(new DataInputStream(new BufferedInputStream(this.getClass()
                .getClassLoader()
                .getResourceAsStream("edu/sc/seis/fissuresUtil/bag/IU.HRV.__.BHE.SAC"))));
        LocalSeismogramImpl orig = SacToFissures.getSeismogram(sac);
        SacPoleZero pz = SacPoleZero.read(new BufferedReader(new InputStreamReader(this.getClass()
                .getClassLoader()
                .getResourceAsStream("edu/sc/seis/fissuresUtil/bag/hrv.bhe.sacpz"))));
        LocalSeismogramImpl bagtfr = new Transfer().apply(orig,
                                                          pz,
                                                          .005f,
                                                          0.01f,
                                                          1e5f,
                                                          1e6f);
        float[] sacdata = sactfr.get_as_floats();
        float[] dhidata = bagtfr.get_as_floats();
        ArrayAssert.assertEquals("data", sacdata, dhidata, 0.0001f);
    }
}