
package edu.sc.seis.fissuresUtil.dataset;

import edu.iris.Fissures.AuditInfo;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.sc.seis.fissuresUtil.xml.DataSet;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;
import edu.sc.seis.fissuresUtil.xml.StdDataSetParamNames;

/**
 * Organizer.java
 *
 *
 * Created: Mon Jan  7 12:24:28 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version $Id: Organizer.java 19011 2007-04-27 17:24:29Z crotwell $
 */

public interface Organizer extends StdDataSetParamNames {

    public void addSeismogram(DataSetSeismogram seis, AuditInfo[] audit);

    public void addSeismogram(DataSetSeismogram seis,
                              EventAccessOperations event,
                              AuditInfo[] audit);

    public void addChannel(Channel chan, AuditInfo[] audit);

    public void addChannel(Channel chan,
                           EventAccessOperations event,
                           AuditInfo[] audit);

    public void addDataSet(DataSet dataset, AuditInfo[] audit);

    public void addDataSetChangeListener(DataSetChangeListener l);

    public void removeDataSetChangeListener(DataSetChangeListener l);

}// Organizer
