package edu.sc.seis.fissuresUtil.display;

import java.util.LinkedList;
import java.util.HashMap;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;

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
    public int sort(DataSetSeismogram seismo, String name){
	seismos.add(name);
	return seismos.size();
    }

    public boolean contains(String name){
	if(seismos.contains(name))
	    return true;
	return false;
    }

    public boolean remove(String name){
	return seismos.remove(name);
    }
	 
    
    protected LinkedList seismos = new LinkedList();
}// SeismogramSorter
