package edu.sc.seis.fissuresUtil.netConnChecker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Category;
import edu.sc.seis.fissuresUtil.cache.WorkerThreadPool;

public class Checker {
    public Checker() {}

    public Checker(Collection connCheckerCollectionReceived) {
        connCheckers.addAll(connCheckerCollectionReceived);
    }

    public void runChecks(){
        synchronized(connCheckers){
            Iterator it = connCheckers.iterator();
            while(it.hasNext()){
                pool.invokeLater((ConnChecker)it.next());
            }
        }
    } // close runChecks

    public void add(ConnChecker connChecker) {
        connCheckers.add(connChecker);
        pool.invokeLater(connChecker);
    }

    public ConnStatus getStatus(Class checkerClass) {
        boolean trying = false;
        boolean foundOne = false;
        synchronized(connCheckers){
            Iterator it = connCheckers.iterator();
            while(it.hasNext()){
                ConnChecker connChecker = (ConnChecker)it.next();
                if (checkerClass.isAssignableFrom(connChecker.getClass())) {
                    ConnStatusResult curStatus = connChecker.getStatus();
                    if(curStatus.getStatus() == ConnStatus.SUCCESSFUL) return ConnStatus.SUCCESSFUL;
                    else if(curStatus.getStatus() == ConnStatus.TRYING) trying = true;
                } else {
                    logger.warn("Skipping "+checkerClass.getName()+"  "+connChecker.getClass().getName());
                }
            }
        }
        if(trying == true && foundOne) return ConnStatus.TRYING;
        return ConnStatus.FAILED;
    }

    /**Returns the ability to get to the net .  i.e. if any are successful ,
     * ConnStatus.SUCCESSFUL is returned.  if any are still trying and none are
     * successful, it returns ConnStatus.TRYING.  if none are trying and none
     * succeeded, it returns ConnStatus.FAILED
     */
    public ConnStatus getStatus(){
        boolean trying = false;
        synchronized(connCheckers){
            Iterator it = connCheckers.iterator();
            while(it.hasNext()){
                ConnStatusResult curStatus = ((ConnChecker)it.next()).getStatus();
                if(curStatus.getStatus() == ConnStatus.SUCCESSFUL) return ConnStatus.SUCCESSFUL;
                else if(curStatus.getStatus() == ConnStatus.TRYING) trying = true;
            }
        }
        if(trying == true) return ConnStatus.TRYING;
        return ConnStatus.FAILED;
    }

    public ConnChecker[] getCheckers(){
        ConnChecker[] checkers = new ConnChecker[connCheckers.size()];
        return (ConnChecker[])connCheckers.toArray(checkers);
    }

    private List connCheckers = Collections.synchronizedList(new ArrayList());

    static Category logger = Category.getInstance(Checker.class);

    private WorkerThreadPool pool = new WorkerThreadPool("Connection Checker Pool", 20);

    private ThreadGroup checkerThreadGroup = new ThreadGroup("Connection Checker");
}// Checker class
