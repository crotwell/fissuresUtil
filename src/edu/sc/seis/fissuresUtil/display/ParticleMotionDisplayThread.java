package edu.sc.seis.fissuresUtil.display;


import edu.sc.seis.TauP.*;
import edu.sc.seis.fissuresUtil.chooser.*;
import edu.sc.seis.fissuresUtil.namingService.*;
import edu.iris.Fissures.IfSeismogramDC.*;
import edu.iris.Fissures.seismogramDC.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.model.*;
import edu.iris.Fissures.network.*;

import java.util.*;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;

import org.apache.log4j.*;

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
    public ParticleMotionDisplayThread (DataSetSeismogram dataSetSeismogram,
					TimeConfigRegistrar timeConfigRegistrar,
					AmpConfigRegistrar hAmpConfigRegistrar,
					AmpConfigRegistrar vAmpConfigRegistrar,
					boolean advancedOption,
					boolean displayButtonPanel,
					ParticleMotionDisplay particleMotionDisplay) {

	this.dataSetSeismogram = new DataSetSeismogram[1];
	this.dataSetSeismogram[0] = dataSetSeismogram;
	this.timeConfigRegistrar = timeConfigRegistrar;
	this.hAmpConfigRegistrar = hAmpConfigRegistrar;
	this.vAmpConfigRegistrar = vAmpConfigRegistrar;
	this.advancedOption = advancedOption;
	this.displayButtonPanel = displayButtonPanel;
	this.particleMotionDisplay = particleMotionDisplay;
	
    }

    public ParticleMotionDisplayThread(DataSetSeismogram[] dataSetSeismogram,
				       TimeConfigRegistrar timeConfigRegistrar,
				       AmpConfigRegistrar hAmpConfigRegistrar,
				       AmpConfigRegistrar vAmpConfigRegistrar,
				       boolean advancedOption,
				       boolean displayButtonPanel,
				       ParticleMotionDisplay particleMotionDisplay) {
	
	
	this.dataSetSeismogram = new DataSetSeismogram[dataSetSeismogram.length];
	System.arraycopy(dataSetSeismogram,
			 0,
			 this.dataSetSeismogram,
			 0,
			 dataSetSeismogram.length);
	this.timeConfigRegistrar = timeConfigRegistrar;
	this.hAmpConfigRegistrar = hAmpConfigRegistrar;
	this.vAmpConfigRegistrar = vAmpConfigRegistrar;
	this.advancedOption = advancedOption;
	this.displayButtonPanel = displayButtonPanel;
	this.particleMotionDisplay = particleMotionDisplay;
    }


    public void execute() {


	if(dataSetSeismogram.length == 1) {
	    dataSetSeismogram = retrieve_seismograms();
	} else {
	    channelGroup = new ChannelId[dataSetSeismogram.length];
	    for(int counter = 0; counter < dataSetSeismogram.length; counter++) {

		channelGroup[counter] = dataSetSeismogram[counter].getSeismogram().getChannelID();
	    }
	}


	//decide whether to form the radioSetPanel or the checkBoxPanel.
	if(displayButtonPanel) {
	    if(!advancedOption) {
		particleMotionDisplay.formRadioSetPanel(channelGroup);
	    } else {
		particleMotionDisplay.formCheckBoxPanel(channelGroup);
	    }
	}

	for(int counter = 0; counter < dataSetSeismogram.length; counter++) {

	    for(int subcounter = counter+1; subcounter < dataSetSeismogram.length; subcounter++) {
		
		boolean horizPlane = isHorizontalPlane(dataSetSeismogram[counter].getSeismogram().getChannelID(),
						       dataSetSeismogram[subcounter].getSeismogram().getChannelID(),
						       dataSetSeismogram[counter].getDataSet());
		if(horizPlane) {
		    particleMotionDisplay.displayBackAzimuth(dataSetSeismogram[counter].getDataSet(), channelGroup[counter]);
		}
		particleMotionDisplay.getView().addParticleMotionDisplay(dataSetSeismogram[counter], 
							       dataSetSeismogram[subcounter], 
							       timeConfigRegistrar, 
							       hAmpConfigRegistrar, 
							       vAmpConfigRegistrar, 
							       null,
							       getOrientationName(channelGroup[counter].channel_code)+"-"+
							       getOrientationName(channelGroup[subcounter].channel_code),
							       horizPlane);
		particleMotionDisplay.updateTimeRange();
		
	    }
	}
	if(displayButtonPanel) {
	    particleMotionDisplay.setInitialButton();
	}

    }

    public DataSetSeismogram[] retrieve_seismograms() {
	LocalSeismogramImpl seis = dataSetSeismogram[0].getSeismogram();
	Date chanIdStartTime = Calendar.getInstance().getTime();
	ChannelId[] channelIds = ((edu.sc.seis.fissuresUtil.xml.XMLDataSet)dataSetSeismogram[0].getDataSet()).getChannelIds();
	Date chanIdendTime = Calendar.getInstance().getTime();
	logger.debug(" Time for CHan ID is "+(chanIdendTime.getTime() - chanIdStartTime.getTime()));
	
	Date channelGroupStartTime = Calendar.getInstance().getTime();
	ChannelGrouperImpl channelProxy = new ChannelGrouperImpl();
	logger.debug("the original channel_code from the seismogram is "+seis.getChannelID().channel_code);
	 channelGroup = channelProxy.retrieve_grouping(channelIds, seis.getChannelID());
	 Date channelGroupEndTime = Calendar.getInstance().getTime();
	 logger.debug(" Time for Chan Grouper is "+(channelGroupEndTime.getTime() - channelGroupStartTime.getTime()));
	logger.debug("THe length of the channel group is "+channelGroup.length);




	edu.iris.Fissures.Time startTime;
	edu.iris.Fissures.Time endTime;
	DataSetSeismogram[] seismograms = new DataSetSeismogram[3];
	if(timeConfigRegistrar != null) {
	    startTime = timeConfigRegistrar.getTimeRange().getBeginTime().getFissuresTime();
	    endTime = timeConfigRegistrar.getTimeRange().getEndTime().getFissuresTime();
	} else {
	    startTime = seis.getBeginTime().getFissuresTime();
	    endTime = seis.getEndTime().getFissuresTime();
	}
	try {
	    for(int counter = 0; counter < channelGroup.length; counter++) {
		
		seismograms[counter] = new DataSetSeismogram(dataSetSeismogram[0].getDataSet().
							     getSeismogram(DisplayUtils.getSeismogramName(channelGroup[counter], 
													  dataSetSeismogram[0].getDataSet(),
													  new edu.iris.Fissures.TimeRange(seis.getBeginTime().getFissuresTime(), seis.getEndTime().getFissuresTime()))), dataSetSeismogram[0].getDataSet());
		//ChannelIdUtil.toStringNoDates(channelGroup[counter]));
		timeConfigRegistrar.addSeismogram(seismograms[counter]);
		//hAmpRangeConfigRegistrar.addSeismogram(seismograms[counter]);
		
	    }
	    return seismograms;
	    
	} catch(Exception e) {
	    
	    e.printStackTrace();//strack trace
	}
	return new DataSetSeismogram[0];
	

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


    public String getOrientationName(String orientation) {

	char ch = orientation.charAt(2);
	if(ch == 'E' || ch == '1' || ch == 'U') return "East";
	else if(ch == 'N' || ch == '2' || ch == 'V') return "North";
	else return "Up";
    }
    
  
    
    

    private DataSetSeismogram[] dataSetSeismogram = new DataSetSeismogram[0];
    
    private TimeConfigRegistrar timeConfigRegistrar;

    private AmpConfigRegistrar hAmpConfigRegistrar;
    
    private  AmpConfigRegistrar vAmpConfigRegistrar;

    private boolean advancedOption = false;

    private boolean displayButtonPanel = false;

    private ParticleMotionDisplay  particleMotionDisplay;

    private ChannelId[] channelGroup;

    static Category logger = 
     Category.getInstance(ParticleMotionDisplayThread.class.getName());
    
}// ParticleMotionDisplayThread
