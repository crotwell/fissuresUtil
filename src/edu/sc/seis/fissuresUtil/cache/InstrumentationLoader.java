/**
 * InstrumentationLoader.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.cache;

import edu.iris.Fissures.IfNetwork.ChannelNotFound;
import edu.iris.Fissures.IfNetwork.Instrumentation;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.sc.seis.fissuresUtil.exceptionHandlerGUI.GlobalExceptionHandler;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;
import edu.sc.seis.fissuresUtil.xml.StdAuxillaryDataNames;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.apache.log4j.Logger;

public class InstrumentationLoader extends Thread
{

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
                logger.debug("added response to dss");
            } catch (ChannelNotFound e) {
                GlobalExceptionHandler.handleStatic("Could not load instrumentation for channel "+
                                ChannelIdUtil.toString(nextWork.seis.getRequestFilter().channel_id),
                            e);
            } catch (Exception e) {
                GlobalExceptionHandler.handleStatic("A problem occured loading the instrumentation for channel "+
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
        return (WorkUnit)queue.removeLast();
    }

    protected synchronized void addToQueue(WorkUnit work) {
        logger.debug("adding work");
        queue.addFirst(work);
        notifyAll();
    }

    boolean noStopThread = true;

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
    }

    static Logger logger = Logger.getLogger(InstrumentationLoader.class);
 }

