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
      
    public int sort(DataSetSeismogram[] seismograms, String[] names){
	this.names.add(names);
	return this.names.size() - 1;
    }

    public boolean contains(String[] names){
	for(int i = 0; i < names.length; i++){
	    if(this.names.contains(names[i]))
		return true;
	}
	return false;
    }

    public boolean remove(String name){
	return names.remove(name);
    }
	 
    
    protected LinkedList names = new LinkedList();
    
    protected HashMap seismos = new HashMap();
}// SeismogramSorter
