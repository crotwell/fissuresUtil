package edu.sc.seis.fissuresUtil.display;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.iris.Fissures.IfSeismogramDC.LocalSeismogram;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.TimeRange;
import edu.sc.seis.fissuresUtil.chooser.DataSetChannelGrouper;
import edu.sc.seis.fissuresUtil.xml.*;
/**
 * ComponentSortedSeismogramDisplay.java
 *
 *
 * Created: Tue Nov 12 13:49:33 2002
 *
 * @author <a href="mailto:">Charlie Groves</a>
 * @version
 */

public class ComponentSortedSeismogramDisplay extends VerticalSeismogramDisplay {
    /**
     * Creates a <code>ComponentSortedSeismogramDisplay</code> without a parent
     *
     * @param mouseForwarder the object every contained BSD forwards its mouse events to
     * @param motionForwarder the object every contained BSD forwards its mouse motion events to
     */
    public ComponentSortedSeismogramDisplay(MouseForwarder mouseForwarder, MouseMotionForwarder motionForwarder){
	this(mouseForwarder, motionForwarder, null);
    }

    /**
     * Creates a <code>ComponentSortedSeismogramDisplay</code> with the passed in parent controlling it
     *
     * @param mouseForwarder the object every contained BSD forwards its mouse events to
     * @param motionForwarder the object every contained BSD forwards its mouse motion events to
     * @param parent the VSD that controls this VSD
     */
    public ComponentSortedSeismogramDisplay(MouseForwarder mouseForwarder, MouseMotionForwarder motionForwarder, 
				     VerticalSeismogramDisplay parent){
	super(mouseForwarder, motionForwarder, parent);
    }

    public BasicSeismogramDisplay addDisplay(DataSetSeismogram[] dss){
	return addDisplay(dss, DisplayUtils.getSeismogramNames(dss));
    }

    /**
     * finds all three components of the passed seismograms and adds them to their respective component displays 
     * with an individual RMeanAmpConfig and the global time registrar for
     *
     * @param dss the seismograms for the new BSD
     * @param names suggested names for the seismograms
     * @return the created BSD
     */
    public BasicSeismogramDisplay addDisplay(DataSetSeismogram[] dss, String[] names){
	return addDisplay(dss, globalRegistrar, new RMeanAmpConfig(dss), names);
    }

    /**
     *  finds all three components of the passed seismograms and adds them to their respective component displays with an 
     * individual RMeanAmpConfig and the passed in TImeConfig
     *
     * @param dss the seismograms for the new BSD
     * @param tc the time config for the new BSD
     * @param names suggested names for the seismograms
     * @return the created BSD
     */
    public BasicSeismogramDisplay addDisplay(DataSetSeismogram[] dss, TimeConfig tc, String[] names){
	return addDisplay(dss, tc, new RMeanAmpConfig(dss), names);
    }
    
     /**
     *  finds all three components of the passed seismograms and adds them to their respective component displays 
     * with the passed in amp config and the global TImeConfig
     *
     * @param dss the seismograms for the new BSD
     * @param ac the amp config for the new BSD
     * @param names suggested names for the seismograms
     * @return the created BSD
     */
    public BasicSeismogramDisplay addDisplay(DataSetSeismogram[] dss, AmpConfig ac, String[] names){
	return addDisplay(dss, globalRegistrar, ac, names);
    }
   
    /**
     * finds all three components of the passed seismograms and adds them to their respective component display
     *  with the passed in amp and time configs 
     *
     * @param dss the seismograms for the new BSD
     * @param tc the time config for the new BSD
     * @param ac the amp config for the new BSD
     * @param names suggested names for the seismograms
     * @return the created BSD
     * @return a <code>BasicSeismogramDisplay</code> value
     */ 
   
    public BasicSeismogramDisplay addDisplay(DataSetSeismogram[] dss, TimeConfig tc, AmpConfig ac, String[] names){
	if(tc == globalRegistrar && globalRegistrar == null){
	    globalRegistrar = new Registrar(dss);
	    tc = globalRegistrar;
	}
	List seismogramNames = getSeismogramNames();
	List newNorth = new ArrayList();
	List newEast = new ArrayList();
	List newZ = new ArrayList();
	List northNames = new ArrayList();
	List eastNames = new ArrayList();
	List zNames = new ArrayList();
	for(int i = 0; i < dss.length; i++){
	    if(!seismogramNames.contains(dss[i].getSeismogram().getName())){
		LocalSeismogramImpl seis = dss[i].getSeismogram();
		String suffix = getSuffix(dss[i], names[i]);
		XMLDataSet dataSet = (XMLDataSet)dss[i].getDataSet();
		ChannelId[] channelGroup = DataSetChannelGrouper.retrieveGrouping(dataSet, seis.getChannelID());
		for(int counter = 0; counter < channelGroup.length; counter++) {
		    LocalSeismogram[] newSeismograms  = DisplayUtils.getSeismogram(channelGroup[counter], dataSet, 
										   new TimeRange(seis.getBeginTime().getFissuresTime(), 
												 seis.getEndTime().getFissuresTime()));
		    for(int j = 0; j < newSeismograms.length; j++){
			DataSetSeismogram current = new DataSetSeismogram((LocalSeismogramImpl)newSeismograms[j], dataSet);
			if(DisplayUtils.getOrientationName(channelGroup[counter].channel_code).equals("North")){
			    newNorth.add(current);
			    northNames.add(current.getSeismogram().getName()+suffix);
			}else if(DisplayUtils.getOrientationName(channelGroup[counter].channel_code).equals("East")){
			    newEast.add(current);
			    eastNames.add(current.getSeismogram().getName()+suffix);
			}else{
			    newZ.add(current);
			    zNames.add(current.getSeismogram().getName()+suffix);
			}
			seismogramNames.add(current.getSeismogram().getName()+suffix);
		    }
		}
	    }
	}
	DataSetSeismogram[] northDss = ((DataSetSeismogram[])newNorth.toArray(new DataSetSeismogram[newNorth.size()]));
   	if(newNorth.size() > 0){
	    String[] northArrayNames = ((String[])northNames.toArray(new String[northNames.size()]));
	    if(north == null){
		north = new BasicSeismogramDisplay(northDss, tc, ac, northArrayNames, this);
		initializeBSD(north, 0);
	    }else{
		north.add(northDss, northArrayNames);
	    }
	}
	DataSetSeismogram[] eastDss = ((DataSetSeismogram[])newEast.toArray(new DataSetSeismogram[newEast.size()]));
	if(newEast.size() > 0){
	    String[] eastArrayNames = ((String[])eastNames.toArray(new String[eastNames.size()]));
	    if(east == null){
		east = new BasicSeismogramDisplay(eastDss, tc, ac, eastArrayNames, this);
		if(north == null){
		    initializeBSD(east, 0);
		}else{
		    initializeBSD(east, 1);
		}
	    }else{
		east.add(eastDss, eastArrayNames);
	    }
	}
	DataSetSeismogram[] zDss = ((DataSetSeismogram[])newZ.toArray(new DataSetSeismogram[newZ.size()]));
	if(newZ.size() > 0){
	    String[] zArrayNames = ((String[])zNames.toArray(new String[zNames.size()]));
	    if(z == null){
		z = new BasicSeismogramDisplay(zDss, tc, ac, zArrayNames, this);
		initializeBSD(z, -1);
	    }else{
		z.add(zDss, zArrayNames);
	    }
	}
	recentlyAddedSeismograms = new DataSetSeismogram[northDss.length + eastDss.length + zDss.length];
	System.arraycopy(northDss, 0, recentlyAddedSeismograms, 0, northDss.length);
	System.arraycopy(eastDss, 0, recentlyAddedSeismograms, northDss.length, eastDss.length);
	System.arraycopy(zDss, 0, recentlyAddedSeismograms, northDss.length + eastDss.length, zDss.length);
	return north;
    } 

    public DataSetSeismogram[] getRecentlyAddedSeismograms(){
	return recentlyAddedSeismograms; 
    }

    private void initializeBSD(BasicSeismogramDisplay disp, int position){
	super.add(disp, position);
	disp.addMouseMotionListener(motionForwarder);
	disp.addMouseListener(mouseForwarder);
	disp.addBottomTimeBorder();
	disp.addTopTimeBorder();
	basicDisplays.add(disp);
    }

    public boolean removeDisplay(BasicSeismogramDisplay display){
	if(display == north){
	    north = null;
	}else if(display == east){
	    east = null;
	}else{
	    z = null;
	}
	return super.removeDisplay(display);
    }

    private static String getSuffix(DataSetSeismogram seismogram, String name){
	return name.substring(seismogram.getSeismogram().getName().length());
    }
    
    private DataSetSeismogram[] recentlyAddedSeismograms;

    private BasicSeismogramDisplay north, east, z;
}// ComponentSortedSeismogramDisplay
