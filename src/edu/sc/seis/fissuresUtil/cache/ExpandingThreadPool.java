/**
 * ExpandingThreadPool.java
 *
 * @author Created by Charles Groves
 */

package edu.sc.seis.fissuresUtil.cache;

import java.util.HashSet;
import java.util.Set;

public class ExpandingThreadPool extends WorkerThreadPool{
    public ExpandingThreadPool(String name, int maxSize){
        super(name, 1);
        this.maxSize = maxSize;
    }
    
    public synchronized void invokeLater(Runnable r){
        formerRunnables.add(r);
        if(active.size() == workers.size() && workers.size() < maxSize){
            createWorker();
        }
        super.invokeLater(r);
    }
    
    private Set formerRunnables = new HashSet();
    
    private int maxSize;
}
