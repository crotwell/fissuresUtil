package edu.sc.seis.fissuresUtil.display;
import edu.sc.seis.fissuresUtil.freq.ColoredFilter;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;
import javax.swing.JComponent;

public class DrawableFilteredSeismogram extends DrawableSeismogram{
    public DrawableFilteredSeismogram(JComponent parent,
									  DataSetSeismogram seismo,
                                      ColoredFilter filter){
        super(parent,
			  new FilteredSeismogramShape(filter, seismo, parent),
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

