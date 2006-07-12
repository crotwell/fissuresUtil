package edu.sc.seis.fissuresUtil.bag;

import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.Sensitivity;
import edu.iris.Fissures.IfSeismogramDC.LocalSeismogram;

public class ChannelSeismogram {

	public ChannelSeismogram(Channel chan, LocalSeismogram seis, Sensitivity sensitivity) {
		this.seis = seis;
		this.chan = chan;
		this.sensitivity = sensitivity;
	}

	public Channel getChannel() {
		return chan;
	}

	public LocalSeismogram getSeismogram() {
		return seis;
	}

	public Sensitivity getSensitivity() {
        if(sensitivity == null){
            throw new UnsupportedOperationException("This channelseismogram has no sensitivity");
        }
		return sensitivity;
	}

	LocalSeismogram seis;

	Channel chan;

	Sensitivity sensitivity;
}
