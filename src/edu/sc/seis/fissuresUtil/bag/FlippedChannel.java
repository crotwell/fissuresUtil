package edu.sc.seis.fissuresUtil.bag;

import edu.iris.Fissures.FissuresException;
import edu.iris.Fissures.Orientation;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.Sensitivity;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;

public class FlippedChannel {

	public static ChannelSeismogram correct(Channel chan,
			LocalSeismogramImpl seis, Sensitivity sensitivity)
			throws FissuresException {
		if (check(chan)) {
			return new ChannelSeismogram(OrientationUtil.flip(chan), Arithmatic
					.mul(seis, -1), sensitivity);
		}
		return new ChannelSeismogram(chan, seis, sensitivity);
	}

	public static boolean check(Channel chan) {
		return (chan.get_code().charAt(2) == 'Z' && check(OrientationUtil
				.getUp(), chan))
				|| (chan.get_code().charAt(2) == 'N' && check(OrientationUtil
						.getNorth(), chan))
				|| (chan.get_code().charAt(2) == 'E' && check(OrientationUtil
						.getEast(), chan));

	}

	public static boolean check(Orientation correct, Channel chan) {
		return OrientationUtil.angleBetween(correct, chan.an_orientation) >= 180 - tol;
	}

	private static double tol = 0.01;
}
