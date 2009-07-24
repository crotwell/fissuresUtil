package edu.sc.seis.fissuresUtil.display.drawable;
import edu.sc.seis.fissuresUtil.display.SeismogramDisplay;
import edu.sc.seis.fissuresUtil.freq.NamedFilter;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;

public class DrawableFilteredSeismogram extends DrawableSeismogram{
    public DrawableFilteredSeismogram(SeismogramDisplay parent,
                                      DataSetSeismogram seismo,
                                      NamedFilter filter){
        super(parent, new FilteredSeismogramShape(filter, seismo, parent));
        setRemover(new FilteredSeismogramRemover(parent, this));
        setVisibility(filter.getVisibility());
    }

    public String getName(){ return getFilteredSeismogram().getName(); }

    public NamedFilter getFilter(){
        return ((FilteredSeismogramShape)shape).getFilter();
    }

    public DataSetSeismogram getFilteredSeismogram(){
        return ((FilteredSeismogramShape)shape).getFilteredSeismogram();
    }
}
