package edu.sc.seis.fissuresUtil.display.registrar;
import edu.iris.Fissures.model.UnitRangeImpl;
import edu.sc.seis.fissuresUtil.display.DisplayUtils;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;

/**
 * AmpEvent.java
 *
 *
 * Created: Sun Sep 15 20:01:55 2002
 *
 * @author <a href="mailto:">Charlie Groves</a>
 * @version
 */

public interface AmpEvent {
    /**
     *@returns the amplitude range for the given seismogram
     */
    public UnitRangeImpl getAmp(DataSetSeismogram seismo);

    /**
     *@returns true if the amp event has data about the given seismogram,
     * false otherwise
     */
    public boolean contains(DataSetSeismogram seismo);

    /**
     *@returns the generic amplitude range that describes the overall range of
     * the events seismograms for things like scale borders
     */
    public UnitRangeImpl getAmp();

    /**
     *@returns the seismograms held by this event
     */
    public DataSetSeismogram[] getSeismograms();
}// AmpEvent
