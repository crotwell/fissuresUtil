/**
 * SacGrabber.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.simple;

import edu.iris.Fissures.FissuresException;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfNetwork.NetworkId;
import edu.iris.Fissures.IfSeismogramDC.DataCenterOperations;
import edu.iris.Fissures.IfSeismogramDC.LocalSeismogram;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.model.ISOTime;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.iris.dmc.seedcodec.CodecException;
import edu.sc.seis.fissuresUtil.cache.BulletproofVestFactory;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.fissuresUtil.sac.FissuresToSac;
import edu.sc.seis.fissuresUtil.sac.SacTimeSeries;
import java.io.File;
import java.io.IOException;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.InvalidName;
import org.omg.CosNaming.NamingContextPackage.NotFound;

public class SacGrabber {

    public SacGrabber() throws org.omg.CORBA.ORBPackage.InvalidName, CannotProceed, InvalidName, NotFound, InvalidName, CannotProceed, org.omg.CosNaming.NamingContextPackage.InvalidName, NotFound {
        seisDC = new DataCenterOperations[2];
        seisDC[0] = BulletproofVestFactory.vestSeismogramDC("edu/iris/dmc",
                                                            "IRIS_BudDataCenter",
                                                            Initializer.getNS());
        seisDC[1] = BulletproofVestFactory.vestSeismogramDC("edu/iris/dmc",
                                                            "IRIS_PondDataCenter",
                                                            Initializer.getNS());
    }

    public int grab(String net, String station, String site, String chan, String beginTime, int seconds) throws FissuresException, CodecException, IOException {
        RequestFilter[] rf = new RequestFilter[1];
        MicroSecondDate bTime = new ISOTime(beginTime).getDate();
        ChannelId channelId = new ChannelId(new NetworkId(net, bTime.getFissuresTime()), station, site, chan, bTime.getFissuresTime());
        rf[0] = new RequestFilter(channelId,
                                  bTime.getFissuresTime(),
                                  bTime.add(new TimeInterval(seconds, UnitImpl.SECOND)).getFissuresTime());
        LocalSeismogram[] seis = new LocalSeismogram[0];
        int seisNum = 0;
        while(seis.length == 0 && seisNum < seisDC.length) {
            seis = seisDC[seisNum].retrieve_seismograms(rf);
            for (int i = 0; i < seis.length; i++) {
                SacTimeSeries sac = FissuresToSac.getSAC((LocalSeismogramImpl)seis[i]);
                File out = new File(ChannelIdUtil.toStringNoDates(channelId));
                int fNum = 1;
                while (out.exists()) {
                    fNum++;
                    out = new File(ChannelIdUtil.toStringNoDates(channelId)+"."+fNum);
                }
                sac.write(out);
            }
            seisNum++;
        }
        return seis.length;
    }

    /**
     *
     */
    public static void main(String[] args) {
        try {
            SacGrabber grabber = new SacGrabber();
            if (args.length == 6) {
                grabber.grab(args[0], args[1], args[2], args[3], args[4], Integer.parseInt(args[5]));
            }
        } catch (Exception e) {
            GlobalExceptionHandler.handle("Problem getting seismogram. ", e);
        }
    }

    DataCenterOperations[] seisDC;
}

