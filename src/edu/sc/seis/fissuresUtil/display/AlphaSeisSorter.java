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
    public int sort(DataSetSeismogram seismo, String name){
	int i = 0;
	while(i < seismos.size() && ((String)seismos.get(i)).compareToIgnoreCase(name) < 0){
	    i++;
	}
	seismos.add(i, name);
	return i;
    }
}// AlphaSeisSorter
