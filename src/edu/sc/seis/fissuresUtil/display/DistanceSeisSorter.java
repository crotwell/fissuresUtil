package edu.sc.seis.fissuresUtil.display;

/**
 * DistanceSeisSorter.java
 *
 *
 * Created: Tue Jul  9 13:32:21 2002
 *
 * @author <a href="mailto:">Charlie Groves</a>
 * @version
 */

public class DistanceSeisSorter extends SeismogramSorter {
    public int sort(DataSetSeismogram seismo, String name){
	int i = 0;
	while(i < seismos.size() && seismo.isFurtherThan((DataSetSeismogram)seismos.get(names.get(i)))){
	    i++;
	}
	seismos.put(name, seismo);
	names.add(i, seismo);
	return i;
    }
}// DistanceSeisSorter
