/**
 * WorkerThread.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.cache;

import edu.sc.seis.fissuresUtil.exceptionHandlerGUI.GlobalExceptionHandler;
import java.util.LinkedList;

public class WorkerThreadPool
{
    public WorkerThreadPool(String name, int numThreads){
        this(name, numThreads, Thread.NORM_PRIORITY);
    }


    public WorkerThreadPool(String name, int numThreads, int priority) {
        this.name = name;
        for (int i = 0; i < numThreads; i++) {
            BackgroundWorker t = new BackgroundWorker(name + i, priority);
            t.start();
            workers.add(t);
        }
    }

    public synchronized void invokeLater(Runnable runnable) {
        queue.addFirst(runnable);
        notifyAll();
    }

    public static WorkerThreadPool getDefaultPool() {
        if (defaultPool == null) {
            defaultPool = new WorkerThreadPool("default workers", 2);
        }
        return defaultPool;
    }

    static WorkerThreadPool defaultPool;


    protected synchronized Runnable getFromQueue() throws InterruptedException {
        while (queue.isEmpty()) {
            wait();
        }
        return (Runnable)queue.removeLast();
    }

    protected LinkedList queue = new LinkedList();

    protected LinkedList workers = new LinkedList();

    private String name;

    protected class BackgroundWorker extends Thread {
        protected BackgroundWorker(String name, int priority) {
            super(name);
            setPriority(priority);
            setDaemon(true);
        }

        public void run() {
            while (noQuit) {
                try {
                    Runnable r = getFromQueue();
                    r.run();
                } catch (Exception e) {
                    GlobalExceptionHandler.handleStatic(e);
                }
            }
        }

        boolean noQuit = true;
    }
}

