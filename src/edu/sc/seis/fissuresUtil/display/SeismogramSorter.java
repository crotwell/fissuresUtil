package edu.sc.seis.fissuresUtil.display;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * SeismogramSorter.java
 *
 *
 * Created: Thu Jul  4 12:40:41 2002
 *
 * @author <a href="mailto:">Charlie Groves</a>
 * @version
 */

public class SeismogramSorter {

    public int sort(DataSetSeismogram seismogram){
        names.add(seismogram.toString());
        return names.size() - 1;
    }

    public boolean remove(DataSetSeismogram seismogram){
        return names.remove(seismogram.toString());
    }

    public void clear(){ names.clear(); }

    protected LinkedList names = new LinkedList();
}// SeismogramSorter
