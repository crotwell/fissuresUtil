package edu.sc.seis.fissuresUtil.display.drawable;
import edu.sc.seis.fissuresUtil.display.SeismogramDisplay;
import edu.sc.seis.fissuresUtil.freq.NamedFilter;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;
import edu.sc.seis.fissuresUtil.xml.FilteredDataSetSeismogram;

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
    public FilteredSeismogramShape(NamedFilter filter,
                                   DataSetSeismogram seismogram,
                                   SeismogramDisplay parent){
        super(parent, FilteredDataSetSeismogram.getFiltered(seismogram,filter));
    }

    public NamedFilter getFilter(){
        return ((FilteredDataSetSeismogram)getSeismogram()).getFilter();
    }

    public DataSetSeismogram getFilteredSeismogram(){
        return getSeismogram();
    }
}// FilteredSeismogramShape
