/**
 * WorkerThread.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.cache;

import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import org.apache.log4j.Logger;

public class WorkerThreadPool{
    public WorkerThreadPool(String name, int numThreads){
        this(name, numThreads, Thread.NORM_PRIORITY);
    }

    public WorkerThreadPool(String name, int numThreads, int priority) {
        this.numThreads = numThreads;
        for (int i = 0; i < numThreads; i++) {
            BackgroundWorker bw = new BackgroundWorker(name + i, priority);
            idle.add(bw);
            bw.start();
        }
    }

    public synchronized void invokeLater(Runnable runnable) {
        if(!queue.contains(runnable)){ queue.addFirst(runnable); }
        notifyAll();
    }

    public static WorkerThreadPool getDefaultPool() {
        if (defaultPool == null) {
            defaultPool = new WorkerThreadPool("default workers", 2);
        }
        return defaultPool;
    }

    private synchronized Runnable getFromQueue() throws InterruptedException {
        while (queue.isEmpty()) { wait(); }
        idle.remove(Thread.currentThread());
        return (Runnable)queue.removeLast();
    }

    public synchronized int getNumWaiting(){ return queue.size(); }

    public int getNumIdle(){ return idle.size(); }

    public synchronized boolean isEmployed(){
        return getNumWaiting() != 0 || getNumIdle() < numThreads;
    }

    private int numThreads;
    private LinkedList queue = new LinkedList();
    private Set idle = Collections.synchronizedSet(new HashSet());
    private static WorkerThreadPool defaultPool;

    private class BackgroundWorker extends Thread {
        protected BackgroundWorker(String name, int priority) {
            super(name);
            setPriority(priority);
            setDaemon(true);
        }

        public void run() {
            while (true) {
                try {
                    getFromQueue().run();
                } catch (Throwable e) {
                    GlobalExceptionHandler.handle(e);
                }finally{
                    idle.add(this);
                }
            }
        }
    }

    private static Logger logger = Logger.getLogger(WorkerThreadPool.class);
}
