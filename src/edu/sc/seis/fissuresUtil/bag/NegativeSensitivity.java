package edu.sc.seis.fissuresUtil.bag;

import edu.iris.Fissures.FissuresException;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.Sensitivity;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;

public class NegativeSensitivity {

	public static boolean check(Sensitivity sensitivity) {
		return sensitivity.sensitivity_factor < 0;
	}

	public static ChannelSeismogram correct(Channel chan,
			LocalSeismogramImpl seis, Sensitivity sensitivity)
			throws FissuresException {
		if (check(sensitivity)) {
			return new ChannelSeismogram(chan, Arithmatic.mul(seis, -1),
					new Sensitivity(-1 * sensitivity.sensitivity_factor,
							sensitivity.frequency));
		}
		return new ChannelSeismogram(chan, seis, sensitivity);
	}
}
