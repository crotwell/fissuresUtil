package edu.sc.seis.fissuresUtil.bag;

import edu.iris.Fissures.FissuresException;
import edu.iris.Fissures.IfNetwork.Sensitivity;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;

public class NegativeSensitivity {

    public static boolean check(Sensitivity sensitivity) {
        return sensitivity.sensitivity_factor < 0;
    }
    
    public static boolean check(QuantityImpl sensitivity) {
        return sensitivity.value < 0;
    }

	public static ChannelSeismogram correct(ChannelImpl chan,
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
