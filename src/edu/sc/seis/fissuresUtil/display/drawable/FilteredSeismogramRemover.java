package edu.sc.seis.fissuresUtil.display.drawable;

/**
 * FilteredSeismogramRemover.java
 *
 * @author Created by Charlie Groves
 */

import edu.sc.seis.fissuresUtil.display.SeismogramDisplay;
import edu.sc.seis.fissuresUtil.freq.ColoredFilter;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;

public class FilteredSeismogramRemover extends SeismogramRemover{
    public FilteredSeismogramRemover(DataSetSeismogram seis,
                                     ColoredFilter filter,
                                     SeismogramDisplay display){
        super(seis, display);
        this.filter = filter;
    }

    public void clicked(){
        display.removeFilter(filter);
    }

    private ColoredFilter filter;
}
