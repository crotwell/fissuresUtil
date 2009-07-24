package edu.sc.seis.fissuresUtil.xml;

import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.cache.WorkerThreadPool;

/**
 * MemoryDataSetSeismogram.java
 * 
 * 
 * Created: Wed Mar 12 11:38:42 2003
 * 
 * @author <a href="mailto:crotwell@owl.seis.sc.edu">Philip Crotwell</a>
 * @version 1.0
 */
public class MemoryDataSetSeismogram extends DataSetSeismogram implements
        Cloneable {

    public MemoryDataSetSeismogram(RequestFilter requestFilter, String name) {
        super(null, name, requestFilter);
        memoryCache = new LocalSeismogramImpl[0];
    }

    public MemoryDataSetSeismogram(LocalSeismogramImpl seis, String name) {
        this(makeSeisArray(seis), null, name);
    }

    public MemoryDataSetSeismogram(LocalSeismogramImpl seis) {
        this(makeSeisArray(seis), null);
    }

    public MemoryDataSetSeismogram(LocalSeismogramImpl seis, DataSet ds) {
        this(makeSeisArray(seis), ds);
    }

    public MemoryDataSetSeismogram(LocalSeismogramImpl seis,
                                   DataSet ds,
                                   String name) {
        this(makeSeisArray(seis), ds, name);
    }

    public MemoryDataSetSeismogram(LocalSeismogramImpl[] seis, DataSet ds) {
        this(seis, ds, null);
    }

    public MemoryDataSetSeismogram(LocalSeismogramImpl[] seis,
                                   DataSet ds,
                                   String name) {
        this(seis, ds, name, makeRequestFilter(seis));
    }

    public MemoryDataSetSeismogram(LocalSeismogramImpl[] seis,
                                   DataSet ds,
                                   String name,
                                   RequestFilter rf) {
        super(ds, name, rf);
        memoryCache = seis;
    }

    public void retrieveData(final SeisDataChangeListener dataListener) {
        WorkerThreadPool.getDefaultPool().invokeLater(new Runnable() {

            public void run() {
                pushData(memoryCache, dataListener);
                finished(dataListener);
            }
        });
    }

    public synchronized LocalSeismogramImpl[] getCache() {
        return memoryCache;
    }

    public synchronized void add(LocalSeismogramImpl seis) {
        LocalSeismogramImpl[] tmp = new LocalSeismogramImpl[memoryCache.length + 1];
        System.arraycopy(memoryCache, 0, tmp, 0, memoryCache.length);
        tmp[tmp.length - 1] = seis;
        memoryCache = tmp;
    }

    protected LocalSeismogramImpl[] memoryCache;

    static final RequestFilter makeRequestFilter(LocalSeismogramImpl[] seis) {
        if(seis == null) {
            throw new IllegalArgumentException("Seismogram array cannot be null");
        }
        if(seis.length == 0) {
            throw new IllegalArgumentException("Seismogram array cannot be empty");
        }
        MicroSecondDate pre = seis[0].getBeginTime();
        MicroSecondDate post = seis[0].getEndTime();
        for(int i = 0; i < seis.length; i++) {
            if(pre.after(seis[i].getBeginTime())) {
                pre = seis[i].getBeginTime();
            } // end of if ()
            if(post.before(seis[i].getEndTime())) {
                post = seis[i].getEndTime();
            } // end of if ()
        } // end of for ()
        RequestFilter out = new RequestFilter(seis[0].channel_id,
                                              pre.getFissuresTime(),
                                              post.getFissuresTime());
        return out;
    }

    static final LocalSeismogramImpl[] makeSeisArray(LocalSeismogramImpl seis) {
        LocalSeismogramImpl[] tmp = {seis};
        return tmp;
    }
}
