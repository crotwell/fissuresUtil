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
	names.add(name);
	int rtnValue = seismos.size();
	seismos.put(name, seismo);
	return rtnValue;
    }

    public boolean contains(String name){
	if(names.contains(name))
	    return true;
	return false;
    }

    public boolean remove(String name){
	seismos.remove(name);
	return names.remove(name);
    }
	 
    
    protected LinkedList names = new LinkedList();
    
    protected HashMap seismos = new HashMap();
}// SeismogramSorter
