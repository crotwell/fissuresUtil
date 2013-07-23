package edu.sc.seis.fissuresUtil.chooser;

import java.util.List;

import edu.iris.Fissures.IfNetwork.ChannelNotFound;
import edu.iris.Fissures.IfNetwork.Instrumentation;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.network.NetworkAttrImpl;
import edu.iris.Fissures.network.StationImpl;
import edu.sc.seis.fissuresUtil.sac.InvalidResponse;

/** hacky copy-paste of SOD's NetworkSource, probably should break up packages so don't have
 * to do this to avoid circular dependencies.
 * @author crotwell
 *
 */
public interface ChannelChooserSource {

    public List<NetworkAttrImpl> getNetworks() throws ChannelChooserException;

    public List<StationImpl> getStations(NetworkAttrImpl net) throws ChannelChooserException;

    public List<ChannelImpl> getChannels(StationImpl station) throws ChannelChooserException;

    public QuantityImpl getSensitivity(ChannelImpl chanId) throws ChannelNotFound, InvalidResponse, ChannelChooserException;

    public Instrumentation getInstrumentation(ChannelImpl chanId) throws ChannelNotFound, InvalidResponse, ChannelChooserException;

}
