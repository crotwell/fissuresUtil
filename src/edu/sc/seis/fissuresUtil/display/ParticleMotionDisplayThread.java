package edu.sc.seis.fissuresUtil.display;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.sc.seis.fissuresUtil.display.registrar.Registrar;
import edu.sc.seis.fissuresUtil.xml.DataSet;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;
import java.awt.Color;
import org.apache.log4j.Category;

/**
 * ParticleMotionDisplayThread.java
 *
 *
 * Created: Fri Jul 26 12:42:56 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class ParticleMotionDisplayThread{
    public ParticleMotionDisplayThread(DataSetSeismogram dataSetSeismogram,
                                       Registrar registrar,
                                       ParticleMotionDisplay particleMotionDisplay) {
        this.dataSetSeismogram = dataSetSeismogram;
        this.registrar = registrar;
        this.particleMotionDisplay = particleMotionDisplay;
    }


    public void execute() {
        dssArray = DisplayUtils.getComponents(dataSetSeismogram);
        if(dssArray.length < 3){// in case all the components aren't available, the thread fails
            completion = false;
            return;
        }

        ChannelId[] channelGroup = new ChannelId[dssArray.length];
        for(int counter = 0; counter < dssArray.length; counter++) {
            channelGroup[counter] = dssArray[counter].getRequestFilter().channel_id;
        }
        Color displayColor = selectionColors[particleMotionDisplay.getView().getSelectedParticleMotion().length % selectionColors.length];

        for(int counter = 0; counter < dssArray.length; counter++) {
            String counterOrientation = DisplayUtils.getOrientationName(channelGroup[counter].channel_code);
            for(int subcounter = counter+1; subcounter < dssArray.length; subcounter++) {
                boolean horizPlane = isHorizontalPlane(dssArray[counter].getRequestFilter().channel_id,
                                                       dssArray[subcounter].getRequestFilter().channel_id,
                                                       dssArray[counter].getDataSet());
                if(horizPlane) {
                    particleMotionDisplay.displayBackAzimuth(dssArray[counter].getDataSet(), channelGroup[counter]);
                }
                String subCounterOrientation = DisplayUtils.getOrientationName(channelGroup[subcounter].channel_code);
                String orientationString;
                DataSetSeismogram hSeis,vSeis;
                //From display util determined orientation, lay the seismograms
                //out in the particle motion view
                if(counterOrientation.equals(DisplayUtils.UP)){//Up always goes vertical
                    vSeis = dssArray[counter];
                    hSeis = dssArray[subcounter];
                    orientationString = counterOrientation + "-" + subCounterOrientation;
                }else if(counterOrientation.equals(DisplayUtils.EAST)){//East always goes horizontal
                    vSeis = dssArray[subcounter];
                    hSeis = dssArray[counter];
                    orientationString = subCounterOrientation + "-" + counterOrientation;
                }else{//counterOrientation must be North
                    if(subCounterOrientation.equals(DisplayUtils.EAST)){//North goes vertical with East
                        vSeis = dssArray[counter];
                        hSeis = dssArray[subcounter];
                        orientationString = DisplayUtils.NORTHEAST;
                    }else{//North goes horizontal with Up
                        vSeis = dssArray[subcounter];
                        hSeis = dssArray[counter];
                        orientationString = DisplayUtils.UPNORTH;
                    }
                }
                particleMotionDisplay.getView().addParticleMotionDisplay(hSeis,
                                                                         vSeis,
                                                                         registrar,
                                                                         displayColor,
                                                                         orientationString,
                                                                         horizPlane);
            }
        }
        if(displayButtonPanel) {
            particleMotionDisplay.setInitialButton();
        }
        completion = true;

    }

    public boolean isHorizontalPlane(ChannelId channelIdone,
                                     ChannelId channelIdtwo,
                                     DataSet dataset) {

        if(dataset.getChannel(channelIdone).an_orientation.dip == 0 &&
           dataset.getChannel(channelIdtwo).an_orientation.dip == 0)  {
            return true;
        }
        return false;
    }

    /**
     *@returns true if the execution step of the thread finishes correctly.
     *
     */
    public boolean getCompletion(){ return completion; }

    private boolean completion = false;

    private DataSetSeismogram dataSetSeismogram;

    private DataSetSeismogram[] dssArray;

    private Registrar registrar;

    private boolean advancedOption = false;

    private boolean displayButtonPanel = false;

    private ParticleMotionDisplay  particleMotionDisplay;

    private static Color[] selectionColors = { new Color(255, 0, 0),
            new Color(0, 0, 255),
            Color.magenta,
            Color.cyan,
            Color.white,
            Color.black};

    static Category logger =
        Category.getInstance(ParticleMotionDisplayThread.class.getName());

}// ParticleMotionDisplayThread
