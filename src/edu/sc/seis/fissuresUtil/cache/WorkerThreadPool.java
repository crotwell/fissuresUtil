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

    public WorkerThreadPool(String name, int numThreads) {
        for (int i = 0; i < numThreads; i++) {
            BackgroundWorker t = new BackgroundWorker("fissures worker"+i);
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

    protected class BackgroundWorker extends Thread {
        protected BackgroundWorker(String name) {
            super(name);
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

