package edu.sc.seis.fissuresUtil.display;

import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;

/**
 * @author groves Created on Mar 28, 2005
 */
public class SeismogramContainerFactory {

    public static SeismogramContainer create(DataSetSeismogram dss) {
        return create(null, dss);
    }

    public static SeismogramContainer create(SeismogramContainerListener initialListener,
                                             DataSetSeismogram seismogram) {
        return new SoftRefSeismogramContainer(initialListener, seismogram);
    }
}