/**
 * InstrumentationLoader.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.cache;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import org.apache.log4j.Logger;
import edu.iris.Fissures.IfNetwork.ChannelNotFound;
import edu.iris.Fissures.IfNetwork.Instrumentation;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;
import edu.sc.seis.fissuresUtil.xml.StdAuxillaryDataNames;

public class InstrumentationLoader extends Thread
{

    public InstrumentationLoader() {
        super("InstrumentationLoader");
    }

    public void run() {
        while (noStopThread) {
            WorkUnit nextWork = null;
            try {
                nextWork = getFromQueue();
                Instrumentation inst =
                nextWork.net.retrieve_instrumentation(nextWork.seis.getRequestFilter().channel_id,
                                                      nextWork.seis.getBeginTime());
                nextWork.seis.addAuxillaryData(StdAuxillaryDataNames.RESPONSE,
                                  inst.the_response);
                logger.debug("added response to dss for "+
                                 ChannelIdUtil.toStringNoDates(nextWork.seis.getRequestFilter().channel_id));
            } catch (ChannelNotFound e) {
                GlobalExceptionHandler.handle("Could not load instrumentation for channel "+
                                ChannelIdUtil.toString(nextWork.seis.getRequestFilter().channel_id)+
                                " at "+nextWork.seis.getBeginTime().date_time,
                            e);
            } catch (org.omg.CORBA.SystemException e) {
                // might be a transient??? Try again if not too many tries
                nextWork.numTries++;
                if (nextWork.numTries < 10) {
                    addToQueue(nextWork);
                }
            } catch (Throwable e) {
                GlobalExceptionHandler.handle("A problem occured loading the instrumentation for channel "+
                                ChannelIdUtil.toString(nextWork.seis.getRequestFilter().channel_id),
                            e);
            }
        }
    }

    protected synchronized boolean isEmpty() {
        return queue.isEmpty();
    }

    protected synchronized WorkUnit getFromQueue()
    throws InterruptedException
    {
        while (queue.isEmpty()) {
            wait();
        }
        // sort so high numbered retrys are at begining and grab last
        // which has lowest retries
        Collections.sort(queue, numTrysComparator);
        Collections.reverse(queue);
        return (WorkUnit)queue.removeLast();
    }

    protected synchronized void addToQueue(WorkUnit work) {
        logger.debug("adding work");
        queue.addFirst(work);
        notifyAll();
    }

    boolean noStopThread = true;

    NumTrysComparator numTrysComparator = new NumTrysComparator();

    public void getInstrumentation(DataSetSeismogram seis, NetworkAccess net) {
        addToQueue(new WorkUnit(seis, net));
    }

    private LinkedList queue = new LinkedList();

    /** just hold a work unit for putting in the list */
    class WorkUnit {
        WorkUnit(DataSetSeismogram seis, NetworkAccess net) {
            this.seis = seis;
            this.net = net;
        }
        DataSetSeismogram seis;
        NetworkAccess net;
        int numTries = 0;
    }

    class NumTrysComparator implements Comparator {
        /**
         * @param o1 the first object to be compared.
         * @param o2 the second object to be compared.
         * @return a negative integer, zero, or a positive integer as the
         *         first argument is less than, equal to, or greater than the
         *         second.
         * @throws ClassCastException if the arguments' types prevent them from
         *         being compared by this Comparator.
         */
        public int compare(Object o1, Object o2) {
            WorkUnit wu1 = (WorkUnit)o1;
            WorkUnit wu2 = (WorkUnit)o2;
            if (wu1.numTries < wu2.numTries) return -1;
            if (wu1.numTries > wu2.numTries) return 1;
            return 0;
        }

    }

    static Logger logger = Logger.getLogger(InstrumentationLoader.class);
 }

