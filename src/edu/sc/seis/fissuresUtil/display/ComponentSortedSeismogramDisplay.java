package edu.sc.seis.fissuresUtil.display;
import edu.sc.seis.fissuresUtil.display.registrar.AmpConfig;
import edu.sc.seis.fissuresUtil.display.registrar.IndividualizedAmpConfig;
import edu.sc.seis.fissuresUtil.display.registrar.RMeanAmpConfig;
import edu.sc.seis.fissuresUtil.display.registrar.TimeConfig;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;
import java.util.Iterator;

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
     * finds all three components of the passed seismograms and adds them to their respective component displays
     * with an individual RMeanAmpConfig and the global time registrar for each component display
     *
     * @param dss the seismograms for the new BSD
     * @return the created BSD
     */
    public BasicSeismogramDisplay addDisplay(DataSetSeismogram[] dss){
        return addDisplay(dss, tc, new RMeanAmpConfig(dss));
    }

    /**
     *  finds all three components of the passed seismograms and adds them to their respective component displays with an
     * individual RMeanAmpConfig and the passed in TImeConfig
     *
     * @param dss the seismograms for the new BSD
     * @param tc the time config for the new BSD
     * @return the created BSD
     */
    public BasicSeismogramDisplay addDisplay(DataSetSeismogram[] dss, TimeConfig tc){
        return addDisplay(dss, tc, new RMeanAmpConfig(dss));
    }

    /**
     *  finds all three components of the passed seismograms and adds them to their respective component displays
     * with the passed in amp config and the global TImeConfig
     *
     * @param dss the seismograms for the new BSD
     * @param ac the amp config for the new BSD
     * @return the created BSD
     */
    public BasicSeismogramDisplay addDisplay(DataSetSeismogram[] dss, AmpConfig ac){
        return addDisplay(dss, tc, ac);
    }

    /**
     * finds all three components of the passed seismograms and adds them to their respective component display
     *  with the passed in amp and time configs
     *
     * @param dss the seismograms for the new BSD
     * @param tc the time config for the new BSD
     * @param ac the amp config for the new BSD
     * @return the created BSD
     */
    public BasicSeismogramDisplay addDisplay(DataSetSeismogram[] dss, TimeConfig tc, AmpConfig ac){
        DataSetSeismogram[][] componentSorted = DisplayUtils.sortByComponents(dss);
        addNorth(componentSorted[0], tc);
        addEast(componentSorted[1], tc);
        addZ(componentSorted[2], tc);
        return north;
    }

    private void addNorth(DataSetSeismogram[] newNorth, TimeConfig tc){
        north = addToDisplay(north, newNorth, tc,DisplayUtils.NORTH);
    }

    private void addEast(DataSetSeismogram[] newEast, TimeConfig tc){
        east = addToDisplay(east, newEast, tc, DisplayUtils.EAST);
    }

    private void addZ(DataSetSeismogram[] newZ, TimeConfig tc){
        z = addToDisplay(z, newZ, tc, DisplayUtils.UP);
    }

    private BasicSeismogramDisplay addToDisplay(BasicSeismogramDisplay display, DataSetSeismogram[] seismos,
                                                TimeConfig tc, String orientation){
        if(seismos.length > 0){
            if(display == null){
                display = new BasicSeismogramDisplay(tc, this);
                initializeBSD(display, 0, orientation);
            }
            display.add(seismos);
        }
        return display;
    }

    private void initializeBSD(BasicSeismogramDisplay disp, int position, String orientation){
        getCenterPanel().add(disp, position);
        disp.addTitle(orientation, CENTER_LEFT);
        basicDisplays.add(disp);
    }

    public void setIndividualizedAmpConfig(AmpConfig ac){
        Iterator it = basicDisplays.iterator();
        Class configClass = ac.getClass();
        while(it.hasNext()){
            try{
                ((BasicSeismogramDisplay)it.next()).setAmpConfig(new IndividualizedAmpConfig((AmpConfig)configClass.newInstance()));
            }catch(IllegalAccessException e){
                GlobalExceptionHandler.handle("Problem creating ampConfig from class", e);
            }catch(InstantiationException e){
                GlobalExceptionHandler.handle("Problem creating ampConfig from class", e);
            }
        }
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

    private BasicSeismogramDisplay north, east, z;
}// ComponentSortedSeismogramDisplay
