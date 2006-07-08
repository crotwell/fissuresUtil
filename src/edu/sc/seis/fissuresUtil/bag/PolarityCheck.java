package edu.sc.seis.fissuresUtil.bag;

import edu.iris.Fissures.FissuresException;
import edu.iris.Fissures.Orientation;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.Sensitivity;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;

public class PolarityCheck {

	public static ChannelSeismogram check(Channel chan,
			LocalSeismogramImpl seis, Sensitivity sensitivity)
			throws FissuresException {
		if (sensitivity.sensitivity_factor < 0 || checkFlip(chan)) {
			if (sensitivity.sensitivity_factor < 0 && checkFlip(chan)) {
				// both sensitivity and channel are flipped so seis is same
				return new ChannelSeismogram(OrientationUtil.flip(chan), seis,
						new Sensitivity(-1 * sensitivity.sensitivity_factor,
								sensitivity.frequency));
			} else if (sensitivity.sensitivity_factor < 0) {
				return new ChannelSeismogram(chan, Arithmatic.mul(seis, -1),
						new Sensitivity(-1 * sensitivity.sensitivity_factor,
								sensitivity.frequency));
			} else {
				return new ChannelSeismogram(OrientationUtil.flip(chan),
						Arithmatic.mul(seis, -1), sensitivity);

			}
		}
		return new ChannelSeismogram(chan, seis, sensitivity);
	}

	public static boolean checkFlip(Channel chan) {
		return (chan.get_code().charAt(2) == 'Z' && checkFlip(OrientationUtil
				.getUp(), chan))
				|| (chan.get_code().charAt(2) == 'N' && checkFlip(
						OrientationUtil.getNorth(), chan))
				|| (chan.get_code().charAt(2) == 'E' && checkFlip(
						OrientationUtil.getEast(), chan));

	}

	public static boolean checkFlip(Orientation correct, Channel chan) {
		return OrientationUtil.angleBetween(correct, chan.an_orientation) >= 180 - tol;
	}

	private static double tol = 0.01;
}
