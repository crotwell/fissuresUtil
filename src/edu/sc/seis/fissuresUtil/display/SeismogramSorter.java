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
      
    public int sort(DataSetSeismogram[] seismograms){
	for(int i = 0; i < seismograms.length; i++){
	    names.add(seismograms[i].getName());
	}
	return this.names.size() - 1;
    }
    
    public boolean contains(DataSetSeismogram[] seismos){
	for(int i = 0; i < seismos.length; i++){
	    if(this.names.contains(seismos[i].toString()))
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
