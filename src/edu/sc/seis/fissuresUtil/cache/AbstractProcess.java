/**
 * AbstractProcess.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.cache;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class AbstractProcess implements Process{
    public AbstractProcess(String name){
        this.name = name;
    }


    /**
     * Method getStatus is used to indicate the current status of this long
     * running process
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
    public void addStatusListener(StatusListener listener){
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
        finished = true;
        setStatus(FINISHED);
        fireStatusUpdate();
    }

    private boolean finished = false;

    private List listeners = new ArrayList();

    private String status = INITIALIZE;

    private String name;
}

