/**
 * ExpandingThreadPool.java
 *
 * @author Created by Charles Groves
 */

package edu.sc.seis.fissuresUtil.cache;

public class ExpandingThreadPool extends WorkerThreadPool{
    public ExpandingThreadPool(String name, int maxSize){
        super(name, maxSize);
        this.maxSize = maxSize;
    }
    //FIXME this is a race condition
    /*public synchronized void invokeLater(Runnable r){
        super.invokeLater(r);
        if(queue.contains(r) && workers.size() < maxSize){
            createWorker();
        }
     }*/
    
    private int maxSize;
}
