package edu.sc.seis.fissuresUtil.display;
import edu.sc.seis.fissuresUtil.freq.ColoredFilter;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;
import edu.sc.seis.fissuresUtil.xml.FilteredDataSetSeismogram;
import javax.swing.JComponent;

/**
 * FilteredSeismogramShape.java
 *
 *
 * Created: Thu Aug 08 14:24:04 2002
 *
 * @author Charlie Groves
 * @version
 */

public class FilteredSeismogramShape extends SeismogramShape {
    public FilteredSeismogramShape(ColoredFilter filter,
                                   DataSetSeismogram seismogram,
                                   JComponent parent){
        super(parent, new FilteredDataSetSeismogram(seismogram, filter));
    }

    public ColoredFilter getFilter(){
        return ((FilteredDataSetSeismogram)getSeismogram()).getFilter();
    }

    public DataSetSeismogram getFilteredSeismogram(){
        return getSeismogram();
    }
}// FilteredSeismogramShape
