package edu.sc.seis.fissuresUtil.display;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.chooser.ChannelGrouperImpl;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;
import java.awt.Color;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
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
                                       boolean advancedOption,
                                       boolean displayButtonPanel,
                                       ParticleMotionDisplay particleMotionDisplay) {
        this.dataSetSeismogram = dataSetSeismogram;
        this.registrar = registrar;
        this.advancedOption = advancedOption;
        this.displayButtonPanel = displayButtonPanel;
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

        //decide whether to form the radioSetPanel or the checkBoxPanel.
        if(displayButtonPanel) {
            if(!advancedOption) {
                particleMotionDisplay.formRadioSetPanel(channelGroup);
            } else {
                particleMotionDisplay.formCheckBoxPanel(channelGroup);
            }

        }
        Color displayColor = selectionColors[particleMotionDisplay.getView().getSelectedParticleMotion().length % selectionColors.length];

        for(int counter = 0; counter < dssArray.length; counter++) {
            for(int subcounter = counter+1; subcounter < dssArray.length; subcounter++) {

                boolean horizPlane = isHorizontalPlane(dssArray[counter].getRequestFilter().channel_id,
                                                       dssArray[subcounter].getRequestFilter().channel_id,
                                                       dssArray[counter].getDataSet());
                if(horizPlane) {
                    particleMotionDisplay.displayBackAzimuth(dssArray[counter].getDataSet(), channelGroup[counter]);
                }
                particleMotionDisplay.getView().addParticleMotionDisplay(dssArray[counter],
                                                                         dssArray[subcounter],
                                                                         registrar,
                                                                         displayColor,
                                                                         DisplayUtils.getOrientationName(channelGroup[counter].channel_code)+"-"+
                                                                         DisplayUtils.getOrientationName(channelGroup[subcounter].channel_code),
                                                                         horizPlane);
                //particleMotionDisplay.updateTimeRange();

            }
        }
        if(displayButtonPanel) {
            particleMotionDisplay.setInitialButton();
        }
        completion = true;

    }

    public boolean isHorizontalPlane(ChannelId channelIdone,
                                     ChannelId channelIdtwo,
                                     edu.sc.seis.fissuresUtil.xml.DataSet dataset) {

        Channel channelOne = ((edu.sc.seis.fissuresUtil.xml.XMLDataSet)dataset).getChannel(channelIdone);
        Channel channelTwo = ((edu.sc.seis.fissuresUtil.xml.XMLDataSet)dataset).getChannel(channelIdtwo);
        if((Math.abs(channelOne.an_orientation.dip) == 0 &&
            channelTwo.an_orientation.dip == 0))  {
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
