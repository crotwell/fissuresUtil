/**
 * WorkerThread.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.cache;

import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class WorkerThreadPool{
    public WorkerThreadPool(String name, int numThreads){
        this(name, numThreads, Thread.NORM_PRIORITY);
    }


    public WorkerThreadPool(String name, int numThreads, int priority) {
        this.name = name;
        this.priority = priority;
        for (int i = 0; i < numThreads; i++) { createWorker(); }
    }

    protected synchronized void createWorker(){
        BackgroundWorker t = new BackgroundWorker(name + workers.size(),
                                                  priority);
        t.start();
        workers.add(t);
    }

    public synchronized void invokeLater(Runnable runnable) {
        if(!queue.contains(runnable)){
            queue.addFirst(runnable);
        }
        notifyAll();
    }

    public static WorkerThreadPool getDefaultPool() {
        if (defaultPool == null) {
            defaultPool = new WorkerThreadPool("default workers", 2);
        }
        return defaultPool;
    }

    private static WorkerThreadPool defaultPool;


    private synchronized Runnable getFromQueue() throws InterruptedException {
        while (queue.isEmpty()) {
            wait();
        }
        return (Runnable)queue.removeLast();
    }

    protected LinkedList workers = new LinkedList();

    protected List active = new ArrayList();

    protected LinkedList queue = new LinkedList();

    private String name;

    private int priority;

    private class BackgroundWorker extends Thread {
        protected BackgroundWorker(String name, int priority) {
            super(name);
            setPriority(priority);
            setDaemon(true);
        }

        public void run() {
            active.add(this);
            while (noQuit) {
                try {
                    Runnable r = getFromQueue();
                    r.run();
                } catch (Throwable e) {
                    GlobalExceptionHandler.handle(e);
                }
            }
            active.remove(this);
        }

        boolean noQuit = true;
    }
}

