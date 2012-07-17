package edu.sc.seis.fissuresUtil.cache;
import java.util.LinkedList;
import java.util.List;



public abstract class AbstractQueue {
    //data structure to hold the values.???
    public AbstractQueue() {

    }


    /**
     * pops the first element of the queue.
     * @return a <code>java.lang.Object</code> value
     */
    public synchronized java.lang.Object pop() {

        while(list.size() == 0 && sourceAlive == true) {
            try {
                wait();
            } catch(InterruptedException ie) { }

        }
        if(list.size() == 0) return null;
        java.lang.Object obj = list.get(list.size()-1);
        list.remove(list.size() - 1);
        notifyAll();
        return obj;

    }


    /**
     * inserts the obj at the end of the queue.
     *
     * @param obj a <code>java.lang.Object</code> value to be inserted into the queue.
     */
    public synchronized void push(java.lang.Object obj) {
        list.add(0, obj);
        notifyAll();
    }


    /**
     * returns the length of the queue.
     *
     * @return an <code>int</code> value
     */
    public synchronized int getLength() {

        return list.size();

    }

    /**
     * sets if the source i.e., the thread which pushes objects into the queue
     * is alive
     *
     * @param value a <code>boolean</code> value
     */
    public synchronized void setSourceAlive(boolean value) {

        this.sourceAlive = value;
        notifyAll();
    }

    /**
     * returns true if the source i.e., the thread which pushes objects into the queue
     * is alive, else returns false.
     *
     * @return a <code>boolean</code> value
     */
    public synchronized boolean getSourceAlive() {
        return this.sourceAlive;
    }

    private boolean sourceAlive = true;

    private List list = new LinkedList();

}
