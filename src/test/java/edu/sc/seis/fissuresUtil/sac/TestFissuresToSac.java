package edu.sc.seis.fissuresUtil.sac;


import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;

import edu.iris.Fissures.event.OriginImpl;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.sc.seis.fissuresUtil.mockFissures.IfEvent.MockOrigin;
import edu.sc.seis.seisFile.sac.Complex;
import edu.sc.seis.seisFile.sac.SacConstants;
import edu.sc.seis.seisFile.sac.SacHeader;
import edu.sc.seis.seisFile.sac.SacPoleZero;
import edu.sc.seis.seisFile.sac.SacTimeSeries;

public class TestFissuresToSac  {

    @Test
    public void testAddOrigin() {
        int npts = 1000;
        float delta = 0.025f;
        float[] y = new float[npts];
        float b = 10.0f;
        SacHeader header = SacHeader.createEmptyEvenSampledTimeSeriesHeader();
        header.setIztype( SacConstants.IB);
        header.setB(0);
        header.setDelta( delta);
        OriginImpl mockOrigin = MockOrigin.create();
        MicroSecondDate otime = mockOrigin.getTime();
        FissuresToSac.setKZTime(header, otime.add(new TimeInterval(b, UnitImpl.SECOND)));
        FissuresToSac.addOrigin(header, mockOrigin);
        SacTimeSeries sac = new SacTimeSeries(header, y);
        assertEquals(b, sac.getHeader().getB(), 0.000001f);
        assertEquals(delta, sac.getHeader().getDelta(), 0.000001f);
        assertEquals(b+(npts-1)*delta, sac.getHeader().getE(), 0.000001f);

    }

}
