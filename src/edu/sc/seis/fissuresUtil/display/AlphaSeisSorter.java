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
    public int sort(DataSetSeismogram seismo, String name){
	int i = 0;
	while(i < names.size() && ((String)names.get(i)).compareToIgnoreCase(name) < 0){
	    i++;
	}
	names.add(i, name);
	return i;
    }
}// AlphaSeisSorter
