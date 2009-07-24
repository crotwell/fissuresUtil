/**
 * JobTracker.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.cache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JobTracker implements StatusListener{


    private JobTracker(){}

    public void add(Job addend){
        addend.add(this);
        statusUpdated(addend);
    }

    public void statusUpdated(Job updated) {
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

    public List getActiveJobs(){
        return active;
    }

    public List getFinishedJobs(){
        return finished;
    }

    public void clearFinished(){
        finished.clear();
        fireTrackerUpdated();
    }


    public void add(TrackerListener listener){
        listeners.add(listener);
    }

    public void remove(TrackerListener listener){
        listeners.remove(listener);
    }

    private void fireTrackerUpdated(){
        TrackerListener[] tempListeners = (TrackerListener[])listeners.toArray(new TrackerListener[0]);
        for (int i = 0; i < tempListeners.length; i++) {
            tempListeners[i].trackerUpdated(this);
        }
    }

    public static JobTracker getTracker(){ return tracker;}

    private List finished = Collections.synchronizedList(new ArrayList());

    private List active = Collections.synchronizedList(new ArrayList());

    private static JobTracker tracker = new JobTracker();

    private List listeners = Collections.synchronizedList(new ArrayList());
}

