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
	while(i < seismos.size() && seismo.isFurtherThan((DataSetSeismogram)seismos.get(i))){
	    i++;
	}
	seismos.add(i, seismo);
	return i;
    }
}// DistanceSeisSorter
