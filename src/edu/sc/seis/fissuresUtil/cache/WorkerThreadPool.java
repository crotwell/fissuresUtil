/**
 * WorkerThread.java
 * 
 * @author Created by Omnicore CodeGuide
 */
package edu.sc.seis.fissuresUtil.cache;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;

public class WorkerThreadPool {

    public WorkerThreadPool(String name, int numThreads) {
        this(name, numThreads, Thread.NORM_PRIORITY);
    }

    public WorkerThreadPool(String name, int numThreads, int priority) {
        this.poolSize = numThreads;
        tg = new ThreadGroup(name);
        tg.setMaxPriority(priority);
        fillPool();
    }
    
    public void setMaxQueueSize(int i) {
        if (i < 1) {
            throw new IllegalArgumentException("queue must be at least size 1 > "+i);
        }
        maxQueueSize = i;
    }

    public synchronized void invokeLater(Runnable runnable) {
        while (queue.size() > maxQueueSize) {
            try {
                Thread.sleep(100);
            } catch(InterruptedException e) {}
        }
        if(!queue.contains(runnable)) {
            queue.addFirst(runnable);
        }
        fillPool();
        notifyAll();
    }

    private void fillPool() {
        int numNewThreads = poolSize - tg.activeCount();
        for(int i = 0; i < numNewThreads; i++) {
            BackgroundWorker bw = new BackgroundWorker(tg, tg.getName()+" "
                    + totalNumCreated++, tg.getMaxPriority());
            idle.add(bw);
            bw.start();
        }
    }

    public synchronized static WorkerThreadPool getDefaultPool() {
        if(defaultPool == null) {
            defaultPool = new WorkerThreadPool("default workers", 2);
        }
        return defaultPool;
    }

    private synchronized Runnable getFromQueue() throws InterruptedException {
        while(queue.isEmpty()) {
            wait();
        }
        idle.remove(Thread.currentThread());
        return (Runnable)queue.removeLast();
    }

    public synchronized int getNumWaiting() {
        return queue.size();
    }

    public int getNumIdle() {
        return idle.size();
    }

    public synchronized boolean isEmployed() {
        return getNumWaiting() != 0 || getNumIdle() < poolSize;
    }

    private int totalNumCreated = 0;

    private int poolSize;
    
    private int maxQueueSize = DEFAULT_MAX_QUEUE_SIZE;

    private ThreadGroup tg;

    private LinkedList queue = new LinkedList();

    private Set idle = Collections.synchronizedSet(new HashSet());

    private static WorkerThreadPool defaultPool;
    
    public static final int DEFAULT_MAX_QUEUE_SIZE = 1000;

    private class BackgroundWorker extends Thread {

        protected BackgroundWorker(ThreadGroup group, String name, int priority) {
            super(group, name);
            setPriority(priority);
            setDaemon(true);
        }

        public void run() {
            while(true) {
                try {
                    getFromQueue().run();
                } catch(Throwable e) {
                    GlobalExceptionHandler.handle(e);
                } finally {
                    idle.add(this);
                }
            }
        }
    }

}