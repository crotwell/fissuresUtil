package edu.sc.seis.fissuresUtil.display;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;
import java.awt.Color;

/**
 * ThreeCSelection.java
 *
 *
 * Created: Tue Jan 14 12:50:57 2003
 *
 * @author <a href="mailto:crotwell@owl.seis.sc.edu">Philip Crotwell</a>
 * @version 1.0
 */
public class ThreeCSelection extends Selection {
     public ThreeCSelection(MicroSecondDate begin, 
			    MicroSecondDate end, 
			    Registrar reg, 
			    DataSetSeismogram[] seismograms, 
			    BasicSeismogramDisplay parent, 
			    Color color){
	 super(begin, end, reg, seismograms, parent, color);
     }

} // ThreeCSelection
