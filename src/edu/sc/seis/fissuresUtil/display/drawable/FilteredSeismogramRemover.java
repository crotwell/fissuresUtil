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
    public FilteredSeismogramRemover(SeismogramDisplay display,
                                    DrawableFilteredSeismogram filtered){
        super(null, display);
        this.filtered = filtered;
    }

    public void clicked(){
        filtered.setVisibility(false);
    }

    private DrawableFilteredSeismogram filtered;

}
