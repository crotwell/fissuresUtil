package edu.sc.seis.fissuresUtil.display.drawable;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.sc.seis.fissuresUtil.display.BasicSeismogramDisplay;
import edu.sc.seis.fissuresUtil.display.registrar.Registrar;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;
import java.awt.Color;

/**
 * SingleSelection represents a selected time window within a display of a
 * seismogram. Most functionality is within the Selection class.
 *
 *
 * Created: Tue Jan 14 12:48:52 2003
 *
 * @author <a href="mailto:crotwell@owl.seis.sc.edu">Philip Crotwell</a>
 * @version 1.0
 */
public class SingleSelection extends Selection {

     public SingleSelection(MicroSecondDate begin,
                MicroSecondDate end,
                Registrar reg,
                DataSetSeismogram[] seismograms,
                BasicSeismogramDisplay parent,
                Color color){
     super(begin, end, reg, seismograms, parent, color);
     }

}
