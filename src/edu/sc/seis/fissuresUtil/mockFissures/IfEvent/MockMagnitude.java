package edu.sc.seis.fissuresUtil.mockFissures.IfEvent;

import edu.iris.Fissures.IfEvent.Magnitude;

public class MockMagnitude{
    
    public static final Magnitude[][] getMagnitudes(float minMag, float maxMag, int count){
        Magnitude[][] magnitudes = new Magnitude[count][];
        float diff = (maxMag-minMag)/(float)count;
        float curMag = minMag;
        for(int i = 0; i < count; i++, curMag+=diff) {
            float curMag2 = curMag+0.2f;
            float curMag3 = curMag-0.3f;
            magnitudes[i] = new Magnitude[]{ new Magnitude("type"+curMag, curMag, "contributor"+curMag),
                        new Magnitude("type"+curMag2, curMag2, "contributor"+curMag2),
                        new Magnitude("type"+curMag3, curMag3, "contributor"+curMag3) };
        }
        return magnitudes;
    }
    
    public static final Magnitude[] MAGS = {
        new Magnitude("type5",5,"contributor5"),
            new Magnitude("type4",4,"contributor4"),
            new Magnitude("type6",6,"contributor6")};
}
