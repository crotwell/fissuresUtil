package edu.sc.seis.fissuresUtil.display.drawable;
import edu.sc.seis.fissuresUtil.display.SeismogramDisplay;
import edu.sc.seis.fissuresUtil.freq.ColoredFilter;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;
import edu.sc.seis.fissuresUtil.xml.FilteredDataSetSeismogram;

public class DrawableFilteredSeismogram extends DrawableSeismogram{
    public DrawableFilteredSeismogram(SeismogramDisplay parent,
                                      DataSetSeismogram seismo,
                                      ColoredFilter filter){
        super(parent,
              new FilteredSeismogramShape(filter, seismo, parent),
              filter.getColor(),
              seismo.toString(),
              new FilteredSeismogramRemover(seismo, filter, parent));
        setVisibility(filter.getVisibility());
    }

    public String getName(){ return getFilteredSeismogram().getName(); }

    public ColoredFilter getFilter(){
        return ((FilteredSeismogramShape)shape).getFilter();
    }

    public DataSetSeismogram getFilteredSeismogram(){
        return ((FilteredSeismogramShape)shape).getFilteredSeismogram();
    }
}

