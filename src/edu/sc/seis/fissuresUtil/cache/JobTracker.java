/**
 * JobTracker.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.cache;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class JobTracker implements StatusListener{


    private JobTracker(){}


    public void add(TrackerListener listener){
        listeners.add(listener);
    }

    public void add(Job addend){
        addend.add(this);
        statusUpdated(addend);
    }

    public synchronized void statusUpdated(Job updated) {
        if(updated.isFinished()){
            active.remove(updated);
            if(!finished.contains(updated)){
                finished.add(updated);
            }
        }else{
            finished.remove(updated);
            if(!active.contains(updated)){
                active.add(updated);
            }
        }
        fireTrackerUpdated();
    }

    public synchronized List getActiveJobs(){
        return active;
    }

    public synchronized List getFinishedJobs(){
        return finished;
    }

    public synchronized void clearFinished(){
        finished.clear();
        fireTrackerUpdated();
    }

    private void fireTrackerUpdated(){
        Iterator it = listeners.iterator();
        while(it.hasNext()){
            ((TrackerListener)it.next()).trackerUpdated(this);
        }
    }

    public static JobTracker getTracker(){ return tracker;}

    private List finished = new ArrayList();

    private List active = new ArrayList();

    private static JobTracker tracker = new JobTracker();

    private List listeners = new ArrayList();
}

