package edu.sc.seis.fissuresUtil.display;

import java.awt.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.sc.seis.fissuresUtil.display.registrar.TimeConfig;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;

/**
 * ParticleMotionDisplayThread.java
 * 
 * 
 * Created: Fri Jul 26 12:42:56 2002
 * 
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */
public class ParticleMotionDisplayThread {

    public ParticleMotionDisplayThread(DataSetSeismogram dataSetSeismogram,
                                       TimeConfig tc,
                                       ParticleMotionDisplay particleMotionDisplay,
                                       Color displayColor) {
        this.dataSetSeismogram = dataSetSeismogram;
        this.tc = tc;
        this.particleMotionDisplay = particleMotionDisplay;
        this.displayColor = displayColor;
    }

    public void execute() {
        dssArray = DisplayUtils.getComponents(dataSetSeismogram);
        if(dssArray.length < 2) {// in case only one component was available
            completion = false;
            return;
        }
        ChannelId[] channelGroup = new ChannelId[dssArray.length];
        for(int counter = 0; counter < dssArray.length; counter++) {
            channelGroup[counter] = dssArray[counter].getRequestFilter().channel_id;
        }
        for(int counter = 0; counter < dssArray.length; counter++) {
            String counterOrientation = DisplayUtils.getOrientationName(channelGroup[counter].channel_code);
            for(int subcounter = counter + 1; subcounter < dssArray.length; subcounter++) {
                boolean horizPlane = isHorizontalPlane(dssArray[counter].getChannel(),
                                                       dssArray[subcounter].getChannel());
                String subCounterOrientation = DisplayUtils.getOrientationName(channelGroup[subcounter].channel_code);
                String orientationString;
                DataSetSeismogram hSeis, vSeis;
                // From display util determined orientation, lay the seismograms
                // out in the particle motion view
                if(counterOrientation.equals(DisplayUtils.UP)) {// Up always
                    // goes vertical
                    vSeis = dssArray[counter];
                    hSeis = dssArray[subcounter];
                    orientationString = counterOrientation + "-"
                            + subCounterOrientation;
                } else if(counterOrientation.equals(DisplayUtils.EAST)) {// East
                    // always
                    // goes
                    // horizontal
                    vSeis = dssArray[subcounter];
                    hSeis = dssArray[counter];
                    orientationString = subCounterOrientation + "-"
                            + counterOrientation;
                } else {// counterOrientation must be North
                    if(subCounterOrientation.equals(DisplayUtils.EAST)) {// North
                        // goes
                        // vertical
                        // with
                        // East
                        vSeis = dssArray[counter];
                        hSeis = dssArray[subcounter];
                        orientationString = counterOrientation + "-"
                                + subCounterOrientation;
                    } else {// North goes horizontal with Up
                        vSeis = dssArray[subcounter];
                        hSeis = dssArray[counter];
                        orientationString = subCounterOrientation + "-"
                                + counterOrientation;
                    }
                }
                particleMotionDisplay.getView().add(hSeis,
                                                    vSeis,
                                                    tc,
                                                    displayColor,
                                                    orientationString,
                                                    horizPlane);
            }
        }
        completion = true;
    }

    public boolean isHorizontalPlane(Channel one, Channel two) {
        return one.getOrientation().dip == 0 && two.getOrientation().dip == 0;
    }

    /**
     * @returns true if the execution step of the thread finishes correctly.
     * 
     */
    public boolean getCompletion() {
        return completion;
    }

    private boolean completion = false;

    private DataSetSeismogram dataSetSeismogram;

    private DataSetSeismogram[] dssArray;

    private TimeConfig tc;

    private ParticleMotionDisplay particleMotionDisplay;

    private Color displayColor;

    private static Logger logger = LoggerFactory.getLogger(ParticleMotionDisplayThread.class.getName());
}// ParticleMotionDisplayThread
