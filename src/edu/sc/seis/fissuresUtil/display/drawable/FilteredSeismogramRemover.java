package edu.sc.seis.fissuresUtil.display.drawable;

/**
 * FilteredSeismogramRemover.java
 *
 * @author Created by Charlie Groves
 */

import edu.sc.seis.fissuresUtil.display.SeismogramDisplay;
import java.util.Iterator;

public class FilteredSeismogramRemover extends SeismogramRemover{
    public FilteredSeismogramRemover(SeismogramDisplay display,
                                     DrawableFilteredSeismogram filtered){
        super(null, display);
        this.filtered = filtered;
    }

    public void clicked(){
        Iterator it = filtered.getParent().iterator(DrawableSeismogram.class);
        while(it.hasNext()){
            ((DrawableSeismogram)it.next()).remove(filtered);
        }
    }

    private DrawableFilteredSeismogram filtered;

}
