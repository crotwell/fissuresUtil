package edu.sc.seis.fissuresUtil.xml;

import edu.iris.Fissures.IfSeismogramDC.*;
import edu.iris.Fissures.seismogramDC.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.model.*;
import edu.iris.Fissures.*;

import java.util.*;
import javax.swing.SwingUtilities;
import org.apache.log4j.*;

/**
 * MemoryDataSetSeismogram.java
 *
 *
 * Created: Wed Mar 12 11:38:42 2003
 *
 * @author <a href="mailto:crotwell@owl.seis.sc.edu">Philip Crotwell</a>
 * @version 1.0
 */
public class MemoryDataSetSeismogram extends DataSetSeismogram implements Cloneable {

    public MemoryDataSetSeismogram(LocalSeismogramImpl seis,
				   String name) {
	this(makeSeisArray(seis), null, name);
    }

    public MemoryDataSetSeismogram(LocalSeismogramImpl seis) {
	this(makeSeisArray(seis), null);
    }

    public MemoryDataSetSeismogram(LocalSeismogramImpl seis,
				   DataSet ds) {
	this(makeSeisArray(seis), ds);
    }

    public MemoryDataSetSeismogram(LocalSeismogramImpl seis,
				   DataSet ds,
				   String name) {
	this(makeSeisArray(seis), ds, name);
    }

    public MemoryDataSetSeismogram(LocalSeismogramImpl[] seis,
				   DataSet ds) {
	this(seis, ds, null);
    }

    public MemoryDataSetSeismogram(LocalSeismogramImpl[] seis,
				   DataSet ds,
				   String name) {
	super(makeRequestFilter(seis), null, null, name);
	seisCache = seis;
    }

    public Object clone() {
	return super.clone();
    }

    public void retrieveData(final SeisDataChangeListener dataListener) {
	SwingUtilities.invokeLater(new Runnable() {
		public void run() {
		    pushData(seisCache, dataListener);
		    finished(dataListener);
		}
	    });
    }

    public LocalSeismogramImpl[] getSeismograms() {
	return seisCache;
    }

    protected LocalSeismogramImpl[] seisCache;

    static final RequestFilter makeRequestFilter(LocalSeismogramImpl[] seis) {
	MicroSecondDate pre = seis[0].getBeginTime();
	MicroSecondDate post = seis[0].getEndTime();
	for ( int i=0; i<seis.length;i++) {
	    if ( pre.after(seis[i].getBeginTime())) {
		pre = seis[i].getBeginTime();
	    } // end of if ()
	    if ( post.before(seis[i].getEndTime())) {
		post = seis[i].getEndTime();
	    } // end of if ()
	} // end of for ()
	RequestFilter out =
	    new RequestFilter(seis[0].channel_id,
			      pre.getFissuresTime(),
			      post.getFissuresTime());
	return out;
    }

    static final LocalSeismogramImpl[] makeSeisArray(LocalSeismogramImpl seis) {
	LocalSeismogramImpl[] tmp = new LocalSeismogramImpl[1];
	tmp[0] = seis;
	return tmp;
    }
}
