package edu.sc.seis.fissuresUtil.display;

import java.util.LinkedList;
import java.util.HashMap;
import java.util.Comparator;

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
    /**
     * <code>sort</code> uses the names list in SeismogramSorter to return the alphabetical
     * position of this name and then adds it to the list
     * @param seismograms the seismograms that belong to this name
     * @param name the name to be sorted
     * @return the names alphabetical position in the list, numbered from 0
     */
    public int sort(DataSetSeismogram[] seismograms){
	int i = 0;
	while(i < names.size() && ((String)names.get(i)).compareToIgnoreCase(seismograms[i].getName()) < 0){
	    i++;
	}
	names.add(i, seismograms[i].getName());
	return i;
    }
}// AlphaSeisSorter
