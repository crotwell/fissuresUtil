package edu.sc.seis.fissuresUtil.bag;

import com.oregondsp.signalProcessing.fft.RDFT;

public class OregonDspFFT {

    public static float[] forward(float[] realData) {
        RDFT rdft = new RDFT((int)Math.round(Math.log(realData.length) / Math.log(2)));
        float[] forward = new float[realData.length];
        rdft.evaluate(realData, forward);
        return forward;
    }

    /**
     * Performs the inverse fft operation of the realFFT call.
     */
    public static float[] inverse(float[] realData) {
        RDFT rdft = new RDFT((int)Math.round(Math.log(realData.length) / Math.log(2)));
        float[] inverse = new float[realData.length];
        rdft.evaluateInverse(realData, inverse);
        return inverse;
    }

    public static float[] correlate(float[] x, float[] y) {
        float[] xforward = new float[x.length];
        xforward = forward(x);
        float[] yforward = new float[y.length];
        yforward = forward(y);
        float[] ans = new float[x.length];
        // handle 0 and nyquist
        ans[0] = xforward[0] * yforward[0];
        ans[x.length / 2] = xforward[x.length / 2] * yforward[x.length / 2];
        float a, b, c, d;
        for (int j = 1; j < x.length / 2; j++) {
            a = xforward[j];
            b = xforward[x.length - j];
            c = yforward[j];
            d = yforward[x.length - j];
            ans[j] = a * c + b * d;
            ans[x.length - j] = -a * d + b * c;
        }
        ans = inverse(ans);
        return ans;
    }

    public static float[] convolve(float[] x, float[] y, float delta) {
        float[] xforward = new float[x.length];
        xforward = forward(x);
        float[] yforward = new float[y.length];
        yforward = forward(y);
        float[] ans = new float[x.length];
        // handle 0 and nyquist
        ans[0] = xforward[0] * yforward[0];
        ans[x.length / 2] = xforward[x.length / 2] * yforward[x.length / 2];
        float a, b, c, d;
        for (int j = 1; j < x.length / 2; j++) {
            a = xforward[j];
            b = xforward[x.length - j];
            c = yforward[j];
            d = yforward[x.length - j];
            ans[j] = a * c - b * d;
            ans[x.length - j] = a * d + b * c;
        }
        ans = inverse(ans);
        for (int i = 0; i < ans.length; i++) {
            ans[i] *= delta;
        }
        return ans;
    }
}
