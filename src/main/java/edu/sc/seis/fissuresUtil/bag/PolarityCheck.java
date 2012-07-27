package edu.sc.seis.fissuresUtil.bag;

import edu.iris.Fissures.FissuresException;
import edu.iris.Fissures.IfNetwork.Sensitivity;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;

public class PolarityCheck {

	public static boolean check(ChannelImpl chan, LocalSeismogramImpl seis,
			Sensitivity sensitivity) throws FissuresException {
		return FlippedChannel.check(chan)
				&& NegativeSensitivity.check(sensitivity);
	}

	public static ChannelSeismogram correct(ChannelImpl chan,
			LocalSeismogramImpl seis, Sensitivity sensitivity)
			throws FissuresException {
		if (NegativeSensitivity.check(sensitivity)
				&& FlippedChannel.check(chan)) {
			// both sensitivity and channel are flipped so seis is same
			return new ChannelSeismogram(OrientationUtil.flip(chan), seis,
					new Sensitivity(-1 * sensitivity.sensitivity_factor,
							sensitivity.frequency));
		} else if (NegativeSensitivity.check(sensitivity)) {
			return NegativeSensitivity.correct(chan, seis, sensitivity);
		} else if (FlippedChannel.check(chan)) {
			return FlippedChannel.correct(chan, seis, sensitivity);
		}

		return new ChannelSeismogram(chan, seis, sensitivity);
	}
}
