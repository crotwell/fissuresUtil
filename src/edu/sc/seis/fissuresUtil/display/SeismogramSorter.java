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
    public int sort(LocalSeismogramImpl seismo, String name){
	names.add(name);
	return names.size();
    }

    public boolean contains(String name){
	if(names.contains(name))
	    return true;
	return false;
    }

    public boolean remove(String name){
	return names.remove(name);
    }
	 
    
    protected HashMap seismos;

    protected LinkedList names = new LinkedList();
}// SeismogramSorter
