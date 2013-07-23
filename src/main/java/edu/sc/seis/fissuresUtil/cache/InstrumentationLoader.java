/**
 * InstrumentationLoader.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.cache;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.iris.Fissures.IfNetwork.ChannelNotFound;
import edu.iris.Fissures.IfNetwork.FilterType;
import edu.iris.Fissures.IfNetwork.Instrumentation;
import edu.iris.Fissures.IfNetwork.Response;
import edu.iris.Fissures.IfNetwork.Sensitivity;
import edu.iris.Fissures.IfNetwork.Stage;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.network.ChannelImpl;
import edu.sc.seis.fissuresUtil.chooser.ChannelChooser;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.fissuresUtil.sac.InvalidResponse;
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
                nextWork.net.getSource(nextWork.seis.getChannelId().network_id).getInstrumentation((ChannelImpl)nextWork.net.getChannel(nextWork.seis.getRequestFilter().channel_id));
                nextWork.seis.addAuxillaryData(StdAuxillaryDataNames.RESPONSE,
                                  inst.the_response);
                logger.debug("added response to dss for "+
                                 ChannelIdUtil.toStringNoDates(nextWork.seis.getRequestFilter().channel_id));
            } catch (ChannelNotFound e) {
                logger.debug("Could not load instrumentation for channel "+
                             ChannelIdUtil.toString(nextWork.seis.getRequestFilter().channel_id)+
                             " at "+nextWork.seis.getBeginTime().date_time);
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
    
    /**
     * Checks for nonsense sensitivity (overall gain of -1) and trys to repair by multiplying the
     * gains of the individual stages. This only works if all the frequencys are either the same
     * or zero. We assume a frequency of zero means that there is no frequnecy dependence for this
     * stage. 
     */
    public static void repairResponse(Response resp) throws InvalidResponse {
        if(isValid(resp)) {
            return;
        }
        logger.info("response is not valid, repairing");
        Stage[] stages = resp.stages;
        float sensitivity = stages[0].the_gain.gain_factor;
        for(int i = 1; i < stages.length; i++) {
            // assume that a stage with frequency 0 means it has no frequency dependence
            if(stages[i - 1].the_gain.frequency != stages[i].the_gain.frequency && stages[i].the_gain.frequency != 0) {
                throw new InvalidResponse("No sensitivity and different frequencies in the stages of the response. Stage 0="+stages[0].the_gain.frequency+"  stage "+i+"= "+stages[i].the_gain.frequency);
            }
            sensitivity *= stages[i].the_gain.gain_factor;
        }
        resp.the_sensitivity.sensitivity_factor = sensitivity;
        resp.the_sensitivity.frequency = stages[0].the_gain.frequency;
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

    public void getInstrumentation(DataSetSeismogram seis, ChannelChooser channelChooser) {
        addToQueue(new WorkUnit(seis, channelChooser));
    }

    public static boolean isValidSensitivity(QuantityImpl sens) {
        return sens.getValue() != -1;
    }
    
    public static boolean isValid(Sensitivity sens) {
        return sens.frequency >= 0 && sens.sensitivity_factor != -1;
    }

    public static boolean isValid(Instrumentation inst) {
        return isValid(inst.the_response);
    }
    
    public static boolean isValid(Response resp) {
        return resp.stages.length != 0 && isValid(resp.the_sensitivity);
    }
    
    public static void checkResponse(Response resp) throws InvalidResponse {
        if (!InstrumentationLoader.isValid(resp)) {
            if (resp.stages.length == 0) {
                throw new InvalidResponse("response is not valid, zero stages");
            } else if (resp.the_sensitivity.frequency < 0) {
                throw new InvalidResponse("response is not valid, sensitivity frequency negative");
            } else if (resp.the_sensitivity.sensitivity_factor == -1) {
                throw new InvalidResponse("response is not valid, sensitivity factor = -1");
            } else if (resp.stages[0].the_gain.gain_factor == 1 && 
                    resp.stages[0].filters[0].discriminator().equals(FilterType.POLEZERO) &&
                    resp.stages[0].filters[0].pole_zero_filter().poles.length == 0 &&
                    resp.stages[0].filters[0].pole_zero_filter().zeros.length == 0 ) {
                throw new InvalidResponse("response is not valid, stage[0] gain = 1, no poles, no zeros, marker for \"UNKNOWN\"");
            }
        }
    }

    private LinkedList queue = new LinkedList();

    /** just hold a work unit for putting in the list */
    class WorkUnit {
        WorkUnit(DataSetSeismogram seis, ChannelChooser net2) {
            this.seis = seis;
            this.net = net2;
        }
        DataSetSeismogram seis;
        ChannelChooser net;
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

    static Logger logger = LoggerFactory.getLogger(InstrumentationLoader.class);
 }

