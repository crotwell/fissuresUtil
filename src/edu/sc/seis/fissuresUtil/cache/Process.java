/**
 * Process.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.cache;


public interface Process extends Runnable{
    /**
     * Method getStatus is used to indicate the current status of this long
     * running process
     *
     * @return   a String describing what this process is up to at the current
     * time
     *
     */
    public String getStatus();


    /**
     * Method getName returns a name for this process that succinctly describes
     * its action
     *
     * @return   a String naming this process.  If the string is more than 30
     * characters it will be truncated
     *
     */
    public String getName();

    /**
     * Method setStatusListener adds a status listener to this
     * Process that will be notified whenver the status changes
     *
     */
    public void addStatusListener(StatusListener listener);

    /**
     * Method finished allows a process monitor to know if the process is
     * finished.  When finished is set to true, a status changed message is
     * fired to all StatusListeners
     *
     * @return true if finished, false otherwise
     *
     */
    public boolean finished();

    /**
     *String used to set status when a task is finished
     */
    public static final String FINISHED = "Finished";

    public static final String INITIALIZE = "Initializing";
}
