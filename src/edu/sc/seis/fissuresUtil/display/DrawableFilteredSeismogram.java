package edu.sc.seis.fissuresUtil.display;
import edu.sc.seis.fissuresUtil.freq.ColoredFilter;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;

public class DrawableFilteredSeismogram extends DrawableSeismogram{
    public DrawableFilteredSeismogram(DataSetSeismogram seismo,
                                      ColoredFilter filter){
        super(new FilteredSeismogramShape(filter, seismo),
              filter.getColor(),
              seismo.toString());
        setVisibility(filter.getVisibility());
    }
    
    public ColoredFilter getFilter(){
        return ((FilteredSeismogramShape)shape).getFilter();
    }
    
    public DataSetSeismogram getFilteredSeismogram(){
        return ((FilteredSeismogramShape)shape).getFilteredSeismogram();
    }
}

