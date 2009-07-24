/**
 * AbstractJob.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.cache;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;

public abstract class AbstractJob implements Job, Runnable {
    public AbstractJob(String name){
        this.name = name;
    }

    public void run() {
        try {
            runJob();
            if ( ! isFinished()) {
                // runJob didn't set finished on its own
                setFinished();
            }
        } catch (Throwable e) {
            if ( ! isFinished()) {
                // runJob didn't set finished on its own
                setFinished();
            }
            GlobalExceptionHandler.handle(e);
        }
    }

    /**
     * Method getStatus is used to indicate the current status of this long
     * running Job
     *
     * @return   a String describing what this process is up to at the current
     * time
     *
     */
    public String getStatus(){
        return status;
    }

    public void setStatus(String status){
        this.status = status;
        fireStatusUpdate();
    }

    /**
     * Method getName returns a name for this process that succinctly describes
     * its action
     *
     * @return   a String naming this process.  If the string is more than 30
     * characters it will be truncated
     *
     */
    public String getName(){
        return name;
    }

    /**
     * Method addStatusListener adds a status listener to this
     * Process that will be notified whenver the status changes
     *
     */
    public void add(StatusListener listener){
        if(!listeners.contains(listener))
            listeners.add(listener);
    }

    private void fireStatusUpdate(){
        Iterator it = listeners.iterator();
        while(it.hasNext()){
            ((StatusListener)it.next()).statusUpdated(this);
        }
    }

    public boolean isFinished(){ return finished; }

    protected void setFinished(){
        setFinished(true);
    }

    protected void setFinished(boolean finished){
        this.finished = finished;
        if(finished){
            setStatus(FINISHED);
        }
        fireStatusUpdate();
    }

    private boolean finished = false;

    private List listeners = new ArrayList();

    private String status = INITIALIZE;

    private String name;
}

