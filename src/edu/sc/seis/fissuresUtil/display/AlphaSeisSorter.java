package edu.sc.seis.fissuresUtil.display;

import java.util.LinkedList;
import java.util.HashMap;
import java.util.Comparator;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;

/**
 * AlphaSeisSorter.java
 *
 *
 * Created: Thu Jul  4 12:40:41 2002
 *
 * @author <a href="mailto:">Charlie Groves</a>
 * @version
 */

public class AlphaSeisSorter extends SeismogramSorter{
    public int sort(LocalSeismogramImpl seismo, String name){
	int i = 0;
	while(i < names.size() && ((String)names.get(i)).compareToIgnoreCase(name) < 0){
	    i++;
	}
	names.add(i, seismo.getName());
	return i;
    }

    public boolean contains(String name){
	if(names.contains(name))
	   return true;
	return false;
    }
    
    protected HashMap seismos;

    protected LinkedList names = new LinkedList();
}// AlphaSeisSorter
